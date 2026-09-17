using AiProject.Api.Common;
using AiProject.Api.Data;
using AiProject.Api.DTOs;
using AiProject.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace AiProject.Api.Services;

public class ArticleService
{
    private readonly AppDbContext _db;

    public ArticleService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<PageResponse<ArticleResponse>> ListPublicAsync(int page, int pageSize)
    {
        page = Math.Max(page, 1);
        pageSize = Math.Clamp(pageSize, 1, 100);

        var query = _db.Articles.Where(a => a.Published).OrderByDescending(a => a.CreatedAt);
        var total = await query.CountAsync();
        var articles = await query.Skip((page - 1) * pageSize).Take(pageSize).ToListAsync();

        var items = new List<ArticleResponse>();
        foreach (var a in articles)
        {
            items.Add(ArticleResponse.From(a, await GetAuthorNameAsync(a.UserId)));
        }

        return new PageResponse<ArticleResponse>
        {
            Items = items,
            Total = total,
            Page = page,
            PageSize = pageSize
        };
    }

    public async Task<ArticleResponse> DetailAsync(long id)
    {
        var article = await _db.Articles.FirstOrDefaultAsync(a => a.Id == id && a.Published)
            ?? throw new BizException(ErrorCodes.NotFound, "文章不存在");
        return ArticleResponse.From(article, await GetAuthorNameAsync(article.UserId));
    }

    public async Task<ArticleResponse> CreateAsync(long userId, ArticleRequest request)
    {
        var article = new Article
        {
            UserId = userId,
            Title = request.Title,
            Content = request.Content ?? string.Empty,
            Published = request.Published
        };

        _db.Articles.Add(article);
        await _db.SaveChangesAsync();
        return ArticleResponse.From(article, await GetAuthorNameAsync(userId));
    }

    public async Task<ArticleResponse> UpdateAsync(long userId, string role, long id, ArticleRequest request)
    {
        var article = await GetOwnedArticleAsync(id, userId, role);
        article.Title = request.Title;
        article.Content = request.Content ?? string.Empty;
        article.Published = request.Published;
        article.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return ArticleResponse.From(article, await GetAuthorNameAsync(article.UserId));
    }

    public async Task DeleteAsync(long userId, string role, long id)
    {
        var article = await GetOwnedArticleAsync(id, userId, role);
        _db.Articles.Remove(article);
        await _db.SaveChangesAsync();
    }

    private async Task<Article> GetOwnedArticleAsync(long id, long userId, string role)
    {
        var article = await _db.Articles.FirstOrDefaultAsync(a => a.Id == id)
            ?? throw new BizException(ErrorCodes.NotFound, "文章不存在");

        if (role != "ADMIN" && article.UserId != userId)
            throw new BizException(ErrorCodes.Forbidden, "无权限执行此操作");

        return article;
    }

    private async Task<string> GetAuthorNameAsync(long userId)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Id == userId);
        return user?.Username ?? "unknown";
    }
}
