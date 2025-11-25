package ru.kpfu.itis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.dto.AddItemDto;
import ru.kpfu.itis.model.InventoryEntity;
import ru.kpfu.itis.model.InventoryReservation;
import ru.kpfu.itis.repository.InventoryRepository;
import ru.kpfu.itis.repository.InventoryReservationRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    @GetMapping("/items")
    public List<InventoryEntity> getItems() {
        return inventoryRepository.findAll();
    }

    @GetMapping("/reservations")
    public List<InventoryReservation> getReservations() {
        return reservationRepository.findAll();
    }

}


