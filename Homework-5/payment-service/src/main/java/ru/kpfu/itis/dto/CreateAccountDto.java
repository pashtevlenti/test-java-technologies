package ru.kpfu.itis.dto;

import java.math.BigDecimal;

public record CreateAccountDto(String username, BigDecimal balance) {}
