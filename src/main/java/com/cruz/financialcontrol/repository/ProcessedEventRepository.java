package com.cruz.financialcontrol.repository;

import com.cruz.financialcontrol.model.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Class Name: ProcessedEventRepository
 * Description:
 *
 * @author edson
 * @date 22/09/2026
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    /**
     * Atomically records that {@code eventId} has been processed, returning the number of rows
     * inserted: 1 if this is the first time it's been seen, 0 if a row already exists (i.e. this
     * is a Kafka redelivery/duplicate that should be skipped). The {@code ON CONFLICT DO NOTHING}
     * makes this safe under concurrent consumers without needing a prior SELECT.
     */
    @Modifying
    @Query(value = "INSERT INTO processed_events (event_id, processed_at) VALUES (:eventId, now()) ON CONFLICT (event_id) DO NOTHING",
            nativeQuery = true)
    int tryMarkProcessed(@Param("eventId") Long eventId);
}
