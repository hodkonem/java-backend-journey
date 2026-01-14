package ru.itwizardry.spring.module4userserviceapi.event.dto;

public record UserEventDto(String type, Long id, String email, String name, Integer age) {
    public static UserEventDto userCreated(Long id, String email, String name, Integer age) {
        return new UserEventDto("USER_CREATED", id, email, name, age);
    }
}
