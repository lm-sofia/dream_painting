package com.aiproject.article.dto;

import com.aiproject.article.Article;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ArticleResponse {

    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String author;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;

    public static ArticleResponse from(Article article, String authorName) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getUserId(),
                authorName,
                article.isPublished(),
                article.getCreatedAt(),
                article.getUpdatedAt());
    }
}
