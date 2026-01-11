package ru.itwizardry.spring.module4userserviceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itwizardry.spring.module4userserviceapi.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}