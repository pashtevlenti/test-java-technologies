package ru.kpfu.itis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "outbox_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer sagaId;
    private String topic;
    @Column(columnDefinition = "text")
    private String payload;
    private boolean published = false;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}