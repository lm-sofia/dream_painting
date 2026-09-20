package com.aiproject.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "generation_tasks", indexes = {
        @Index(name = "idx_tasks_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class GenerationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 归属用户（同草稿模式：所有操作带 userId 守卫） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 来源草稿（逻辑关联 drafts.id） */
    @Column(name = "draft_id", nullable = false)
    private Long draftId;

    /** 当前状态（6 态状态机） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    /** 当前智能体阶段（AGENTS 状态下有意义；第 5 课） */
    @Enumerated(EnumType.STRING)
    @Column(name = "agent_stage", length = 20)
    private AgentStage agentStage;

    /** 智能体内部进度 0-100（当前智能体的细粒度进度） */
    @Column(name = "agent_progress", nullable = false)
    private Integer agentProgress = 0;

    /** 进度 0-100（worker 阶段推进时更新） */
    @Column(nullable = false)
    private Integer progress = 0;

    /** 当前阶段文案（如"编剧智能体构思中…"，前端展示） */
    @Column(name = "stage_message", length = 200)
    private String stageMessage;

    /** 失败原因（status=FAILED 时有值） */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** 结果：视频 URL（status=SUCCESS 后有值，对象存储） */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** 生成引擎侧任务 ID（第 7 课：提交真实模型后回填，worker 轮询用它；重启后可续查） */
    @Column(name = "provider_task_id", length = 100)
    private String providerTaskId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
