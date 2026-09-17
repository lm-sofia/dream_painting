using AiProject.Api.Models;

namespace AiProject.Api.DTOs;

public class UserResponse
{
    public long Id { get; set; }
    public string Username { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }

    public static UserResponse From(User user) => new()
    {
        Id = user.Id,
        Username = user.Username,
        Email = user.Email,
        Role = user.Role,
        CreatedAt = user.CreatedAt
    };
}
