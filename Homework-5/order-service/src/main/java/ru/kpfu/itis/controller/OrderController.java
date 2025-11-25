package ru.kpfu.itis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.dto.CreateOrderDto;
import ru.kpfu.itis.dto.PayDto;
import ru.kpfu.itis.model.OrderEntity;
import ru.kpfu.itis.service.OrderService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderEntity> create(@RequestBody CreateOrderDto dto) {
        return ResponseEntity.ok(orderService.createOrder(dto.itemsJson(), dto.total()));
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<OrderEntity> find(@PathVariable UUID sagaId) {
        Optional<OrderEntity> order = orderService.findBySaga(sagaId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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

