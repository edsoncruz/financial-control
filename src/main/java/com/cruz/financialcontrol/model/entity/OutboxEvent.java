package com.cruz.financialcontrol.model.entity;

/**
 * Class Name: OutboxEvent
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */


import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import com.cruz.financialcontrol.model.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Class Name: OutboxEvent
 * Description: Transactional outbox row: written in the SAME DB transaction as the business
 * change (e.g. User creation), guaranteeing at-least-once delivery to Kafka
 * without a dual-write problem. A separate relay process polls PENDING rows
 * and publishes them to the broker.
 * @author edson
 * @date 21/09/2026
 */

@Getter
@Setter
@Entity
@Table(name = "OUTBOX_EVENTS")
public class OutboxEvent extends BaseEntity {

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 50)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;
}
