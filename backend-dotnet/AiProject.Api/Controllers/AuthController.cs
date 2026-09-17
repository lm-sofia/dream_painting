using System.Security.Claims;
using AiProject.Api.Common;
using AiProject.Api.DTOs;
using AiProject.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AiProject.Api.Controllers;

[ApiController]
[Route("api/v1/auth")]
public class AuthController : ControllerBase
{
    private readonly AuthService _authService;

    public AuthController(AuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<UserResponse>>> Register([FromBody] RegisterRequest request)
    {
        if (!ModelState.IsValid)
            throw new BizException(ErrorCodes.BadRequest, "参数校验失败");
        return ApiResponse<UserResponse>.Ok(await _authService.RegisterAsync(request));
    }

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<AuthResponse>>> Login([FromBody] LoginRequest request)
    {
        if (!ModelState.IsValid)
            throw new BizException(ErrorCodes.BadRequest, "参数校验失败");
        return ApiResponse<AuthResponse>.Ok(await _authService.LoginAsync(request));
    }

    [HttpGet("me")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UserResponse>>> Me()
    {
        var username = User.FindFirstValue("username");
        var user = await _authService.GetUserByUsernameAsync(username ?? string.Empty);
        return ApiResponse<UserResponse>.Ok(UserResponse.From(user));
    }
}
