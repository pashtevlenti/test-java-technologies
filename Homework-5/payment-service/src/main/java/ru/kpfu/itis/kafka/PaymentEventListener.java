package ru.kpfu.itis.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.service.PaymentService;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.payment.request", groupId = "payment-group")
    public void onPaymentRequest(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        Integer sagaId = node.get("sagaId").asInt();
        Integer orderId = node.get("orderId").asInt();
        String username = node.get("username").asText();
        java.math.BigDecimal amount = node.get("amount").decimalValue();

        paymentService.handlePaymentRequest(sagaId, orderId, username, amount);
    }

    @KafkaListener(topics = "payment.failed", groupId = "payment-group")
    public void onRefundRequest(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        Integer sagaId = node.get("sagaId").asInt();
        paymentService.refund(sagaId);
    }
}
