namespace AiProject.Api.DTOs;

/// <summary>
/// 分页响应，对应契约 GET /api/v1/articles 的 data 结构。
/// </summary>
public class PageResponse<T>
{
    public List<T> Items { get; set; } = new();
    public long Total { get; set; }
    public int Page { get; set; }
    public int PageSize { get; set; }
}
