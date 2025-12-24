package ru.itwizardry.userservice.dao.proxy;

import ru.itwizardry.userservice.entity.User;

public interface UserDaoLogFormatter {
    String findById(Long id);
    String findByEmail(String email);
    String save(User user);
    String updateById(Long id, String name, String email, Integer age);
    String delete(Long id);
}
