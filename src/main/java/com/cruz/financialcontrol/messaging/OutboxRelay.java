package com.cruz.financialcontrol.messaging;

import com.cruz.financialcontrol.model.entity.OutboxEvent;
import com.cruz.financialcontrol.model.enums.OutboxStatus;
import com.cruz.financialcontrol.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Class Name: OutboxRelay
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxRelay {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void relay() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByIdAsc(OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));

        for (OutboxEvent event : pending) {
            publishOne(event);
        }
    }

    @Transactional
    void publishOne(OutboxEvent event) {
        try {
            // Key by aggregateId so all events for the same user stay ordered on one partition.
            kafkaTemplate.send(KafkaTopics.USER_EVENTS, event.getAggregateId(), event.getPayload()).get(); // synchronous ack; swap for async callback if throughput demands it

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
        } catch (Exception ex) {
            event.setAttempts(event.getAttempts() + 1);
            if (event.getAttempts() >= MAX_ATTEMPTS) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("Outbox event {} failed permanently after {} attempts", event.getId(), event.getAttempts(), ex);
            } else {
                log.warn("Outbox event {} publish attempt {} failed", event.getId(), event.getAttempts(), ex);
            }
        }
        outboxEventRepository.save(event);
    }
}
