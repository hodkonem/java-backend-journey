package ru.itwizardry.userservice.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.itwizardry.userservice.entity.User;

import java.util.HashMap;
import java.util.Map;

public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        StandardServiceRegistry registry = null;

        try {
            String host = env("POSTGRES_HOST", "localhost");
            String port = env("POSTGRES_PORT", "5432");
            String db   = env("POSTGRES_DB", "user_service");
            String user = requireEnv("POSTGRES_USER");
            String pass = requireEnv("POSTGRES_PASSWORD");

            Map<String, Object> settings = new HashMap<>();
            settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            settings.put("hibernate.connection.url",
                    "jdbc:postgresql://" + host + ":" + port + "/" + db);
            settings.put("hibernate.connection.username", user);
            settings.put("hibernate.connection.password", pass);

            registry = new StandardServiceRegistryBuilder()
                    .applySettings(settings)
                    .build();

            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .buildMetadata();

            return metadata.buildSessionFactory();

        } catch (Exception e) {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
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
        SESSION_FACTORY.close();
    }
}
