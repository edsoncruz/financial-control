package com.cruz.financialcontrol.model.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Class Name: TransferDTO
 * Description:
 *
 * @author edson
 * @date 11/09/2026
 */
public record UpdateTransferDTO(

        @NotNull
        Long originAccountId,

        @NotNull
        Long destinationAccountId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        LocalDate date
) {}
