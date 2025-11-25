package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void onOrderCreated(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        Integer sagaId = node.get("sagaId").asInt();
        String itemsJson = node.get("items").toString();
        inventoryService.handleOrderCreated(sagaId, itemsJson);
    }

    @KafkaListener(topics = "order.complete.request", groupId = "inventory-group")
    public void onOrderComplete(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        Integer sagaId = node.get("sagaId").asInt();
        inventoryService.handleOrderCompleteRequest(sagaId);
    }

    @KafkaListener(topics = "payment.failed", groupId = "inventory-group")
    public void onOrderFailed(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        Integer sagaId = node.get("sagaId").asInt();
        inventoryService.handleOrderFailed(sagaId);
    }
}
