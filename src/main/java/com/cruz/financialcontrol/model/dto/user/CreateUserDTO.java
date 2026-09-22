package com.cruz.financialcontrol.model.dto.user;

import jakarta.validation.constraints.*;

public record CreateUserDTO(
        @NotNull
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50)
        String name,

        @Email
        @NotNull
        @NotBlank(message = "Email is required")
        @Size(min = 3, max = 50)
        String email,

        @NotNull
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50)
        String password
) {}
