package com.aiproject.security;

import com.aiproject.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        // 密钥长度 >= 32 字节
        provider = new JwtTokenProvider(
                "unit-test-secret-key-0123456789abcdefghijklmnop", 86400000L);
    }

    @Test
    void generateAndParseToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("USER");
        UserPrincipal principal = new UserPrincipal(user);

        String token = provider.generateToken(principal);
        assertThat(token).isNotBlank();
        assertThat(provider.extractUsername(token)).isEqualTo("alice");
        assertThat(provider.isValid(token, principal)).isTrue();
    }

    @Test
    void expiredTokenIsInvalid() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("USER");
        UserPrincipal principal = new UserPrincipal(user);

        JwtTokenProvider shortLived = new JwtTokenProvider(
                "unit-test-secret-key-0123456789abcdefghijklmnop", -1000L);
        String token = shortLived.generateToken(principal);

        assertThat(shortLived.isValid(token, principal)).isFalse();
    }
}
