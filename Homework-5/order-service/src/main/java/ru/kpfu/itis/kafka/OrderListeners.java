package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public void onInventoryReserved(String payload) {
        JsonNode n = null;
        try {
            n = mapper.readTree(payload);
            String sagaId = String.valueOf(n.get("sagaId"));
            orderService.handleInventoryReserved(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-group")
    public void onInventoryFailed(String message) {
        JsonNode node = null;
        try {
            node = mapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String sagaId = String.valueOf(node.get("sagaId"));
        orderService.handleInventoryFailed(UUID.fromString(sagaId));
    }

    @KafkaListener(topics = "payment.paid", groupId = "order-group")
    public void onPaymentPaid(String message) {
        JsonNode node = null;
        try {
            node = mapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            orderService.handlePaymentPaid(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @KafkaListener(topics = "payment.failed", groupId = "order-group")
    public void onPaymentFailed(String message) {
        JsonNode node = null;
        try {
            node = mapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            orderService.handlePaymentFailed(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @KafkaListener(topics = "order.complete.request", groupId = "order-group")
    public void onComplete(String message){
        JsonNode node = null;
        try {
            node = mapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            orderService.handleOrderCompleteRequest(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}