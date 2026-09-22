package com.cruz.financialcontrol.messaging;

import com.cruz.financialcontrol.model.event.UserCreatedEvent;
import com.cruz.financialcontrol.model.entity.OutboxEvent;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Class Name: OutboxEventPublisher
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    // Must be called within the caller's existing @Transactional boundary,
    // so the outbox row commits atomically with the business entity.
    public void publishUserCreated(User user) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("USER");
        event.setAggregateId(user.getId().toString());
        event.setEventType("USER_CREATED");
        event.setPayload("{}"); // placeholder; payload is NOT NULL and needs the row's own generated id first
        event = outboxEventRepository.saveAndFlush(event);

        // The outbox row's own id doubles as a stable, unique event id the consumer can use
        // to dedupe redelivered Kafka messages (see EmailNotificationListener).
        event.setPayload(jsonMapper.writeValueAsString(new UserCreatedEvent(event.getId(), user.getId(), user.getName(), user.getEmail())));
        outboxEventRepository.save(event);
    }
}
