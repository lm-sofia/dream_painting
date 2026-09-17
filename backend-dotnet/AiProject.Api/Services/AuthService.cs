using AiProject.Api.Common;
using AiProject.Api.Data;
using AiProject.Api.DTOs;
using AiProject.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace AiProject.Api.Services;

public class AuthService
{
    private readonly AppDbContext _db;
    private readonly JwtTokenService _jwt;

    public AuthService(AppDbContext db, JwtTokenService jwt)
    {
        _db = db;
        _jwt = jwt;
    }

    public async Task<UserResponse> RegisterAsync(RegisterRequest request)
    {
        if (await _db.Users.AnyAsync(u => u.Username == request.Username))
            throw new BizException(ErrorCodes.Conflict, "用户名已存在");

        if (await _db.Users.AnyAsync(u => u.Email == request.Email))
            throw new BizException(ErrorCodes.Conflict, "邮箱已被注册");

        var user = new User
        {
            Username = request.Username,
            Email = request.Email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password, workFactor: 10),
            Role = "USER"
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();
        return UserResponse.From(user);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Username == request.Username)
            ?? throw new BizException(ErrorCodes.Unauthorized, "用户名或密码错误");

        if (!BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
            throw new BizException(ErrorCodes.Unauthorized, "用户名或密码错误");

        var token = _jwt.GenerateToken(user.Id, user.Username, user.Role);
        return new AuthResponse
        {
            Token = token,
            TokenType = "Bearer",
            ExpiresIn = 86400,
            User = UserResponse.From(user)
        };
    }

    public async Task<User> GetUserByUsernameAsync(string username)
    {
        return await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new BizException(ErrorCodes.Unauthorized, "用户不存在");
    }
}
