package com.aiproject.style;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "styles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_styles_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Style {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 风格名称，如「新国风」「赛博霓虹」 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 一句话描述（列表卡片展示） */
    @Column(length = 500)
    private String description;

    /** 分类：国风 / 赛博 / 治愈 / 热血 / 搞笑 / 奇幻 */
    @Column(nullable = false, length = 30)
    private String category;

    /** 封面图 URL（对象存储/CDN） */
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    /** 提示词模板：生成时用户创意填充占位符 {idea}，交给 7 智能体 */
    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;

    /** 排序权重（越小越靠前） */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 是否上架（下架=不可选，保留历史作品） */
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
