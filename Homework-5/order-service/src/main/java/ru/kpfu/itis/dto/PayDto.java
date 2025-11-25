package ru.kpfu.itis.dto;

import java.math.BigDecimal;

public record PayDto(String username, BigDecimal amount) {
}
