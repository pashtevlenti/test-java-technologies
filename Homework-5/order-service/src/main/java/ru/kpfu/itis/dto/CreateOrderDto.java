package ru.kpfu.itis.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderDto(List<ItemDto> items, BigDecimal total) {}