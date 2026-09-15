package com.aiproject.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页响应，对应契约 GET /api/v1/articles 的 data 结构。
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private long total;
    private int page;
    private int pageSize;
}
