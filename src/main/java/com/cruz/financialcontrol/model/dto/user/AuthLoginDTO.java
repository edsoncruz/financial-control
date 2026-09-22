package com.cruz.financialcontrol.model.dto.user;

import jakarta.validation.constraints.*;

public record AuthLoginDTO(

        @Email
        @NotNull
        @NotBlank(message = "Email is required")
        @Size(min = 3, max = 50)
        String email,

        @NotNull
        @NotBlank(message = "Password is required")
        String password
) {}
