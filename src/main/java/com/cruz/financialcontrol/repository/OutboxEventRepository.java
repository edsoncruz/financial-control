package com.cruz.financialcontrol.repository;


import com.cruz.financialcontrol.model.entity.OutboxEvent;
import com.cruz.financialcontrol.model.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Class Name: OutboxEventRepository
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusOrderByIdAsc(OutboxStatus status, Pageable pageable);
}