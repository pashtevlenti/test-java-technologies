package ru.kpfu.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.model.InventoryReservation;

import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Integer> {
    List<InventoryReservation> findAllBySagaId(UUID sagaId);
}
