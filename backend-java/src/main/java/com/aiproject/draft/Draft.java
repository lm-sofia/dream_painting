package com.aiproject.draft;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "drafts", indexes = {
        @Index(name = "idx_drafts_user_updated", columnList = "user_id, updated_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 归属用户（草稿是私有数据，所有查询/修改必须带 userId —— 越权防护核心） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 作品标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 一句话创意（用户脑洞，交给 7 智能体扩写） */
    @Column(columnDefinition = "TEXT")
    private String idea;

    /** 所选风格 id（styles 表外键，逻辑关联） */
    @Column(name = "style_id")
    private Long styleId;

    /** 时长（秒）：15 / 30 / 60 */
    @Column(nullable = false)
    private Integer duration = 30;

    /** 画面比例：9:16 / 16:9 / 1:1 */
    @Column(nullable = false, length = 10)
    private String ratio = "9:16";

    /** 是否开启 AI 配音/音效 */
    @Column(nullable = false)
    private Boolean voiceover = true;

    /** 草稿状态：DRAFT（第 4 课提交生成时转为任务） */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
