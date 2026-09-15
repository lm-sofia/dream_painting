package com.aiproject.article;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.article.dto.ArticleRequest;
import com.aiproject.article.dto.ArticleResponse;
import com.aiproject.article.dto.PageResponse;
import com.aiproject.security.UserPrincipal;
import com.aiproject.user.User;
import com.aiproject.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public PageResponse<ArticleResponse> listPublic(int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Article> result = articleRepository.findByPublishedTrue(pageable);
        List<ArticleResponse> items = result.getContent().stream()
                .map(a -> ArticleResponse.from(a, authorName(a.getUserId())))
                .toList();
        return new PageResponse<>(items, result.getTotalElements(), page, pageSize);
    }

    public ArticleResponse detail(Long id) {
        Article article = articleRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        return ArticleResponse.from(article, authorName(article.getUserId()));
    }

    @Transactional
    public ArticleResponse create(UserPrincipal principal, ArticleRequest request) {
        Article article = new Article();
        article.setUserId(principal.getId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent() == null ? "" : request.getContent());
        article.setPublished(Boolean.TRUE.equals(request.getPublished()));
        return ArticleResponse.from(articleRepository.save(article), principal.getUsername());
    }

    @Transactional
    public ArticleResponse update(UserPrincipal principal, Long id, ArticleRequest request) {
        Article article = getOwnedArticle(id, principal);
        article.setTitle(request.getTitle());
        article.setContent(request.getContent() == null ? "" : request.getContent());
        article.setPublished(Boolean.TRUE.equals(request.getPublished()));
        return ArticleResponse.from(articleRepository.save(article), principal.getUsername());
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Article article = getOwnedArticle(id, principal);
        articleRepository.delete(article);
    }

    private Article getOwnedArticle(Long id, UserPrincipal principal) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        boolean isAdmin = "ADMIN".equals(principal.getRole());
        if (!isAdmin && !article.getUserId().equals(principal.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return article;
    }

    private String authorName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("unknown");
    }
}
