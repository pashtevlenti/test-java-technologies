package ru.kpfu.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kpfu.itis.model.Outbox;

import java.util.List;


public interface OutboxRepository extends JpaRepository<Outbox, Integer> {
    @Query("select o from Outbox o where o.published = false order by o.createdAt")
    List<Outbox> findUnpublished();
}