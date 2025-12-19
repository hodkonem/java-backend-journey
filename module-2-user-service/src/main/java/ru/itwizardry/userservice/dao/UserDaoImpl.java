package ru.itwizardry.userservice.dao;

import org.hibernate.Session;
import ru.itwizardry.userservice.entity.User;
import ru.itwizardry.userservice.util.HibernateUtil;

import java.util.function.Function;

public class UserDaoImpl implements UserDao {

    @Override
    public User findById(Long id) {
        if (id == null) return null;

        return inSession(session -> session.find(User.class, id));
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) return null;

        return inSession(session ->
                session.createQuery(
                                "select u from User u where u.email = :email",
                                User.class
                        )
                        .setParameter("email", email)
                        .uniqueResultOptional()
                        .orElse(null)
        );
    }

    @Override
    public void save(User user) {
        if (user == null) return;

        inTx(session -> {
            session.persist(user);
            return null;
        });
    }

    @Override
    public void update(User user) {
        if (user == null || user.getId() == null) return;

        inTx(session -> {
            var managed = session.find(User.class, user.getId());
            if (managed == null) {
                throw new IllegalArgumentException("User not found: id=" + user.getId());
            }

            managed.setName(user.getName());
            managed.setEmail(user.getEmail());
            managed.setAge(user.getAge());

            return null;
        });
    }

    @Override
    public void delete(User user) {
        if (user == null || user.getId() == null) return;

        inTx(session -> {
            var managed = session.find(User.class, user.getId());
            if (managed != null) {
                session.remove(managed);
            }
            return null;
        });
    }

    private <T> T inSession(Function<Session, T> work) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return work.apply(session);
        }
    }

    private <T> T inTx(Function<Session, T> work) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }
}
