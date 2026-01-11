package ru.itwizardry.spring.module4userserviceapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.model.User;
import ru.itwizardry.spring.module4userserviceapi.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("PUT /api/users/{id} — пользователь успешно обновляется")
    void updateUser_shouldUpdateCorrectly() throws Exception {
        User user = User.builder()
                .name("Old Name")
                .email("old@example.com")
                .age(30)
                .build();
        userRepository.save(user);

        UserUpdateRequest update = new UserUpdateRequest("New Name", "new@example.com", 35);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getAge()).isEqualTo(35);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — пользователь удаляется")
    void deleteUser_shouldRemoveFromDatabase() throws Exception {
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .age(28)
                .build();
        userRepository.save(user);

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        boolean exists = userRepository.existsById(user.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("POST /api/users — создаёт нового пользователя")
    void createUser_shouldPersistAndReturn() throws Exception {
        var request = new UserCreateRequest("Created User", "created@example.com", 32);

        var result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Created User"))
                .andReturn();

        var responseJson = result.getResponse().getContentAsString();
        var response = objectMapper.readTree(responseJson);
        Long userId = response.get("id").asLong();

        var savedUser = userRepository.findById(userId);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("created@example.com");
    }

    @Test
    @DisplayName("GET /api/users/{id} — возвращает пользователя по ID")
    void getUserById_shouldReturnUser() throws Exception {
        var user = userRepository.save(User.builder()
                .name("Test User")
                .email("testuser@example.com")
                .age(29)
                .build());

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }
}
