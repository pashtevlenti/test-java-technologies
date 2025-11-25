package ru.kpfu.itis.dto;

import java.math.BigDecimal;

public record CreateOrderDto(String itemsJson, BigDecimal total) {
}
