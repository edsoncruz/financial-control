package com.cruz.financialcontrol.model.dto.transaction;

import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTransactionDTO(

        @NotNull
        @Size(min = 1, max = 50, message = "Description must be between 1 and 50 characters")
        String description,

        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Transaction status is required")
        TransactionStatus status,

        @NotNull
        Long transactionCategoryId

) {}
