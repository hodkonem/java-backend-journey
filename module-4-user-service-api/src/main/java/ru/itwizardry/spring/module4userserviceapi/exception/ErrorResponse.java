package ru.itwizardry.spring.module4userserviceapi.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Object message
) {}
