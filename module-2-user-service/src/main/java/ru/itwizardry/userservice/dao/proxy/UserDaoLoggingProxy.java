package ru.itwizardry.userservice.dao.proxy;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.entity.User;

import java.util.Objects;
import java.util.function.Supplier;

public class UserDaoLoggingProxy implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoLoggingProxy.class);

    private final UserDao target;

    public UserDaoLoggingProxy(UserDao target) {
        this.target = Objects.requireNonNull(target, "target UserDao must not be null");
    }

    @Override
    public User findById(Session session, Long id) {
        return timed("UserDao.findById id=" + id, () -> target.findById(session, id));
    }

    @Override
    public User findByEmail(Session session, String email) {
        return timed("UserDao.findByEmail email=" + maskEmail(email),
                () -> target.findByEmail(session, email));
    }

    @Override
    public void save(Session session, User user) {
        timedVoid("UserDao.save userId=" + safeId(user),
                () -> target.save(session, user));
    }

    @Override
    public void update(User managed, String name, String email, Integer age) {
        timedVoid("UserDao.update userId=" + safeId(managed) + " email=" + maskEmail(email),
                () -> target.update(managed, name, email, age));
    }

    @Override
    public void delete(User managed) {
        timedVoid("UserDao.delete userId=" + safeId(managed),
                () -> target.delete(managed));
    }

    private <T> T timed(String op, Supplier<T> action) {
        long start = System.nanoTime();
        try {
            return action.get();
        } catch (RuntimeException ex) {
            log.error("{} failed", op, ex);
            throw ex;
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{} took {} ms", op, tookMs);
        }
    }

    private void timedVoid(String op, Runnable action) {
        timed(op, () -> {
            action.run();
            return null;
        });
    }

    private static String safeId(User user) {
        if (user == null) return "null";
        return String.valueOf(user.getId());
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) return "null/blank";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
