package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.service.OrderService;

@Component
@RequiredArgsConstructor
public class OrderListeners {

    private final OrderService orderService;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "inventory.reserved", groupId = "order-group")
    public void onInventoryReserved(String payload) throws Exception {
        JsonNode n = mapper.readTree(payload);
        int sagaId = n.get("sagaId").asInt();
        orderService.handleInventoryReserved(sagaId);
    }

    @KafkaListener(topics = "payment.paid", groupId = "order-group")
    public void onPaymentPaid(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        orderService.handlePaymentPaid(node.get("sagaId").asInt());
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-group")
    public void onPaymentFailed(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        orderService.handlePaymentFailed(node.get("sagaId").asInt());
    }

    @KafkaListener(topics = "order.complete.request", groupId = "order-group")
    public void onComplete(String message) throws Exception {
        JsonNode node = mapper.readTree(message);
        orderService.handleOrderCompleteRequest(node.get("sagaId").asInt());
    }
}