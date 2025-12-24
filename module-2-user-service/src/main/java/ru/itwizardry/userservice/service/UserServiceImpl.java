package ru.itwizardry.userservice.service;

import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.entity.User;

import java.util.Objects;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    @Override
    public User create(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);

        User user = newUser(name, email, age);

        try {
            return userDao.save(user);
        } catch (RuntimeException ex) {
            if (isUniqueViolation(ex)) {
                throw new IllegalStateException("Email already exists: " + email);
            }
            throw ex;
        }
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        return userDao.findById(id).orElse(null);
    }

    @Override
    public User getByEmail(String email) {
        validateEmail(email);
        return userDao.findByEmail(email).orElse(null);
    }

    @Override
    public User update(Long id, String name, String email, Integer age) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        validateName(name);
        validateEmail(email);
        validateAge(age);


        try {
            int updated = userDao.updateById(id, name, email, age);
            if (updated == 0) return null;
            return userDao.findById(id).orElse(null);
        } catch (RuntimeException ex) {
            if (isUniqueViolation(ex)) {
                throw new IllegalStateException("Email already exists: " + email);
            }
            throw ex;
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        userDao.delete(id);
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
        if (age == null) {
            throw new IllegalArgumentException("Age cannot be null");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }

    private static boolean isUniqueViolation(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof org.hibernate.exception.ConstraintViolationException h) {
                return "23505".equals(h.getSQLState());
            }
            t = t.getCause();
        }
        return false;
    }
}
