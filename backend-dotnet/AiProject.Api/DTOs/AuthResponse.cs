namespace AiProject.Api.DTOs;

public class AuthResponse
{
    public string Token { get; set; } = string.Empty;
    public string TokenType { get; set; } = "Bearer";
    public long ExpiresIn { get; set; }
    public UserResponse User { get; set; } = new();
}
