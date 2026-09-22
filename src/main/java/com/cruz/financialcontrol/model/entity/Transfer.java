package com.cruz.financialcontrol.model.entity;

import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Class Name: Transfer
 * Description:
 *
 * @author edson
 * @date 14/09/2026
 */
@Getter
@Setter
@Entity
@Table(name = "TRANSFERS")
public class Transfer extends BaseEntity {

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Transaction origin;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Transaction destination;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;
}


