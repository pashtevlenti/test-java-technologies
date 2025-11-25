package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.service.OrderService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderListeners {

    private final OrderService orderService;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "inventory.reserved", groupId = "order-group")
    public void onInventoryReserved(String payload) throws Exception {
        JsonNode n = mapper.readTree(payload);
        String sagaId = String.valueOf(n.get("sagaId"));
        orderService.handleInventoryReserved(UUID.fromString(sagaId));
    }

    @KafkaListener(topics = "payment.paid", groupId = "order-group")
    public void onPaymentPaid(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        orderService.handlePaymentPaid(UUID.fromString(sagaId));
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-group")
    public void onPaymentFailed(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        orderService.handlePaymentFailed(UUID.fromString(sagaId));
    }

    @KafkaListener(topics = "order.complete.request", groupId = "order-group")
    public void onComplete(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        orderService.handleOrderCompleteRequest(UUID.fromString(sagaId));
    }
}