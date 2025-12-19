package ru.itwizardry.userservice.service;

import ru.itwizardry.userservice.entity.User;

public interface UserService {
    User create(String name, String email, Integer age);
    User getById(Long id);
    User getByEmail(String email);
    User update(Long id, String name, String email, Integer age);
    void delete(Long id);
}