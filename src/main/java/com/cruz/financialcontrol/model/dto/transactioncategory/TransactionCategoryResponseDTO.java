package com.cruz.financialcontrol.model.dto.transactioncategory;

import com.cruz.financialcontrol.model.enums.TransactionType;

import java.io.Serializable;

public record TransactionCategoryResponseDTO(
        Long id,
        String description,
        TransactionType transactionType
) implements Serializable {}
