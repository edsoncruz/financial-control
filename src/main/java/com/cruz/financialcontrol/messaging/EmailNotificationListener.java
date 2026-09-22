package com.cruz.financialcontrol.messaging;

import com.cruz.financialcontrol.model.event.UserCreatedEvent;
import com.cruz.financialcontrol.repository.ProcessedEventRepository;
import com.cruz.financialcontrol.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;


/**
 * Class Name: EmailNotificationListener
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final JsonMapper jsonMapper;
    private final EmailService emailService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "email-notifications")
    @Transactional
    public void onUserEvent(String payload) {
        UserCreatedEvent event = jsonMapper.readValue(payload, UserCreatedEvent.class);

        // Atomic "insert if absent": returns 0 rows inserted when this event id was already
        // processed, which is how we detect and no-op Kafka redeliveries/duplicates.
        if (processedEventRepository.tryMarkProcessed(event.eventId()) == 0) {
            log.info("Event {} already processed, skipping duplicate delivery", event.eventId());
            return;
        }

        emailService.sendWelcomeEmail(event.email(), event.name());

        log.info("Welcome email dispatched for user {}", event.userId());
    }
}