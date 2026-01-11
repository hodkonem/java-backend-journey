package ru.itwizardry.spring.module4userserviceapi.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be at most 100 characters") String email,
        @NotNull
        @Min(1)
        @Max(150)
        Integer age
) {
}
