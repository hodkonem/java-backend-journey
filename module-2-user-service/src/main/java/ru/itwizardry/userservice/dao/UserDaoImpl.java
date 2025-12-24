package ru.itwizardry.userservice.dao;

import org.hibernate.Session;
import ru.itwizardry.userservice.entity.User;

import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private final Session session;

    public UserDaoImpl(Session session) {
        this.session = session;
    }


    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(session.find(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return session.createQuery(
                        "select u from User u where u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public User save(User user) {
        session.persist(user);
        return user;
    }

    @Override
    public int updateById(Long id, String name, String email, Integer age) {
        return session.createMutationQuery(
                        "update User u " +
                                "set u.name = :name, u.email = :email, u.age = :age " +
                                "where u.id = :id"
                )
                .setParameter("name", name)
                .setParameter("email", email)
                .setParameter("age", age)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public int delete(Long id) {
        return session.createMutationQuery(
                        "delete from User u where u.id = :id"
                )
                .setParameter("id", id)
                .executeUpdate();
    }
}