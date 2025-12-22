package ru.itwizardry.userservice.dao;

import ru.itwizardry.userservice.entity.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);

    int updateById(Long id, String name, String email, Integer age);

    int delete(Long id);
}
