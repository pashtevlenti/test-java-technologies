package ru.kpfu.itis.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.model.Outbox;
import ru.kpfu.itis.repository.OutboxRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Component
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final OutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    public void dispatch() {
        List<Outbox> rows = outboxRepo.findUnpublished();
        for (Outbox r : rows) {
            try {
                kafkaTemplate.send(r.getTopic(), r.getPayload()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            r.setPublished(true);
            outboxRepo.save(r);
        }
    }
}