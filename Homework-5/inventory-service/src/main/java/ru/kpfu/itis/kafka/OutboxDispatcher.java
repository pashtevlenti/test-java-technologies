package ru.kpfu.itis.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    public void dispatch() {
        List<Outbox> events = outboxRepository.findByProcessedFalse();
        for (Outbox event : events) {
            kafkaTemplate.send(event.getTopic(), event.getPayload());
            event.setProcessed(true);
            outboxRepository.save(event);
        }
    }
}
