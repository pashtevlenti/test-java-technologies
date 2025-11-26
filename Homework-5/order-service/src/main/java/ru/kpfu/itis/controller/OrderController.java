package ru.kpfu.itis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.dto.CreateOrderDto;
import ru.kpfu.itis.dto.PayDto;
import ru.kpfu.itis.model.OrderEntity;
import ru.kpfu.itis.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<OrderEntity> create(@RequestBody CreateOrderDto dto) throws JsonProcessingException {
        String itemsJson = objectMapper.writeValueAsString(dto.items());
        return ResponseEntity.ok(orderService.createOrder(itemsJson, dto.total()));
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<OrderEntity> find(@PathVariable UUID sagaId) {
        Optional<OrderEntity> order = orderService.findBySaga(sagaId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<OrderEntity> getAll() {
        return orderService.getAllOrders();
    }

    @PostMapping("/{sagaId}/pay")
    public ResponseEntity<String> pay(@PathVariable UUID sagaId, @RequestBody PayDto dto) {
        Optional<OrderEntity> order = orderService.findBySaga(sagaId);
        if (order.isEmpty()) return ResponseEntity.notFound().build();

        BigDecimal amount = dto.amount() == null ? order.get().getTotal() : dto.amount();

        boolean result = orderService.pay(sagaId, dto.username(), amount);
        return result
                ? ResponseEntity.accepted().body("PAYMENT_REQUESTED")
                : ResponseEntity.status(409).body("INVALID_STATE");
    }
}

