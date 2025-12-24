package ru.itwizardry.userservice.dao.proxy;

import ru.itwizardry.userservice.entity.User;

public class DefaultUserDaoLogFormatter implements UserDaoLogFormatter {

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
        return "UserDao.save email=" + maskEmail(user.getEmail());
    }

    @Override
    public String updateById(Long id, String name, String email, Integer age) {
        return "UserDao.updateById id=" + id + " email=" + maskEmail(email);
    }

    @Override
    public String delete(Long id) {
        return "UserDao.delete id=" + id;
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "null/blank";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
