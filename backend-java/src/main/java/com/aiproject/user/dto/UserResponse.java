package com.aiproject.user.dto;

import com.aiproject.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private Integer level;
    private Integer continuousDays;
    private java.time.Instant createdAt;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                user.getPhone(), user.getLevel(), user.getContinuousDays(), user.getCreatedAt());
    }
}
