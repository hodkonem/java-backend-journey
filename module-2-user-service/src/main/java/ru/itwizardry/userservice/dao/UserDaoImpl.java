package ru.itwizardry.userservice.dao;

import org.hibernate.Session;
import ru.itwizardry.userservice.entity.User;

import java.util.Objects;

public class UserDaoImpl implements UserDao {

    @Override
    public User findById(Session session, Long id) {
        requireSession(session);
        if (id == null) return null;

        return session.find(User.class, id);
    }

    @Override
    public User findByEmail(Session session, String email) {
        requireSession(session);
        if (email == null || email.isBlank()) return null;

        return session.createQuery(
                        "select u from User u where u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .uniqueResultOptional()
                .orElse(null);
    }

    @Override
    public void save(Session session, User user) {
        requireSession(session);
        if (user == null) return;

        session.persist(user);
    }

    @Override
    public void update(User managed, String name, String email, Integer age) {
        if (managed == null) return;

        managed.setName(name);
        managed.setEmail(email);
        managed.setAge(age);
    }

    @Override
    public void delete(User managed) {
        if (managed == null) return;
    }

    private void requireSession(Session session) {
        Objects.requireNonNull(session, "Session must not be null");
    }
}