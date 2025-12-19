package ru.itwizardry.userservice.dao;

import ru.itwizardry.userservice.entity.User;

public interface UserDao {
    User findById(Long id);
    User findByEmail(String email);

    void save(User user);
    void update(User user);
    void delete(User user);
}
