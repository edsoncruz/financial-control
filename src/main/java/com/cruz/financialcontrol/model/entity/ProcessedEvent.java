package com.cruz.financialcontrol.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Class Name: ProcessedEvent
 * Description: Dedupe ledger for Kafka consumers. The primary key is the source event's own
 * unique id (e.g. the originating {@link OutboxEvent#getId()}), not a synthetic one — this lets
 * {@link com.cruz.financialcontrol.repository.ProcessedEventRepository#tryMarkProcessed(Long)}
 * do a single atomic "insert if absent" per message, making listener processing idempotent
 * under Kafka's at-least-once redelivery semantics.
 *
 * @author edson
 * @date 22/09/2026
 */
@Getter
@Setter
@Entity
@Table(name = "PROCESSED_EVENTS")
public class ProcessedEvent {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
