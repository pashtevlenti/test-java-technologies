package ru.kpfu.itis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.model.InventoryEntity;
import ru.kpfu.itis.model.InventoryReservation;
import ru.kpfu.itis.model.Outbox;
import ru.kpfu.itis.repository.InventoryRepository;
import ru.kpfu.itis.repository.InventoryReservationRepository;
import ru.kpfu.itis.repository.OutboxRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleOrderCreated(Integer sagaId, String itemsJson) {
        try {
            List<Map<String, Object>> items =
                    objectMapper.readValue(itemsJson, List.class);

            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                Integer qty = (Integer) item.get("quantity");

                InventoryEntity inventory = inventoryRepository
                        .findByItemName(name)
                        .orElse(null);

                if (inventory == null || inventory.getQuantity() < qty) {
                    Outbox fail = Outbox.builder()
                            .sagaId(sagaId)
                            .topic("inventory.failed")
                            .payload("{\"sagaId\":%d}".formatted(sagaId))
                            .build();

                    outboxRepository.save(fail);
                    return;
                }

                InventoryEntity updated = InventoryEntity.builder()
                        .id(inventory.getId())
                        .itemName(inventory.getItemName())
                        .quantity(inventory.getQuantity() - qty)
                        .build();

                inventoryRepository.save(updated);

                InventoryReservation reservation = InventoryReservation.builder()
                        .sagaId(sagaId)
                        .itemName(name)
                        .quantity(qty)
                        .build();

                reservationRepository.save(reservation);
            }

            Outbox success = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("inventory.reserved")
                    .payload("{\"sagaId\":%d}".formatted(sagaId))
                    .build();

            outboxRepository.save(success);

        } catch (Exception ignored) {}
    }

    @Transactional
    public void handleOrderCompleteRequest(Integer sagaId) {
        List<InventoryReservation> reservations =
                reservationRepository.findAllBySagaId(sagaId);

        reservationRepository.deleteAll(reservations);

        Outbox complete = Outbox.builder()
                .sagaId(sagaId)
                .topic("inventory.completed")
                .payload("{\"sagaId\":%d}".formatted(sagaId))
                .build();

        outboxRepository.save(complete);
    }

    @Transactional
    public void handleOrderFailed(Integer sagaId) {
        List<InventoryReservation> reservations =
                reservationRepository.findAllBySagaId(sagaId);

        for (InventoryReservation res : reservations) {
            inventoryRepository.findByItemName(res.getItemName()).ifPresent(inv -> {

                InventoryEntity restored = InventoryEntity.builder()
                        .id(inv.getId())
                        .itemName(inv.getItemName())
                        .quantity(inv.getQuantity() + res.getQuantity())
                        .build();

                inventoryRepository.save(restored);
            });

            reservationRepository.delete(res);
        }
    }
}
