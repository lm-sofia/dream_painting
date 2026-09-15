package com.aiproject.article;

import com.aiproject.article.dto.ArticleRequest;
import com.aiproject.article.dto.ArticleResponse;
import com.aiproject.article.dto.PageResponse;
import com.aiproject.common.ApiResponse;
import com.aiproject.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<PageResponse<ArticleResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(articleService.listPublic(page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(articleService.detail(id));
    }

    @PostMapping
    public ApiResponse<ArticleResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(articleService.create(principal, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(articleService.update(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        articleService.delete(principal, id);
        return ApiResponse.ok();
    }
}
