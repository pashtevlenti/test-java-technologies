package ru.kpfu.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.model.InventoryEntity;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {
    Optional<InventoryEntity> findByItemName(String itemName);

    @Modifying
    @Query("update InventoryEntity i set i.quantity = i.quantity - :qty where i.itemName = :name and i.quantity >= :qty")
    int reserveAtomically(@Param("name") String name, @Param("qty") int qty);

}