package com.aiproject.style.dto;

import com.aiproject.style.Style;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class StyleResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String coverUrl;
    private String promptTemplate;
    private Integer sortOrder;
    private Boolean active;
    private Instant createdAt;

    public static StyleResponse from(Style style) {
        return new StyleResponse(
                style.getId(), style.getName(), style.getDescription(), style.getCategory(),
                style.getCoverUrl(), style.getPromptTemplate(), style.getSortOrder(),
                style.getActive(), style.getCreatedAt());
    }
}
