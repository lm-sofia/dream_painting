package com.aiproject.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 200, message = "长度不能超过 200")
    private String title;

    private String content;

    private Boolean published = false;
}
