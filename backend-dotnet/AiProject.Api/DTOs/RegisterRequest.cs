using System.ComponentModel.DataAnnotations;

namespace AiProject.Api.DTOs;

public class RegisterRequest
{
    [Required(ErrorMessage = "用户名不能为空")]
    [StringLength(50, MinimumLength = 3, ErrorMessage = "用户名长度需在 3-50 之间")]
    public string Username { get; set; } = string.Empty;

    [Required(ErrorMessage = "邮箱不能为空")]
    [EmailAddress(ErrorMessage = "邮箱格式不合法")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "密码不能为空")]
    [StringLength(64, MinimumLength = 8, ErrorMessage = "密码长度需在 8-64 之间")]
    public string Password { get; set; } = string.Empty;
}
