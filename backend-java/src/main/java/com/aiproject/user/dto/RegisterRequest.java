package com.aiproject.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "不能为空")
    @Size(min = 3, max = 50, message = "长度需在 3-50 之间")
    private String username;

    @NotBlank(message = "不能为空")
    @Email(message = "格式不合法")
    private String email;

    @NotBlank(message = "不能为空")
    @Size(min = 8, max = 64, message = "长度需在 8-64 之间")
    private String password;
}
