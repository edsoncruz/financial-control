package com.cruz.financialcontrol.model.dto.user;

import jakarta.validation.constraints.*;

public record UpdateUserDTO(

        @NotNull
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50)
        String name,

        @Email
        @NotNull
        @NotBlank(message = "Email is required")
        @Size(min = 3, max = 50)
        String email
) {}
