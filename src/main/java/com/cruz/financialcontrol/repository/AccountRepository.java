package com.cruz.financialcontrol.repository;

import com.cruz.financialcontrol.model.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Page<Account> findAllByUserId(Long userId, Pageable pageable);

    Optional<Account> findByNameAndUserId(String name, Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

}
