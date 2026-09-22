package com.cruz.financialcontrol.model.entity;

import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ACCOUNTS")
public class Account extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String name;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    /**
     * The version field is used for optimistic locking in JPA. It is automatically incremented
     * each time the entity is updated. When an update is attempted, JPA checks that the version
     * in the database matches the version in the entity. If they do not match, it means another
     * transaction has modified the entity, and an OptimisticLockException is thrown.
     */
    @Version
    @Column(nullable = false)
    private Long version;
}
