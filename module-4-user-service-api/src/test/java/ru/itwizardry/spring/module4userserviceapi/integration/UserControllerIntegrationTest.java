package ru.itwizardry.spring.module4userserviceapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.event.dto.UserEventDto;
import ru.itwizardry.spring.module4userserviceapi.model.User;
import ru.itwizardry.spring.module4userserviceapi.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @MockBean
    KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("PUT /api/users/{id} — пользователь успешно обновляется")
    void updateUser_shouldUpdateCorrectly() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Old Name")
                .email("old@example.com")
                .age(30)
                .build());

        UserUpdateRequest update = new UserUpdateRequest("New Name", "new@example.com", 35);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.age").value(35));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getAge()).isEqualTo(35);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — пользователь удаляется")
    void deleteUser_shouldRemoveFromDatabase() throws Exception {
        User user = userRepository.save(User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .age(28)
                .build());

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    @DisplayName("POST /api/users — создаёт нового пользователя")
    void createUser_shouldPersistAndReturn() throws Exception {
        var request = new UserCreateRequest("Created User", "created@example.com", 32);

        var mvcResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Created User"))
                .andExpect(jsonPath("$.email").value("created@example.com"))
                .andExpect(jsonPath("$.age").value(32))
                .andReturn();

        Long id = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("id").asLong();
        assertThat(userRepository.findById(id)).isPresent();
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
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.age").value(29));
    }
}
