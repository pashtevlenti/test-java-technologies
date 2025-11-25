package ru.kpfu.itis.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@Entity
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private UUID sagaId;

    private Integer accountId;

    private BigDecimal amount;

    private boolean success;
}
