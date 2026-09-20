package com.aiproject.task;

import com.aiproject.common.ApiResponse;
import com.aiproject.security.UserPrincipal;
import com.aiproject.task.dto.TaskCreateRequest;
import com.aiproject.task.dto.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 生成任务接口：/api/v1/tasks
 * 所有操作 userId 从登录态注入（@AuthenticationPrincipal），不信任客户端。
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskProgressNotifier notifier;

    /** 提交生成任务：POST /api/v1/tasks */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> submit(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.ok(taskService.submit(principal.getId(), request.getDraftId()));
    }

    /** 我的任务列表：GET /api/v1/tasks */
    @GetMapping
    public ApiResponse<List<TaskResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(taskService.listTasks(principal.getId()));
    }

    /** 任务详情（轮询用）：GET /api/v1/tasks/{id} */
    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> detail(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id) {
        return ApiResponse.ok(taskService.getTask(principal.getId(), id));
    }

    /**
     * SSE 进度推送（第 5 课）：GET /api/v1/tasks/{id}/stream
     * 服务端持续推送 progress 事件；SUCCESS/FAILED 后自动关闭连接。
     * 鉴权：先校验任务归属（越权订阅 = 404），再建立连接。
     */
    @GetMapping(value = "/{id}/stream", produces = "text/event-stream")
    public SseEmitter stream(@AuthenticationPrincipal UserPrincipal principal,
                             @PathVariable Long id) {
        // 归属校验：不存在或非本人 → 404（不泄露资源存在性）
        taskService.getTask(principal.getId(), id);
        return notifier.subscribe(principal.getId(), id);
    }
}
