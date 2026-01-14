package ru.itwizardry.spring.module4userserviceapi.domain.event;

public record UserCreatedEvent(
        Long id,
        String email,
        String name,
        Integer age
) {}
