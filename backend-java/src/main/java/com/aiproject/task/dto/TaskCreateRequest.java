package com.aiproject.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 提交生成任务：POST /api/v1/tasks */
@Data
public class TaskCreateRequest {

    /** 来源草稿 id（服务端校验归属，不信任客户端 userId） */
    @NotNull(message = "draftId 不能为空")
    private Long draftId;
}
