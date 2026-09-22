package com.cruz.financialcontrol.model.dto.transfer;

import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponseDTO(
        Long id,
        TransactionResponseDTO origin,
        TransactionResponseDTO destination,
        BigDecimal amount,
        LocalDate date
) {}
