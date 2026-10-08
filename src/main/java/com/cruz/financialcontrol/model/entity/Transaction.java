package com.cruz.financialcontrol.model.entity;

import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "TRANSACTIONS")
public class Transaction extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String description;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TransactionCategory category;
}



