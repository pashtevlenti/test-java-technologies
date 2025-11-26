package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public void onOrderCreated(String message){
        JsonNode node = null;
        try {
            node = objectMapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            String itemsJson = node.get("items").toString();
            inventoryService.handleOrderCreated(UUID.fromString(sagaId), itemsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    @KafkaListener(topics = "order.complete.request", groupId = "inventory-group")
    public void onOrderComplete(String message){
        JsonNode node = null;
        try {
            node = objectMapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            inventoryService.handleOrderCompleteRequest(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @KafkaListener(topics = "payment.failed", groupId = "inventory-group")
    public void onOrderFailed(String message){
        JsonNode node = null;
        try {
            node = objectMapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            inventoryService.handleOrderFailed(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
