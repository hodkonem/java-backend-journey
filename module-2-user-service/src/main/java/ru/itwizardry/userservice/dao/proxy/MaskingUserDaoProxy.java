package ru.itwizardry.userservice.dao.proxy;

import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.entity.User;

import java.util.Optional;

public class MaskingUserDaoProxy implements UserDao {

    private final UserDao delegate;

    public MaskingUserDaoProxy(UserDao target) {
        this.delegate = new LoggingUserDaoProxy(target, new MaskingFormatter());
    }

    @Override
    public Optional<User> findById(Long id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return delegate.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return delegate.save(user);
    }

    @Override
    public int updateById(Long id, String name, String email, Integer age) {
        return delegate.updateById(id, name, email, age);
    }

    @Override
    public int delete(Long id) {
        return delegate.delete(id);
    }

    private static final class MaskingFormatter implements UserDaoLogFormatter {

        @Override
        public String findById(Long id) {
            return "UserDao.findById id=" + id;
        }

        @Override
        public String findByEmail(String email) {
            return "UserDao.findByEmail email=" + maskEmail(email);
        }

        @Override
        public String save(User user) {
            return "UserDao.save email=" + maskEmail(userEmail(user));
        }

        @Override
        public String updateById(Long id, String name, String email, Integer age) {
            return "UserDao.updateById id=" + id + " email=" + maskEmail(email);
        }

        @Override
        public String delete(Long id) {
            return "UserDao.delete id=" + id;
        }

        private static String userEmail(User user) {
            return user == null ? null : user.getEmail();
        }

        private static String maskEmail(String email) {
            if (email == null || email.isBlank()) return "null/blank";
            int at = email.indexOf('@');
            if (at <= 1) return "***";
            return email.charAt(0) + "***" + email.substring(at);
        }
    }
}
