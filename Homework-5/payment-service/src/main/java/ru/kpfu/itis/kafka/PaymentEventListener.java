package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.payment.request", groupId = "payment-group")
    public void onPaymentRequest(String message){
        JsonNode node = null;
        try {
            node = objectMapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            Integer orderId = node.get("orderId").asInt();
            String username = node.get("username").asText();
            BigDecimal amount = node.get("amount").decimalValue();

            paymentService.handlePaymentRequest(UUID.fromString(sagaId), orderId, username, amount);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
}

    @KafkaListener(topics = "payment.failed", groupId = "payment-group")
    public void onRefundRequest(String message){
        JsonNode node = null;
        try {
            node = objectMapper.readTree(message);
            String sagaId = String.valueOf(node.get("sagaId"));
            paymentService.refund(UUID.fromString(sagaId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
