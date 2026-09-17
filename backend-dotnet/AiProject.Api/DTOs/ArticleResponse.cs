using AiProject.Api.Models;

namespace AiProject.Api.DTOs;

public class ArticleResponse
{
    public long Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public long AuthorId { get; set; }
    public string Author { get; set; } = string.Empty;
    public bool Published { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public static ArticleResponse From(Article article, string authorName) => new()
    {
        Id = article.Id,
        Title = article.Title,
        Content = article.Content,
        AuthorId = article.UserId,
        Author = authorName,
        Published = article.Published,
        CreatedAt = article.CreatedAt,
        UpdatedAt = article.UpdatedAt
    };
}
