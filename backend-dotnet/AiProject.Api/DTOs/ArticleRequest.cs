using System.ComponentModel.DataAnnotations;

namespace AiProject.Api.DTOs;

public class ArticleRequest
{
    [Required(ErrorMessage = "标题不能为空")]
    [StringLength(200, ErrorMessage = "标题长度不能超过 200")]
    public string Title { get; set; } = string.Empty;

    public string? Content { get; set; }

    public bool Published { get; set; } = false;
}
