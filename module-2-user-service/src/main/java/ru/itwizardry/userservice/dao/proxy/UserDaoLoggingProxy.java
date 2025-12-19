package ru.itwizardry.userservice.dao.proxy;

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
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public User findById(Long id) {
        return timed("UserDao.findById(id=" + id + ")", () -> target.findById(id));
    }

    @Override
    public User findByEmail(String email) {
        return timed("UserDao.findByEmail", () -> target.findByEmail(email));
    }

    @Override
    public void save(User user) {
        timedVoid("UserDao.save(" + userRef(user) + ")", () -> target.save(user));
    }

    @Override
    public void update(User user) {
        timedVoid("UserDao.update(" + userRef(user) + ")", () -> target.update(user));
    }

    @Override
    public void delete(User user) {
        timedVoid("UserDao.delete(" + userRef(user) + ")", () -> target.delete(user));
    }

    private static String userRef(User user) {
        if (user == null) return "null";
        return "User{id=" + user.getId() + "}";
    }

    private void timedVoid(String op, Runnable action) {
        long start = System.nanoTime();
        try {
            action.run();
        } catch (RuntimeException ex) {
            log.error("{} failed", op, ex);
            throw ex;
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{} took {} ms", op, tookMs);
        }
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
}
