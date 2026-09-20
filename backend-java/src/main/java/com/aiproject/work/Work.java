package com.aiproject.work;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 作品：任务 SUCCESS 时自动落一条（业务闭环——生成完成即入库）。
 * 版本：同一草稿的每次成功生成 version +1（v1/v2…），唯一约束 (draft_id, version)。
 */
@Entity
@Table(name = "works", uniqueConstraints = {
        @UniqueConstraint(name = "uq_works_draft_version", columnNames = {"draft_id", "version"})
}, indexes = {
        @Index(name = "idx_works_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "draft_id", nullable = false)
    private Long draftId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(nullable = false, length = 100)
    private String title;

    /** 视频地址（生产=对象存储 URL；当前演示=外链） */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    /** 版本号：同一草稿的第几次成功生成 */
    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
