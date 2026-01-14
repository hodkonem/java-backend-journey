package ru.itwizardry.spring.module4userserviceapi.event.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.itwizardry.spring.module4userserviceapi.config.AppKafkaProperties;
import ru.itwizardry.spring.module4userserviceapi.domain.event.UserCreatedEvent;
import ru.itwizardry.spring.module4userserviceapi.event.dto.UserEventDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedKafkaListener {

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;
    private final AppKafkaProperties props;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserCreatedEvent event) {
        UserEventDto dto = UserEventDto.userCreated(
                event.id(),
                event.email(),
                event.name(),
                event.age()
        );

        String topic = props.topic();
        kafkaTemplate.send(topic, event.id().toString(), dto);

        log.info("Published USER_CREATED after commit. topic={}, userId={}", topic, event.id());
    }
}
