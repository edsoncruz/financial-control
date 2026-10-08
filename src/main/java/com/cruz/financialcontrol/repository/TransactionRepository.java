package com.cruz.financialcontrol.repository;

import com.cruz.financialcontrol.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = "category")
    Page<Transaction> findAllByAccountUserId(Long userId, Pageable pageable);
}
