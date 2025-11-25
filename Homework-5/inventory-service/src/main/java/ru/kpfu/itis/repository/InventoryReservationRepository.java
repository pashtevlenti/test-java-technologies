package ru.kpfu.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.model.InventoryReservation;

import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Integer> {
    List<InventoryReservation> findAllBySagaId(Integer sagaId);
}
