using System.Security.Claims;
using AiProject.Api.Common;
using AiProject.Api.DTOs;
using AiProject.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AiProject.Api.Controllers;

[ApiController]
[Route("api/v1/articles")]
public class ArticlesController : ControllerBase
{
    private readonly ArticleService _articleService;

    public ArticlesController(ArticleService articleService)
    {
        _articleService = articleService;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<PageResponse<ArticleResponse>>>> List(
        [FromQuery] int page = 1, [FromQuery] int pageSize = 10)
    {
        return ApiResponse<PageResponse<ArticleResponse>>.Ok(
            await _articleService.ListPublicAsync(page, pageSize));
    }

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<ArticleResponse>>> Detail(long id)
    {
        return ApiResponse<ArticleResponse>.Ok(await _articleService.DetailAsync(id));
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<ArticleResponse>>> Create([FromBody] ArticleRequest request)
    {
        if (!ModelState.IsValid)
            throw new BizException(ErrorCodes.BadRequest, "参数校验失败");
        var userId = GetUserId();
        return ApiResponse<ArticleResponse>.Ok(await _articleService.CreateAsync(userId, request));
    }

    [HttpPut("{id:long}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<ArticleResponse>>> Update(long id, [FromBody] ArticleRequest request)
    {
        if (!ModelState.IsValid)
            throw new BizException(ErrorCodes.BadRequest, "参数校验失败");
        var (userId, role) = GetIdentity();
        return ApiResponse<ArticleResponse>.Ok(
            await _articleService.UpdateAsync(userId, role, id, request));
    }

    [HttpDelete("{id:long}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object?>>> Delete(long id)
    {
        var (userId, role) = GetIdentity();
        await _articleService.DeleteAsync(userId, role, id);
        return ApiResponse<object?>.Ok();
    }

    private long GetUserId() => long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? "0");

    private (long userId, string role) GetIdentity()
    {
        var userId = long.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? "0");
        var role = User.FindFirstValue(ClaimTypes.Role) ?? "USER";
        return (userId, role);
    }
}
