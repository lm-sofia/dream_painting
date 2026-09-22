package com.aiproject.task;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.credit.CreditService;
import com.aiproject.draft.Draft;
import com.aiproject.draft.DraftRepository;
import com.aiproject.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 生成任务服务：提交（幂等+扣费+入队）、查询、状态推进。
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final DraftRepository draftRepository;
    private final CreditService creditService;
    private final TaskQueue taskQueue;
    private final TaskProgressNotifier notifier;
    private final com.aiproject.work.WorkRepository workRepository;

    /**
     * 提交生成任务（POST /tasks）。
     * 事务链：校验草稿归属 → 幂等检查 → 建任务 → 扣费（失败则整体回滚）→ 提交后入队。
     */
    @Transactional
    public TaskResponse submit(Long userId, Long draftId) {
        // ① 草稿归属校验（不信任客户端传 userId；别人的草稿 = 404）
        Draft draft = draftRepository.findByIdAndUserId(draftId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "草稿不存在"));

        // ② 幂等：同一草稿已有运行中任务（PENDING/AGENTS/RENDERING/COMPILING）→ 拒绝重复提交
        boolean running = taskRepository.existsByDraftIdAndStatusIn(
                draftId, List.of(TaskStatus.PENDING, TaskStatus.AGENTS,
                        TaskStatus.RENDERING, TaskStatus.COMPILING));
        if (running) {
            throw new BizException(ErrorCode.CONFLICT, "该草稿正在生成中，请勿重复提交");
        }

        // ③ 建任务（默认 PENDING）
        GenerationTask task = new GenerationTask();
        task.setUserId(userId);
        task.setDraftId(draftId);
        task.setStatus(TaskStatus.PENDING);
        task.setProgress(0);
        task.setStageMessage("排队中…");
        try {
            taskRepository.save(task);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发兜底：两个请求同时通过了②的应用层检查，数据库唯一索引 uq_tasks_draft_running 拦第二个
            // 转成友好的 409，而不是 500
            throw new BizException(ErrorCode.CONFLICT, "该草稿正在生成中，请勿重复提交");
        }

        // ④ 扣费（同事务：余额不足抛异常 → 任务创建一并回滚，不会出现"没扣费却建了任务"）
        creditService.deduct(userId, task.getId());

        // ⑤ 事务提交后再入队：防止 worker 在任务未提交时就消费到它
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskQueue.enqueue(task.getId());
            }
        });

        return TaskResponse.from(task);
    }

    /** 我的任务列表（按创建倒序） */
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    /** 任务详情（越权 = 404） */
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long userId, Long taskId) {
        GenerationTask task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        return TaskResponse.from(task);
    }

    /** 仅供内部 worker 使用：按 id 读任务当前状态（无 userId 校验，不暴露给 HTTP） */
    @Transactional(readOnly = true)
    public GenerationTask findByIdForWorker(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
    }

    /**
     * 仅供内部 worker：回填生成引擎侧任务 ID（第 7 课）。
     * 为什么必须走这里：worker 持有的 task 是游离对象，直接 set 不会落库；
     * 只有在本事务内重新加载再保存，修改才持久化。
     */
    @Transactional
    public void saveProviderTaskId(Long taskId, String providerTaskId) {
        GenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        task.setProviderTaskId(providerTaskId);
        taskRepository.save(task);
    }

    /**
     * 状态推进（worker 内部调用，不暴露给 HTTP）。
     * 读 → transitionTo 校验合法性 → 更新 → 保存 → SSE 推送。
     */
    @Transactional
    public void advance(Long taskId, TaskStatus next, int progress, String stageMessage) {
        GenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        task.setStatus(task.getStatus().transitionTo(next));
        task.setProgress(progress);
        task.setStageMessage(stageMessage);
        taskRepository.save(task);
        publish(task);
    }

    /**
     * 智能体阶段推进（第 5 课）：PENDING→AGENTS 迁移 + 切换/更新当前智能体。
     */
    @Transactional
    public void advanceAgent(Long taskId, AgentStage stage, int agentProgress, String stageMessage) {
        GenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        // 关键：首次调用时把 PENDING 迁移到 AGENTS（状态机守门）
        if (task.getStatus() != TaskStatus.AGENTS) {
            task.setStatus(task.getStatus().transitionTo(TaskStatus.AGENTS));
        }
        task.setAgentStage(stage);
        task.setAgentProgress(agentProgress);
        task.setProgress(stage.getTargetProgress());
        task.setStageMessage(stageMessage);
        taskRepository.save(task);
        publish(task);
    }

    /** 标记失败（任意状态可迁移到 FAILED） */
    @Transactional
    public void fail(Long taskId, String errorMessage) {
        GenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        task.setStatus(task.getStatus().transitionTo(TaskStatus.FAILED));
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);
        publish(task);
    }

    /**
     * 生成完成（第 6 课）：COMPILING → SUCCESS + 写入视频地址 + 自动落作品。
     * 同一事务：任务成功与作品入库要么都发生，要么都回滚。
     */
    @Transactional
    public void complete(Long taskId, String videoUrl) {
        GenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "任务不存在"));
        task.setStatus(task.getStatus().transitionTo(TaskStatus.SUCCESS));
        task.setProgress(100);
        task.setVideoUrl(videoUrl);
        task.setStageMessage("生成完成 🎉");
        taskRepository.save(task);

        // 作品落库：标题取自草稿；版本 = 该草稿已有作品数 + 1（v1/v2…）
        Draft draft = draftRepository.findById(task.getDraftId()).orElse(null);
        com.aiproject.work.Work work = new com.aiproject.work.Work();
        work.setUserId(task.getUserId());
        work.setDraftId(task.getDraftId());
        work.setTaskId(taskId);
        work.setTitle(draft != null ? draft.getTitle() : "未命名作品");
        work.setVideoUrl(videoUrl);
        work.setVersion((int) workRepository.countByDraftId(task.getDraftId()) + 1);
        workRepository.save(work);

        publish(task);
    }

    /** SSE 推送（幂等：无订阅者时静默跳过） */
    private void publish(GenerationTask task) {
        notifier.publish(task.getUserId(), task.getId(), TaskResponse.from(task));
    }
}
