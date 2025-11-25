package ru.kpfu.itis.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private UUID sagaId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(columnDefinition = "text")
    private String itemsJson;

    private BigDecimal total;
}