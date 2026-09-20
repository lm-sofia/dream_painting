package com.aiproject.work;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.task.TaskService;
import com.aiproject.task.dto.TaskResponse;
import com.aiproject.work.dto.WorkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 作品服务：列表/详情/重新生成。
 */
@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final TaskService taskService;

    /** 我的作品（按创建倒序） */
    @Transactional(readOnly = true)
    public List<WorkResponse> list(Long userId) {
        return workRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WorkResponse::from)
                .toList();
    }

    /** 作品详情（越权 = 404） */
    @Transactional(readOnly = true)
    public WorkResponse detail(Long userId, Long workId) {
        Work work = workRepository.findByIdAndUserId(workId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "作品不存在"));
        return WorkResponse.from(work);
    }

    /**
     * 重新生成：基于作品的草稿再提交一个任务（业务闭环）。
     * 幂等/额度/状态机全部复用 TaskService.submit（第 4 课成果）。
     */
    @Transactional
    public TaskResponse regenerate(Long userId, Long workId) {
        Work work = workRepository.findByIdAndUserId(workId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "作品不存在"));
        return taskService.submit(userId, work.getDraftId());
    }
}
