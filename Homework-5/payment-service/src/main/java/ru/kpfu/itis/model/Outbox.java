package ru.kpfu.itis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "outbox_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private UUID sagaId;
    private String topic;
    @Column(columnDefinition = "text")
    private String payload;
    private boolean published = false;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}