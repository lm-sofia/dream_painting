package com.aiproject.task.dto;

import com.aiproject.task.AgentStage;
import com.aiproject.task.GenerationTask;
import com.aiproject.task.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** 任务响应：GET /tasks、GET /tasks/{id}、SSE 推送 */
@Data
@Builder
public class TaskResponse {

    private Long id;
    private Long draftId;
    private TaskStatus status;
    private Integer progress;
    /** 当前智能体（AGENTS 状态） */
    private AgentStage agentStage;
    /** 智能体内部进度 0-100 */
    private Integer agentProgress;
    private String stageMessage;
    private String errorMessage;
    private String videoUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(GenerationTask t) {
        return TaskResponse.builder()
                .id(t.getId())
                .draftId(t.getDraftId())
                .status(t.getStatus())
                .progress(t.getProgress())
                .agentStage(t.getAgentStage())
                .agentProgress(t.getAgentProgress())
                .stageMessage(t.getStageMessage())
                .errorMessage(t.getErrorMessage())
                .videoUrl(t.getVideoUrl())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
