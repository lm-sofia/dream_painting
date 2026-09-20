package com.aiproject.style.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StyleRequest {

    @NotBlank(message = "风格名称不能为空")
    @Size(max = 50, message = "名称最长 50 字")
    private String name;

    @Size(max = 500, message = "描述最长 500 字")
    private String description;

    @NotBlank(message = "分类不能为空")
    @Size(max = 30, message = "分类最长 30 字")
    private String category;

    @Size(max = 500, message = "封面 URL 最长 500 字")
    private String coverUrl;

    private String promptTemplate;

    private Integer sortOrder = 0;

    private Boolean active = true;
}
