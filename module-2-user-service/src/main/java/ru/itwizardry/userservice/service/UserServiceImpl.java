package ru.itwizardry.userservice.service;

import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.dao.UserDaoImpl;
import ru.itwizardry.userservice.entity.User;
import ru.itwizardry.userservice.util.HibernateUtil;

import java.util.Objects;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    public UserServiceImpl() {
        this(new UserDaoImpl());
    }

    @Override
    public User create(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            try {
                if (userDao.findByEmail(session, email) != null) {
                    throw new IllegalStateException("Email already exists: " + email);
                }

                User user = newUser(name, email, age);
                userDao.save(session, user);

                tx.commit();
                return user;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    @Override
    public User getById(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return userDao.findById(session, id);
        }
    }

    @Override
    public User getByEmail(String email) {
        validateEmail(email);

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return userDao.findByEmail(session, email);
        }
    }

    @Override
    public User update(Long id, String name, String email, Integer age) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        validateName(name);
        validateEmail(email);
        validateAge(age);

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            try {
                var existing = userDao.findById(session, id);
                if (existing == null) return null;

                var byEmail = userDao.findByEmail(session, email);
                if (byEmail != null && !Objects.equals(byEmail.getId(), id)) {
                    throw new IllegalStateException("Email already exists: " + email);
                }

                userDao.update(existing, name, email, age);

                tx.commit();
                return existing;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }


    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            try {
                var existing = userDao.findById(session, id);
                if (existing == null) {
                    return;
                }

                session.remove(existing);

                tx.commit();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }


    private static User newUser(String name, String email, Integer age) {
        return new User(name, email, age);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }

    private static void validateAge(Integer age) {
        if (age != null && age < 0) {
            throw new IllegalArgumentException("Age must be >= 0");
        }
    }
}
