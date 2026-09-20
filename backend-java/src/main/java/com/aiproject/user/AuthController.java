package com.aiproject.user;

import com.aiproject.common.ApiResponse;
import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.security.JwtTokenProvider;
import com.aiproject.security.UserPrincipal;
import com.aiproject.user.dto.AuthResponse;
import com.aiproject.user.dto.LoginRequest;
import com.aiproject.user.dto.RegisterRequest;
import com.aiproject.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ApiResponse.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // username 字段语义扩展为 identifier：支持用户名或手机号登录
        User user = userRepository.findByUsernameOrPhone(request.getUsername())
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(principal);
        AuthResponse response = new AuthResponse(token, "Bearer", 86400L, UserResponse.from(user));
        return ApiResponse.ok(response);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
        return ApiResponse.ok(UserResponse.from(user));
    }
}
