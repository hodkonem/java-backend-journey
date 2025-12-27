package ru.itwizardry.userservice.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ru.itwizardry.userservice.entity.User;

import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserDaoImpl IT (Hibernate + PostgreSQL Testcontainers)")
class UserDaoImplIT {

    private static final long MISSING_USER_ID = 999L;

    private static final String EMAIL_MARK = "mark@test.com";
    private static final String EMAIL_ABSENT = "absent@test.com";
    private static final String EMAIL_DUP = "dup@test.com";

    private static final String EMAIL_A = "a@test.com";
    private static final String EMAIL_B = "b@test.com";

    private static final String EMAIL_OLD = "old@test.com";
    private static final String EMAIL_NEW = "new@test.com";

    private static final String NAME_MARK = "Mark";
    private static final String NAME_OLD = "Old";
    private static final String NAME_NEW = "New";
    private static final String NAME_USER1 = "User1";
    private static final String NAME_USER2 = "User2";

    private static final int AGE_9 = 9;
    private static final int AGE_10 = 10;
    private static final int AGE_11 = 11;
    private static final int AGE_20 = 20;
    private static final int AGE_21 = 21;
    private static final int AGE_22 = 22;

    private static final Set<String> TABLES_TO_SKIP = Set.of(
            "databasechangelog",
            "databasechangeloglock",
            "flyway_schema_history"
    );

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:15")
            .withDatabaseName("user_service_test")
            .withUsername("test")
            .withPassword("test");

    private SessionFactory sessionFactory;

    @BeforeAll
    void setUp() {
        sessionFactory = buildSessionFactory();
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @BeforeEach
    void cleanDb() {
        inTxVoid(this::truncatePublicTablesExceptSkipped);
    }

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("persists user and assigns id")
        void save_persistsUser_andAssignsId() {
            Long id = inTx(session -> {
                User user = user(NAME_MARK, EMAIL_MARK, AGE_9);
                dao(session).save(user);
                session.flush();

                assertNotNull(user.getId(), "After save+flush user.id must be assigned");
                return user.getId();
            });

            User fromDb = inTx(session -> session.find(User.class, id));
            assertNotNull(fromDb, "User must exist in DB after save");
            assertUser(fromDb, NAME_MARK, EMAIL_MARK, AGE_9);
        }

        @Test
        @DisplayName("throws unique violation (23505) on duplicate email")
        void save_duplicateEmail_throwsUniqueViolation_23505() {
            inTxVoid(session -> {
                dao(session).save(user(NAME_USER1, EMAIL_DUP, AGE_9));
                session.flush();
            });

            RuntimeException ex = assertThrows(RuntimeException.class, () -> inTxVoid(session -> {
                dao(session).save(user(NAME_USER2, EMAIL_DUP, AGE_10));
                session.flush();
            }), "Duplicate email must fail with unique constraint");

            assertPostgresUniqueViolation(ex);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("returns user for existing id")
        void findById_existing_returnsUser() {
            Long id = inTx(session -> {
                User user = user(NAME_MARK, EMAIL_MARK, AGE_9);
                dao(session).save(user);
                session.flush();
                return user.getId();
            });

            Optional<User> foundOpt = inTx(session -> dao(session).findById(id));
            User found = requirePresent(foundOpt, "findById must return user for existing id=" + id);

            assertEquals(id, found.getId(), "Returned user must have expected id");
            assertUser(found, NAME_MARK, EMAIL_MARK, AGE_9);
        }

        @Test
        @DisplayName("returns empty for missing id")
        void findById_missing_returnsEmptyOptional() {
            Optional<User> found = inTx(session -> dao(session).findById(MISSING_USER_ID));
            assertTrue(found.isEmpty(), "findById must return empty for missing id=" + MISSING_USER_ID);
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTests {

        @Test
        @DisplayName("returns user for existing email")
        void findByEmail_existing_returnsUser() {
            inTxVoid(session -> {
                dao(session).save(user(NAME_MARK, EMAIL_MARK, AGE_9));
                session.flush();
            });

            Optional<User> foundOpt = inTx(session -> dao(session).findByEmail(EMAIL_MARK));
            User found = requirePresent(foundOpt, "findByEmail must return user for existing email=" + EMAIL_MARK);

            assertUser(found, NAME_MARK, EMAIL_MARK, AGE_9);
        }

        @Test
        @DisplayName("returns empty for missing email")
        void findByEmail_missing_returnsEmpty() {
            Optional<User> found = inTx(session -> dao(session).findByEmail(EMAIL_ABSENT));
            assertTrue(found.isEmpty(), "findByEmail must return empty for missing email=" + EMAIL_ABSENT);
        }
    }

    @Nested
    @DisplayName("updateById()")
    class UpdateByIdTests {

        @Test
        @DisplayName("returns 1 and updates fields for existing id")
        void updateById_existing_returns1_andUpdatesFields() {
            Long id = inTx(session -> {
                User user = user(NAME_OLD, EMAIL_OLD, AGE_10);
                dao(session).save(user);
                session.flush();
                return user.getId();
            });

            int updated = inTx(session -> dao(session).updateById(id, NAME_NEW, EMAIL_NEW, AGE_11));
            assertEquals(1, updated, "updateById must return 1 for existing row");

            User fromDb = inTx(session -> session.find(User.class, id));
            assertNotNull(fromDb, "User must exist after update");
            assertUser(fromDb, NAME_NEW, EMAIL_NEW, AGE_11);
        }

        @Test
        @DisplayName("throws unique violation (23505) when updating email to existing one")
        void updateById_duplicateEmail_throwsUniqueViolation_23505() {
            Long id1 = inTx(session -> {
                User u = user(NAME_USER1, EMAIL_A, AGE_20);
                dao(session).save(u);
                session.flush();
                return u.getId();
            });

            inTxVoid(session -> {
                dao(session).save(user(NAME_USER2, EMAIL_B, AGE_21));
                session.flush();
            });

            RuntimeException ex = assertThrows(RuntimeException.class, () -> inTxVoid(session -> {
                dao(session).updateById(id1, NAME_USER1, EMAIL_B, AGE_22);
            }), "Updating email to existing one must fail with unique constraint");

            assertPostgresUniqueViolation(ex);
        }

        @Test
        @DisplayName("returns 0 for missing id")
        void updateById_missing_returns0() {
            int updated = inTx(session -> dao(session).updateById(MISSING_USER_ID, NAME_NEW, EMAIL_NEW, AGE_11));
            assertEquals(0, updated, "updateById must return 0 for missing id=" + MISSING_USER_ID);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("returns 1 and removes row for existing id")
        void delete_existing_returns1_andRemovesRow() {
            Long id = inTx(session -> {
                User user = user(NAME_MARK, EMAIL_MARK, AGE_9);
                dao(session).save(user);
                session.flush();
                return user.getId();
            });

            int deleted = inTx(session -> dao(session).delete(id));
            assertEquals(1, deleted, "delete must return 1 for existing row");

            Optional<User> after = inTx(session -> dao(session).findById(id));
            assertTrue(after.isEmpty(), "Deleted user must not be found by id=" + id);
        }

        @Test
        @DisplayName("returns 0 for missing id")
        void delete_missing_returns0() {
            int deleted = inTx(session -> dao(session).delete(MISSING_USER_ID));
            assertEquals(0, deleted, "delete must return 0 for missing id=" + MISSING_USER_ID);
        }
    }

    private static UserDaoImpl dao(Session session) {
        return new UserDaoImpl(session);
    }

    private static User user(String name, String email, int age) {
        return new User(name, email, age);
    }

    private static User requirePresent(Optional<User> opt, String message) {
        assertTrue(opt.isPresent(), message);
        return opt.get();
    }

    private static void assertUser(User u, String name, String email, int age) {
        assertAll("user fields",
                () -> assertEquals(name, u.getName(), "name mismatch"),
                () -> assertEquals(email, u.getEmail(), "email mismatch"),
                () -> assertEquals(Integer.valueOf(age), u.getAge(), "age mismatch"),
                () -> assertNotNull(u.getCreatedAt(), "createdAt must be set")
        );
    }

    private SessionFactory buildSessionFactory() {
        StandardServiceRegistry registry = null;
        try {
            Map<String, Object> settings = buildHibernateSettings();

            registry = new StandardServiceRegistryBuilder()
                    .applySettings(settings)
                    .build();

            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .buildMetadata();

            return metadata.buildSessionFactory();
        } catch (Exception e) {
            if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    private static Map<String, Object> buildHibernateSettings() {
        Map<String, Object> settings = new HashMap<>();

        settings.put(JdbcSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
        settings.put(JdbcSettings.JAKARTA_JDBC_URL, POSTGRES.getJdbcUrl());
        settings.put(JdbcSettings.JAKARTA_JDBC_USER, POSTGRES.getUsername());
        settings.put(JdbcSettings.JAKARTA_JDBC_PASSWORD, POSTGRES.getPassword());

        settings.put(AvailableSettings.HBM2DDL_AUTO, "create");
        settings.put(AvailableSettings.GENERATE_STATISTICS, "false");

        return settings;
    }

    private void truncatePublicTablesExceptSkipped(Session session) {
        session.doWork(connection -> {
            List<String> tables = new ArrayList<>();

            try (var ps = connection.prepareStatement(
                    "select tablename from pg_tables where schemaname = 'public'"
            );
                 var rs = ps.executeQuery()) {

                while (rs.next()) {
                    String table = rs.getString(1);
                    if (!TABLES_TO_SKIP.contains(table.toLowerCase(Locale.ROOT))) {
                        tables.add(table);
                    }
                }
            }

            if (tables.isEmpty()) return;

            String joined = String.join(", ",
                    tables.stream().map(t -> "public.\"" + t + "\"").toList()
            );

            String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";

            try (Statement st = connection.createStatement()) {
                st.execute(sql);
            }
        });
    }

    private void inTxVoid(Consumer<Session> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                work.accept(session);
                tx.commit();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    private <T> T inTx(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    private static void assertPostgresUniqueViolation(Throwable ex) {
        var hCve = findCause(ex, org.hibernate.exception.ConstraintViolationException.class);
        assertNotNull(hCve, "Expected Hibernate ConstraintViolationException in cause chain");

        var sqlEx = hCve.getSQLException();
        assertNotNull(sqlEx, "Expected SQLException inside Hibernate ConstraintViolationException");

        assertEquals("23505", sqlEx.getSQLState(),
                "Expected Postgres unique violation SQLState=23505, but was: " + sqlEx.getSQLState());
    }

    private static <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
        Throwable cur = ex;
        while (cur != null) {
            if (type.isInstance(cur)) return type.cast(cur);
            cur = cur.getCause();
        }
        return null;
    }
}
