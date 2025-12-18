package ru.itwizardry.userservice.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.itwizardry.userservice.entity.User;

public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            String host = env("POSTGRES_HOST", "localhost");
            String port = env("POSTGRES_PORT", "5432");
            String db   = requireEnv("POSTGRES_DB");
            String user = requireEnv("POSTGRES_USER");
            String pass = requireEnv("POSTGRES_PASSWORD");

            configuration.setProperty(
                    "hibernate.connection.driver_class",
                    "org.postgresql.Driver"
            );

            configuration.setProperty(
                    "hibernate.connection.url",
                    "jdbc:postgresql://" + host + ":" + port + "/" + db
            );

            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", pass);

            configuration.addAnnotatedClass(User.class);

            return configuration.buildSessionFactory();

        } catch (Exception e) {
            ExceptionInInitializerError err =
                    new ExceptionInInitializerError("Initial SessionFactory creation failed");
            err.initCause(e);
            throw err;
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required env variable: " + name);
        }
        return value;
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
