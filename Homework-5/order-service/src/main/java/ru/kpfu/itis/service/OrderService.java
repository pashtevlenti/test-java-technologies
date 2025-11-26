package ru.kpfu.itis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.model.OrderEntity;
import ru.kpfu.itis.model.OrderStatus;
import ru.kpfu.itis.model.Outbox;
import ru.kpfu.itis.repository.OrderRepository;
import ru.kpfu.itis.repository.OutboxRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    public Optional<OrderEntity> findBySaga(UUID sagaId) {
        return orderRepository.findBySagaId(sagaId);
    }

    @Transactional
    public OrderEntity createOrder(String itemsJson, BigDecimal total) {
        UUID sagaId = UUID.randomUUID();

        OrderEntity order = OrderEntity.builder()
                .sagaId(sagaId)
                .status(OrderStatus.CREATED)
                .itemsJson(itemsJson)
                .total(total)
                .build();

        orderRepository.save(order);

        Outbox outbox = Outbox.builder()
                .sagaId(sagaId)
                .topic("order.created")
                .payload("{\"sagaId\":%s,\"orderId\":%d, \"items\":%s}".formatted(sagaId, order.getId(),itemsJson))
                .build();

        outboxRepository.save(outbox);

        return order;
    }

    @Transactional
    public boolean pay(UUID sagaId, String username, BigDecimal amount) {
        Optional<OrderEntity> optional = orderRepository.findBySagaId(sagaId);
        if (optional.isEmpty()) return false;

        OrderEntity order = optional.get();
        if (!(order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.RESERVED)) {
            return false;
        }

        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "sagaId", sagaId,
                            "orderId", order.getId(),
                            "username", username,
                            "amount", amount
                    )
            );

            Outbox outbox = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("order.payment.request")
                    .payload(payload)
                    .build();

            outboxRepository.save(outbox);

        } catch (Exception ignored) {
            throw new RuntimeException();
        }

        return true;
    }

    @Transactional
    public void handleInventoryReserved(UUID sagaId) {
        orderRepository.findBySagaId(sagaId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.CREATED) {
                OrderEntity updated = OrderEntity.builder()
                        .id(order.getId())
                        .sagaId(order.getSagaId())
                        .status(OrderStatus.RESERVED)
                        .itemsJson(order.getItemsJson())
                        .total(order.getTotal())
                        .build();

                orderRepository.save(updated);
            }
        });
    }

    @Transactional
    public void handlePaymentPaid(UUID sagaId) {
        orderRepository.findBySagaId(sagaId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.RESERVED) {

                OrderEntity updated = OrderEntity.builder()
                        .id(order.getId())
                        .sagaId(order.getSagaId())
                        .status(OrderStatus.PAID)
                        .itemsJson(order.getItemsJson())
                        .total(order.getTotal())
                        .build();

                orderRepository.save(updated);

                Outbox out = Outbox.builder()
                        .sagaId(sagaId)
                        .topic("order.complete.request")
                        .payload("{\"sagaId\":%s,\"orderId\":%d}".formatted(sagaId, order.getId()))
                        .build();

                outboxRepository.save(out);
            }
        });
    }
    @Transactional
    public void handleOrderCompleteRequest(UUID sagaId) {
        orderRepository.findBySagaId(sagaId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PAID) {

                OrderEntity updated = OrderEntity.builder()
                        .id(order.getId())
                        .sagaId(order.getSagaId())
                        .status(OrderStatus.COMPLETED)
                        .itemsJson(order.getItemsJson())
                        .total(order.getTotal())
                        .build();

                orderRepository.save(updated);
            }
        });
    }
    @Transactional
    public void handleInventoryFailed(UUID sagaId) {
        orderRepository.findBySagaId(sagaId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.CREATED) {
                OrderEntity updated = OrderEntity.builder()
                        .id(order.getId())
                        .sagaId(order.getSagaId())
                        .status(OrderStatus.FAILED)
                        .itemsJson(order.getItemsJson())
                        .total(order.getTotal())
                        .build();
                orderRepository.save(updated);
            }
        });
    }
    @Transactional
    public void handlePaymentFailed(UUID sagaId) {
        orderRepository.findBySagaId(sagaId).ifPresent(order -> {
            if (order.getStatus() != OrderStatus.COMPLETED) {

                OrderEntity updated = OrderEntity.builder()
                        .id(order.getId())
                        .sagaId(order.getSagaId())
                        .status(OrderStatus.FAILED)
                        .itemsJson(order.getItemsJson())
                        .total(order.getTotal())
                        .build();

                orderRepository.save(updated);
            }
        });
    }

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }
}
