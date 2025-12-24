package ru.itwizardry.userservice.dao.proxy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.dao.UserDaoImpl;
import ru.itwizardry.userservice.entity.User;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class TransactionalUserDaoProxy implements UserDao {

    private final SessionFactory sessionFactory;

    public TransactionalUserDaoProxy(SessionFactory sessionFactory) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory");
    }

    @Override
    public Optional<User> findById(Long id) {
        return withSession(session -> new UserDaoImpl(session).findById(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return withSession(session -> new UserDaoImpl(session).findByEmail(email));
    }

    @Override
    public User save(User user) {
        return withTx(session -> new UserDaoImpl(session).save(user));
    }

    @Override
    public int updateById(Long id, String name, String email, Integer age) {
        return withTx(session -> new UserDaoImpl(session).updateById(id, name, email, age));
    }

    @Override
    public int delete(Long id) {
        return withTx(session -> new UserDaoImpl(session).delete(id));
    }

    private <T> T withSession(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            return work.apply(session);
        }
    }

    private <T> T withTx(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        }
    }
}
