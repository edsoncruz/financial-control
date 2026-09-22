package com.cruz.financialcontrol.model.dto.transaction;

import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate date,
        TransactionType type,
        TransactionStatus status,
        Long accountId
){}
