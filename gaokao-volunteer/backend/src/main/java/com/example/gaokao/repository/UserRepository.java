
package com.example.gaokao.repository;

import com.example.gaokao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByIdCard(String idCard);

    boolean existsByPhone(String phone);

    boolean existsByIdCard(String idCard);

    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.enabled = true")
    Optional<User> findActiveUserByPhone(@Param("phone") String phone);
}
