package ru.itwizardry.spring.module4userserviceapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.exception.DuplicateEmailException;
import ru.itwizardry.spring.module4userserviceapi.exception.UserNotFoundException;
import ru.itwizardry.spring.module4userserviceapi.mapper.UserMapper;
import ru.itwizardry.spring.module4userserviceapi.model.User;
import ru.itwizardry.spring.module4userserviceapi.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl (unit)")
class UserServiceImplTest {

    private static final String EMAIL = "test@example.com";
    private static final String NAME = "Mikhail";
    private static final int AGE = 41;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("createUser(): duplicate email -> throws DuplicateEmailException")
    void createUser_duplicateEmail_throwsDuplicateEmailException() {
        UserCreateRequest request = new UserCreateRequest(NAME, EMAIL, AGE);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        var ex = assertThrows(DuplicateEmailException.class, () -> userService.createUser(request));

        assertTrue(ex.getMessage().contains(EMAIL));
        verify(userRepository).existsByEmail(EMAIL);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("createUser(): valid request -> returns saved UserDto")
    void createUser_validRequest_returnsUserDto() {
        UserCreateRequest request = new UserCreateRequest(NAME, EMAIL, AGE);

        User userToSave = User.builder()
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        User savedUser = User.builder()
                .id(1L)
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .createdAt(createdAt)
                .build();

        UserDto expectedDto = new UserDto(1L, NAME, EMAIL, AGE, createdAt);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(userToSave);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto actualDto = userService.createUser(request);

        assertEquals(expectedDto, actualDto);

        verify(userRepository).existsByEmail(EMAIL);
        verify(userMapper).toEntity(request);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("updteUser(): valid request -> updates user and returns UserDto")
    void updateUser_validRequest_returnsUserDto() {
        Long userId = 1L;
        var request = new UserUpdateRequest(NAME, EMAIL, AGE);

        var existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        var updatedUser = User.builder()
                .id(userId)
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .createdAt(existingUser.getCreatedAt())
                .build();

        var expectedDto = new UserDto(userId, NAME, EMAIL, AGE, existingUser.getCreatedAt());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            User target = invocation.getArgument(0);
            UserUpdateRequest req = invocation.getArgument(1);
            target.setName(req.name());
            target.setEmail(req.email());
            target.setAge(req.age());
            return null;
        }).when(userMapper).updateEntity(any(User.class), any(UserUpdateRequest.class));

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        var result = userService.updateUser(userId, request);

        assertEquals(expectedDto, result);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(EMAIL);
        verify(userMapper).updateEntity(existingUser, request);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("updateUser(): non-existing user -> throws UserNotFoundException")
    void updateUser_userNotFound_throwsUserNotFoundException() {
        Long userId = 42L;
        var request = new UserUpdateRequest(NAME, EMAIL, AGE);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, request));

        assertEquals("User with id " + userId + " not found", ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("updateUser(): email taken by another -> throws DuplicateEmailException")
    void updateUser_emailTaken_throwsDuplicateEmailException() {
        Long userId = 1L;
        var request = new UserUpdateRequest(NAME, EMAIL, AGE);

        var existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        var otherUser = User.builder()
                .id(999L)
                .name("Other")
                .email(EMAIL)
                .age(20)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherUser));

        var ex = assertThrows(DuplicateEmailException.class, () -> userService.updateUser(userId, request));

        assertTrue(ex.getMessage().contains(EMAIL));
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(EMAIL);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("deleteUpdate(): existing id -> deletes user")
    void deleteUser_existingUser_deletesUser() {
        Long userId = 1L;

        var existingUser = User.builder()
                .id(userId)
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(existingUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("deleteUser(): non-existing id -> throws UserNotFoundException")
    void deleteUser_userNotFound_throwsUserNotFoundException() {
        Long userId = 42L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

        assertEquals("User with id " + userId + " not found", ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getUserbyId(): existing id -> returns UserDto")
    void getUserById_existingId_returnsUserDto() {
        Long userId = 1L;
        LocalDateTime createdAt = LocalDateTime.now();

        User user = User.builder()
                .id(userId)
                .name(NAME)
                .email(EMAIL)
                .age(AGE)
                .createdAt(createdAt)
                .build();

        UserDto expectedDto = new UserDto(userId, NAME, EMAIL, AGE, createdAt);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto actualDto = userService.getUserById(userId);

        assertEquals(expectedDto, actualDto);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getUserById_nonExistingId_throwsUserNotFoundException")
    void getUserById_nonExistingId_throwsUserNotFoundException() {
        Long userId = 42L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        assertEquals("User with id " + userId + " not found", ex.getMessage());

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getAllUsers(): users found -> returns list of UserDto")
    void getAllUsers_usersFound_returnsList() {
        var createdAt = LocalDateTime.now();

        var user1 = User.builder().id(1L).name("User1").email("user1@test1.com").age(20).createdAt(createdAt).build();
        var user2 = User.builder().id(2L).name("User2").email("user2@example.ru").age(25).createdAt(createdAt).build();

        var dto1 = new UserDto(1L, "User1", "user1@test1.com", 20, createdAt);
        var dto2 = new UserDto(2L, "User2", "user2@example.ru", 25, createdAt);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        var result = userService.getAllUsers();

        assertEquals(List.of(dto1, dto2), result);
        verify(userRepository).findAll();
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getAllUsers(): no users -> returns empty list")
    void getAllUsers_noUsers_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        var result = userService.getAllUsers();

        assertEquals(List.of(), result);
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository, userMapper);
    }
}
