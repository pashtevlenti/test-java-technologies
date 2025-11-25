package ru.kpfu.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.model.PaymentTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findBySagaId(UUID sagaId);
}
