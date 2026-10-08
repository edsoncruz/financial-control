package com.cruz.financialcontrol.repository;

import com.cruz.financialcontrol.model.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Class Name: TransactionCategoryRepository
 * Description:
 *
 * @author edson
 * @date 07/10/2026
 */
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    Optional<TransactionCategory> findByDescription(String description);
}
