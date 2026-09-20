package com.aiproject.work;

import com.aiproject.common.ApiResponse;
import com.aiproject.security.UserPrincipal;
import com.aiproject.task.dto.TaskResponse;
import com.aiproject.work.dto.WorkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作品接口：/api/v1/works（第 6 课）
 */
@RestController
@RequestMapping("/api/v1/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    /** 我的作品：GET /api/v1/works */
    @GetMapping
    public ApiResponse<List<WorkResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(workService.list(principal.getId()));
    }

    /** 作品详情：GET /api/v1/works/{id} */
    @GetMapping("/{id}")
    public ApiResponse<WorkResponse> detail(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id) {
        return ApiResponse.ok(workService.detail(principal.getId(), id));
    }

    /** 重新生成：POST /api/v1/works/{id}/regenerate → 新任务 */
    @PostMapping("/{id}/regenerate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> regenerate(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id) {
        return ApiResponse.ok(workService.regenerate(principal.getId(), id));
    }
}
