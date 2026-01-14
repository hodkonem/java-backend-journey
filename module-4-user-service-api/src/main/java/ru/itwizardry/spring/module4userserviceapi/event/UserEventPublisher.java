package ru.itwizardry.spring.module4userserviceapi.event;

import ru.itwizardry.spring.module4userserviceapi.domain.event.UserCreatedEvent;

public interface UserEventPublisher {
    void publish(UserCreatedEvent event);
}

