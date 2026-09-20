package com.aiproject.style;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.style.dto.StyleRequest;
import com.aiproject.style.dto.StyleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StyleService {

    public static final String CACHE_STYLES = "styles";

    private final StyleRepository styleRepository;

    /**
     * 上架风格列表（公开）。
     * @Cacheable：先查缓存，命中直接返回；未命中才查库并写入缓存。
     * key="'list'"：列表用固定 key（整表一份），SpEL 单引号包裹字符串。
     */
    @Cacheable(cacheNames = CACHE_STYLES, key = "'list'")
    public List<StyleResponse> list() {
        return styleRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(StyleResponse::from).toList();
    }

    /**
     * 风格详情（公开）。
     * key="'style:' + #id"：每个 id 一个缓存项，SpEL 引用方法参数。
     */
    @Cacheable(cacheNames = CACHE_STYLES, key = "'style:' + #id")
    public StyleResponse detail(Long id) {
        Style style = styleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "风格不存在或已下架"));
        return StyleResponse.from(style);
    }

    /**
     * 新增风格（运营/管理端）。
     * @CacheEvict(allEntries=true)：清空 styles 缓存——列表和详情都可能过期，全清最安全。
     * 管理端低频操作，清缓存代价可忽略。
     */
    @Transactional
    @CacheEvict(cacheNames = CACHE_STYLES, allEntries = true)
    public StyleResponse create(StyleRequest request) {
        if (styleRepository.existsByName(request.getName())) {
            throw new BizException(ErrorCode.CONFLICT, "风格名称已存在");
        }
        Style style = new Style();
        applyRequest(style, request);
        return StyleResponse.from(styleRepository.save(style));
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE_STYLES, allEntries = true)
    public StyleResponse update(Long id, StyleRequest request) {
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "风格不存在"));
        applyRequest(style, request);
        return StyleResponse.from(styleRepository.save(style));
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE_STYLES, allEntries = true)
    public void delete(Long id) {
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "风格不存在"));
        // 软删除：仅下架，保留历史作品引用（PRD：下架不删除）
        style.setActive(false);
        styleRepository.save(style);
    }

    private void applyRequest(Style style, StyleRequest request) {
        style.setName(request.getName());
        style.setDescription(request.getDescription());
        style.setCategory(request.getCategory());
        style.setCoverUrl(request.getCoverUrl());
        style.setPromptTemplate(request.getPromptTemplate());
        if (request.getSortOrder() != null) {
            style.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            style.setActive(request.getActive());
        }
    }
}
