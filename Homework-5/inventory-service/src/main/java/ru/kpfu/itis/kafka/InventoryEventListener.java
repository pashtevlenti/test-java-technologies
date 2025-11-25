package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.service.InventoryService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void onOrderCreated(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        String itemsJson = node.get("items").toString();
        inventoryService.handleOrderCreated(UUID.fromString(sagaId), itemsJson);
    }

    @KafkaListener(topics = "order.complete.request", groupId = "inventory-group")
    public void onOrderComplete(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        inventoryService.handleOrderCompleteRequest(UUID.fromString(sagaId));
    }

    @KafkaListener(topics = "payment.failed", groupId = "inventory-group")
    public void onOrderFailed(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        String sagaId = String.valueOf(node.get("sagaId"));
        inventoryService.handleOrderFailed(UUID.fromString(sagaId));
    }
}
