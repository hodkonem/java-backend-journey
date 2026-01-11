package ru.itwizardry.spring.module4userserviceapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.exception.DuplicateEmailException;
import ru.itwizardry.spring.module4userserviceapi.exception.UserNotFoundException;
import ru.itwizardry.spring.module4userserviceapi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users — создание пользователя")
    void createUser_returnsCreatedUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest("Test", "test@example.com", 30);
        UserDto response = new UserDto(1L, "Test", "test@example.com", 30, LocalDateTime.now());

        Mockito.when(userService.createUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("POST /api/users — 400, если email невалиден")
    void createUser_invalidEmail_returnsBadRequest() throws Exception {
        var request = new UserCreateRequest("Test", "invalid-email", 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/users — 400, если имя пустое")
    void createUser_blankName_returnsBadRequest() throws Exception {
        var request = new UserCreateRequest(" ", "test@example.com", 30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/users/{id} — 405, метод не поддерживается")
    void postOnUserId_returnsMethodNotAllowed() throws Exception {
        Long id = 1L;

        mockMvc.perform(post("/api/users/{id}", id))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /api/users/{id} — возвращает пользователя")
    void getUserById_returnsUser() throws Exception {
        Long id = 1L;
        UserDto response = new UserDto(id, "Test", "test@example.com", 30, LocalDateTime.now());

        Mockito.when(userService.getUserById(id)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("GET /api/users — возвращает список пользователей")
    void getAllUsers_returnsList() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "Test1", "t1@example.com", 25, LocalDateTime.now()),
                new UserDto(2L, "Test2", "t2@example.com", 35, LocalDateTime.now())
        );

        Mockito.when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/users/search — успешный поиск по возрасту")
    void searchByAge_validAge_returnsUsers() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "Young", "y@example.com", 20, LocalDateTime.now())
        );

        Mockito.when(userService.findByAge(20)).thenReturn(users);

        mockMvc.perform(get("/api/users/search")
                        .param("age", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].age").value(20));
    }

    @Test
    @DisplayName("GET /api/users/search — 400, если возраст меньше 1")
    void searchByAge_invalidAge_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("age", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Constraint Violation"));
    }

    @Test
    @DisplayName("GET /api/users/{id} — 404, если пользователь не найден")
    void getUserById_userNotFound_returnsNotFound() throws Exception {
        Long id = 42L;

        Mockito.when(userService.getUserById(id))
                .thenThrow(new UserNotFoundException(id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"));
    }

    @Test
    @DisplayName("GET /api/users/{id} — 500, если произошла внутренняя ошибка")
    void getUserById_unexpectedError_returnsInternalServerError() throws Exception {
        Long id = 99L;

        Mockito.when(userService.getUserById(id))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Error"));
    }

    @Test
    @DisplayName("UPDATE /api/users{id} - обновление пользователя")
    void updateUser_returnsUpdatedUser() throws Exception {
        Long id = 1L;
        var updateRequest = new UserUpdateRequest("Updated", "updated@example.com", 40);
        var updateUser = new UserDto(id, "Updated", "updated@example.com", 40, LocalDateTime.now());

        Mockito.when(userService.updateUser(Mockito.eq(id), any())).thenReturn(updateUser);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} — 400, если email уже используется")
    void updateUser_duplicateEmail_returnsBadRequest() throws Exception {
        Long id = 1L;
        var request = new UserUpdateRequest("Name", "test@example.com", 30);

        Mockito.when(userService.updateUser(Mockito.eq(id), any()))
                .thenThrow(new DuplicateEmailException("test@example.com"));

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Duplicate Email"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} — 400, если имя пустое")
    void updateUser_blankName_returnsBadRequest() throws Exception {
        Long id = 1L;
        var request = new UserUpdateRequest(" ", "test@example.com", 30);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - удаление пользователя")
    void deleteUser_returnsNoContent() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(id);
    }
}
