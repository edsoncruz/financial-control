package com.cruz.financialcontrol.model.entity;

import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import com.cruz.financialcontrol.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TRANSACTION_CATEGORY")
public class TransactionCategory extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
}
