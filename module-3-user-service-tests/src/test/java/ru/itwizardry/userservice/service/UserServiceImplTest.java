package ru.itwizardry.userservice.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itwizardry.userservice.dao.UserDao;
import ru.itwizardry.userservice.entity.User;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl (unit)")
class UserServiceImplTest {

    private static final String VALID_NAME = "Mark";
    private static final String VALID_EMAIL = "abc@test.com";
    private static final int VALID_AGE = 9;
    private static final long VALID_ID = 1L;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @ParameterizedTest(name = "create(): invalid name \"{0}\" -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void create_invalidName_throwsIllegalArgumentException(String name) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.create(name, VALID_EMAIL, VALID_AGE));
        assertEquals("Name cannot be null or blank", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @ParameterizedTest(name = "create(): invalid email \"{0}\" -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void create_invalidEmail_throwsIllegalArgumentException(String email) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.create(VALID_NAME, email, VALID_AGE));
        assertEquals("Email cannot be null or blank", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void create_ageNull_throwsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.create(VALID_NAME, VALID_EMAIL, null));
        assertEquals("Age cannot be null", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @ParameterizedTest(name = "create(): age {0} out of range -> IllegalArgumentException")
    @ValueSource(ints = {-1, 151})
    void create_ageOutOfRange_throwsIllegalArgumentException(int age) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.create(VALID_NAME, VALID_EMAIL, age));
        assertEquals("Age must be between 0 and 150", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("create(): valid input -> returns saved user")
    void create_validInput_returnsSavedUser() {
        User saved = new User(VALID_NAME, VALID_EMAIL, VALID_AGE);
        when(userDao.save(any(User.class))).thenReturn(saved);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        User result = userServiceImpl.create(VALID_NAME, VALID_EMAIL, VALID_AGE);

        assertSame(saved, result);

        verify(userDao).save(captor.capture());
        User passed = captor.getValue();

        assertEquals(VALID_NAME, passed.getName());
        assertEquals(VALID_EMAIL, passed.getEmail());
        assertEquals(Integer.valueOf(VALID_AGE), passed.getAge());

        verifyNoMoreInteractions(userDao);
    }

    @Test
    void create_validInput_returnsUserFromDao() {
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userServiceImpl.create(VALID_NAME, VALID_EMAIL, VALID_AGE);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        User captured = captor.getValue();

        assertSame(captured, result);
        assertEquals(VALID_NAME, result.getName());
        assertEquals(VALID_EMAIL, result.getEmail());
        assertEquals(Integer.valueOf(VALID_AGE), result.getAge());

        verifyNoMoreInteractions(userDao);
    }

    @Test
    @DisplayName("create(): duplicate email -> IllegalStateException")
    void create_duplicateEmail_throwsIllegalStateException() {
        SQLException sql = new SQLException("duplicate key", "23505");
        ConstraintViolationException cve =
                new ConstraintViolationException("constraint", sql, "uk_users_email");
        RuntimeException wrapped = new RuntimeException("wrapper", cve);

        when(userDao.save(any(User.class))).thenThrow(wrapped);

        var ex = assertThrows(IllegalStateException.class,
                () -> userServiceImpl.create(VALID_NAME, VALID_EMAIL, VALID_AGE));

        assertEquals("Email already exists: " + VALID_EMAIL, ex.getMessage());

        verify(userDao).save(any(User.class));
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void create_daoThrowsRuntimeException_rethrows() {
        var cause = new SQLException("db error", "08006");
        RuntimeException boom = new RuntimeException("boom", cause);

        when(userDao.save(any(User.class))).thenThrow(boom);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userServiceImpl.create(VALID_NAME, VALID_EMAIL, VALID_AGE));

        assertSame(boom, ex);
        verify(userDao).save(any(User.class));
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void update_idNull_throwsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(null, VALID_NAME, VALID_EMAIL, VALID_AGE));

        assertEquals("Id cannot be null", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("update(): user not found -> returns null")
    void update_userNotFound_returnsNull() {
        when(userDao.updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE)).thenReturn(0);

        User result = userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);

        assertNull(result);
        verify(userDao).updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    @DisplayName("update(): updated -> returns user from DAO")
    void update_updated_returnsUserFromDao() {
        User fromDb = new User(VALID_NAME, VALID_EMAIL, VALID_AGE);

        when(userDao.updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE)).thenReturn(1);
        when(userDao.findById(VALID_ID)).thenReturn(Optional.of(fromDb));

        User result = userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);

        assertSame(fromDb, result);
        verify(userDao).updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);
        verify(userDao).findById(VALID_ID);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void update_updatedButUserMissing_returnsNull() {
        when(userDao.updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE)).thenReturn(1);
        when(userDao.findById(VALID_ID)).thenReturn(Optional.empty());

        User result = userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);

        assertNull(result);
        verify(userDao).updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);
        verify(userDao).findById(VALID_ID);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    @DisplayName("update(): duplicate email -> IllegalStateException")
    void update_duplicateEmail_throwsIllegalStateException() {
        SQLException sql = new SQLException("duplicate key", "23505");
        ConstraintViolationException cve =
                new ConstraintViolationException("constraint", sql, "uk_users_email");
        RuntimeException wrapped = new RuntimeException("wrapper", cve);

        when(userDao.updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE)).thenThrow(wrapped);

        var ex = assertThrows(IllegalStateException.class,
                () -> userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE));

        assertEquals("Email already exists: " + VALID_EMAIL, ex.getMessage());
        verify(userDao).updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void update_daoThrowsRuntimeException_rethrows() {
        var cause = new SQLException("db error", "08006");
        RuntimeException boom = new RuntimeException("boom", cause);

        when(userDao.updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE)).thenThrow(boom);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE));

        assertSame(boom, ex);
        verify(userDao).updateById(VALID_ID, VALID_NAME, VALID_EMAIL, VALID_AGE);
        verifyNoMoreInteractions(userDao);
    }

    @ParameterizedTest(name = "update(): invalid name \"{0}\" -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void update_invalidName_throwsIllegalArgumentException(String name) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(VALID_ID, name, VALID_EMAIL, VALID_AGE));
        assertEquals("Name cannot be null or blank", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @ParameterizedTest(name = "update(): invalid email \"{0}\" -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void update_invalidEmail_throwsIllegalArgumentException(String email) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(VALID_ID, VALID_NAME, email, VALID_AGE));
        assertEquals("Email cannot be null or blank", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void update_ageNull_throwsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, null));
        assertEquals("Age cannot be null", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @ParameterizedTest(name = "update(): age {0} out of range -> IllegalArgumentException")
    @ValueSource(ints = {-1, 151})
    void update_ageOutOfRange_throwsIllegalArgumentException(int age) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(VALID_ID, VALID_NAME, VALID_EMAIL, age));
        assertEquals("Age must be between 0 and 150", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void getById_idNull_throwsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.getById(null));

        assertEquals("Id cannot be null", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("getById(): not found -> returns null")
    void getById_notFound_returnsNull() {
        when(userDao.findById(VALID_ID)).thenReturn(Optional.empty());

        User result = userServiceImpl.getById(VALID_ID);

        assertNull(result);
        verify(userDao).findById(VALID_ID);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void getById_found_returnsUser() {
        User fromDb = new User(VALID_NAME, VALID_EMAIL, VALID_AGE);
        when(userDao.findById(VALID_ID)).thenReturn(Optional.of(fromDb));

        User result = userServiceImpl.getById(VALID_ID);

        assertSame(fromDb, result);
        verify(userDao).findById(VALID_ID);
        verifyNoMoreInteractions(userDao);
    }

    @ParameterizedTest(name = "getByEmail(): invalid email \"{0}\" -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void getByEmail_invalidEmail_throwsIllegalArgumentException(String email) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.getByEmail(email));

        assertEquals("Email cannot be null or blank", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("getByEmail(): not found -> returns null")
    void getByEmail_notFound_returnsNull() {
        when(userDao.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        User result = userServiceImpl.getByEmail(VALID_EMAIL);

        assertNull(result);
        verify(userDao).findByEmail(VALID_EMAIL);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void getByEmail_found_returnsUser() {
        User fromDb = new User(VALID_NAME, VALID_EMAIL, VALID_AGE);
        when(userDao.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(fromDb));

        User result = userServiceImpl.getByEmail(VALID_EMAIL);

        assertSame(fromDb, result);
        verify(userDao).findByEmail(VALID_EMAIL);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void delete_idNull_throwsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.delete(null));

        assertEquals("Id cannot be null", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("delete(): valid id -> calls DAO delete")
    void delete_validId_callsDaoDelete() {
        when(userDao.delete(VALID_ID)).thenReturn(1);

        userServiceImpl.delete(VALID_ID);

        verify(userDao).delete(VALID_ID);
        verifyNoMoreInteractions(userDao);
    }
}
