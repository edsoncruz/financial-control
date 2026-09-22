package com.cruz.financialcontrol.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(

        @NotNull
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotNull
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 50)
        String newPassword
) {}
