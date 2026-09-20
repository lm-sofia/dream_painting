package com.aiproject.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    /** 登录用：用户名或手机号二选一（username 字段语义扩展为 identifier） */
    default Optional<User> findByUsernameOrPhone(String identifier) {
        return findByUsername(identifier).or(() -> findByPhone(identifier));
    }

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
