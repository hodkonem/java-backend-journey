package ru.itwizardry.userservice.dao;

import org.hibernate.Session;
import ru.itwizardry.userservice.entity.User;

public interface UserDao {
    User findById(Session session, Long id);

    User findByEmail(Session session, String email);

    void save(Session session, User user);

    void update(User managed, String name, String email, Integer age);

    void delete(User managed);
}
