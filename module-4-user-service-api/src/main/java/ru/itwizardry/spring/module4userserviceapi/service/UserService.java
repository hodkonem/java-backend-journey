package ru.itwizardry.spring.module4userserviceapi.service;

import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserDto createUser(UserCreateRequest request);

    UserDto updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserDto getUserById(Long id);

    List<UserDto> findByAge(int age);

    List<UserDto> getAllUsers();
}
