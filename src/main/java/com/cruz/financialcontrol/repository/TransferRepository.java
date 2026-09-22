package com.cruz.financialcontrol.repository;

import com.cruz.financialcontrol.model.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class Name: TransferRepository
 * Description:
 *
 * @author edson
 * @date 14/09/2026
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Page<Transfer> findAllByOriginAccountUserId(Long userId, Pageable pageable);
}
