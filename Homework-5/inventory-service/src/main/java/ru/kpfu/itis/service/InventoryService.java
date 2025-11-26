package ru.kpfu.itis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.dto.ItemDto;
import ru.kpfu.itis.model.InventoryEntity;
import ru.kpfu.itis.model.InventoryReservation;
import ru.kpfu.itis.model.Outbox;
import ru.kpfu.itis.repository.InventoryRepository;
import ru.kpfu.itis.repository.InventoryReservationRepository;
import ru.kpfu.itis.repository.OutboxRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleOrderCreated(UUID sagaId, String itemsJson) {
        try {
            List<ItemDto> items = objectMapper.readValue(itemsJson, new TypeReference<List<ItemDto>>() {});

            List<InventoryReservation> tempReservations = new ArrayList<>();
            boolean allReserved = true;

            for (ItemDto item : items) {
                String name = item.name();
                int qty = item.quantity();

                int updated = inventoryRepository.reserveAtomically(name, qty);
                if (updated == 0) {
                    allReserved = false;
                    break;
                }

                InventoryReservation reservation = InventoryReservation.builder()
                        .sagaId(sagaId)
                        .itemName(name)
                        .quantity(qty)
                        .build();
                reservationRepository.save(reservation);
                tempReservations.add(reservation);
            }

            if (!allReserved) {
                for (InventoryReservation res : tempReservations) {
                    inventoryRepository.reserveAtomically(res.getItemName(), -res.getQuantity());  // Add back
                    reservationRepository.delete(res);
                }

                Outbox fail = Outbox.builder()
                        .sagaId(sagaId)
                        .topic("inventory.failed")
                        .payload(String.format("{\"sagaId\":\"%s\"}", sagaId))
                        .build();
                outboxRepository.save(fail);
                return;
            }

            Outbox success = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("inventory.reserved")
                    .payload(String.format("{\"sagaId\":\"%s\"}", sagaId))
                    .build();
            outboxRepository.save(success);

        } catch (Exception e) {
            Outbox fail = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("inventory.failed")
                    .payload(String.format("{\"sagaId\":\"%s\"}", sagaId))
                    .build();
            outboxRepository.save(fail);
        }
    }



    @Transactional
    public void handleOrderCompleteRequest(UUID sagaId) {
        List<InventoryReservation> reservations =
                reservationRepository.findAllBySagaId(sagaId);

        reservationRepository.deleteAll(reservations);

        Outbox complete = Outbox.builder()
                .sagaId(sagaId)
                .topic("inventory.completed")
                .payload("{\"sagaId\":%s}".formatted(sagaId))
                .build();

        outboxRepository.save(complete);
    }

    @Transactional
    public void handleOrderFailed(UUID sagaId) {

        try {
            List<InventoryReservation> reservations = reservationRepository.findAllBySagaId(sagaId);

            for (InventoryReservation res : reservations) {
                String name = res.getItemName();
                int qty = res.getQuantity();

                int updated = inventoryRepository.reserveAtomically(name, -qty);

                if (updated == 0) {
                    Outbox fail = Outbox.builder()
                            .sagaId(sagaId)
                            .topic("inventory.rollback.failed")
                            .payload("""
                            {"sagaId":"%s","item":"%s"}
                            """.formatted(sagaId, name))
                            .build();

                    outboxRepository.save(fail);
                    continue;
                }

                reservationRepository.delete(res);
            }

            Outbox success = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("inventory.rollbacked")
                    .payload("""
                    {"sagaId":"%s"}
                    """.formatted(sagaId))
                    .build();

            outboxRepository.save(success);

        } catch (Exception e) {
            Outbox fail = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("inventory.rollback.failed")
                    .payload("""
                    {"sagaId":"%s"}
                    """.formatted(sagaId))
                    .build();

            outboxRepository.save(fail);
        }
    }

}
