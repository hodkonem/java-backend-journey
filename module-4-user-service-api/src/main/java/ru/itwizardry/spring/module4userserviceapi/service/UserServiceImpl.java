package ru.itwizardry.spring.module4userserviceapi.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.exception.DuplicateEmailException;
import ru.itwizardry.spring.module4userserviceapi.exception.UserNotFoundException;
import ru.itwizardry.spring.module4userserviceapi.mapper.UserMapper;
import ru.itwizardry.spring.module4userserviceapi.model.User;
import ru.itwizardry.spring.module4userserviceapi.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        LOG.info("User created: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!request.email().equals(user.getEmail())) {
            userRepository.findByEmail(request.email())
                    .filter(found -> !found.getId().equals(id))
                    .ifPresent(found -> {
                        throw new DuplicateEmailException(request.email());
                    });
        }

        userMapper.updateEntity(user, request);

        User savedUser = userRepository.save(user);
        LOG.info("User updated: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
        LOG.info("User deleted: {}", id);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findByAge(int age) {
        return userRepository.findAll().stream()
                .filter(user -> user.getAge() == age)
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }
}
