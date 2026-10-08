package com.cruz.financialcontrol.model.dto.transactioncategory;

import com.cruz.financialcontrol.model.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTransactionCategoryDTO(
        @NotBlank(message = "Description is required")
        @Size(max = 50, message = "Description must not exceed 50 characters")
        String description,

        @NotNull(message = "Transaction type is required")
        TransactionType transactionType
) {}
