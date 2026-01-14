package ru.itwizardry.spring.module4userserviceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.itwizardry.spring.module4userserviceapi.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from User u where u.id = :id")
    int deleteByIdReturningCount(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update User u
               set u.name = :name,
                   u.email = :email,
                   u.age = :age
             where u.id = :id
            """)
    int updateByIdReturningCount(Long id, String name, String email, Integer age);
}
