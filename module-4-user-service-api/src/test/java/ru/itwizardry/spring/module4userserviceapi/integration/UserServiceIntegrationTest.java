package ru.itwizardry.spring.module4userserviceapi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = UserServiceIntegrationTest.Initializer.class)
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Создание пользователя через сервис (интеграционно)")
    void createUser_savesAndReturnsUser() {
        var request = new UserCreateRequest("Integration", "int@test.com", 33);
        UserDto created = userService.createUser(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Integration");

        UserDto fetched = userService.getUserById(created.id());
        assertThat(fetched.email()).isEqualTo("int@test.com");
    }
}
