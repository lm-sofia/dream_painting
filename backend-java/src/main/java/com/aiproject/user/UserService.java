package com.aiproject.user;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.user.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BizException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BizException(ErrorCode.CONFLICT, "邮箱已被注册");
        }
        // 手机号可选：填了才校验唯一（空值不冲突，多个 NULL 合法）
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new BizException(ErrorCode.CONFLICT, "手机号已被注册");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("USER");
        // 游戏化字段默认值（与 DB 列默认值一致，双保险）
        user.setLevel(1);
        user.setContinuousDays(0);
        return userRepository.save(user);
    }
}
