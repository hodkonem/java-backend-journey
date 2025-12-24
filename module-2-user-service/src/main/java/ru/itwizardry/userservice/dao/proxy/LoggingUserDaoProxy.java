package ru.itwizardry.userservice.dao.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.entity.User;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LoggingUserDaoProxy implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(LoggingUserDaoProxy.class);

    private final UserDao target;
    private final UserDaoLogFormatter formatter;

    public LoggingUserDaoProxy(UserDao target, UserDaoLogFormatter formatter) {
        this.target = Objects.requireNonNull(target, "target UserDao must not be null");
        this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
    }

    @Override
    public Optional<User> findById(Long id) {
        return timed(formatter.findById(id), () -> target.findById(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return timed(formatter.findByEmail(email), () -> target.findByEmail(email));
    }

    @Override
    public User save(User user) {
        return timed(formatter.save(user), () -> target.save(user));
    }

    @Override
    public int updateById(Long id, String name, String email, Integer age) {
        String op = formatter.updateById(id, name, email, age);
        return timed(op, () -> {
            int rows = target.updateById(id, name, email, age);
            log.info("{} rowsAffected={}", op, rows);
            return rows;
        });
    }

    @Override
    public int delete(Long id) {
        String op = formatter.delete(id);
        return timed(op, () -> {
            int rows = target.delete(id);
            log.info("{} rowsAffected={}", op, rows);
            return rows;
        });
    }

    private <T> T timed(String op, Supplier<T> action) {
        long start = System.nanoTime();
        try {
            T result = action.get();
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{} took {} ms", op, tookMs);
            return result;
        } catch (RuntimeException ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.error("{} failed ({} ms)", op, tookMs, ex);
            throw ex;
        }
    }
}
