package ru.itwizardry.spring.module4userserviceapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itwizardry.spring.module4userserviceapi.domain.event.UserCreatedEvent;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.exception.DuplicateEmailException;
import ru.itwizardry.spring.module4userserviceapi.exception.UserNotFoundException;
import ru.itwizardry.spring.module4userserviceapi.mapper.UserMapper;
import ru.itwizardry.spring.module4userserviceapi.model.User;
import ru.itwizardry.spring.module4userserviceapi.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getAge()
        ));

        log.info("User created: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {

        userRepository.findByEmail(request.email())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new DuplicateEmailException(request.email());
                });

        int updated = userRepository.updateByIdReturningCount(id, request.name(), request.email(), request.age());
        if (updated == 0) throw new UserNotFoundException(id);

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        log.info("User updated: {}", id);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        int deleted = userRepository.deleteByIdReturningCount(id);
        if (deleted == 0) throw new UserNotFoundException(id);
        log.info("User deleted: {}", id);
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
