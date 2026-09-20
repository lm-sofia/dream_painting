package com.aiproject.task;

import com.aiproject.draft.Draft;
import com.aiproject.draft.DraftRepository;
import com.aiproject.generator.GenerateRequest;
import com.aiproject.generator.GeneratorStatus;
import com.aiproject.generator.VideoGenerator;
import com.aiproject.style.Style;
import com.aiproject.style.StyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生产生成 worker（!test profile，第 7 课）：
 * 消费 Redis 队列 → 把草稿描述+风格真正提交给生成引擎 → 轮询进度 → SSE 推送 → 完成落作品。
 * - 引擎可插拔：mock（默认，无 Key 跑通全链路）/ seedance（真实，需 ARK_API_KEY）
 * - 状态推进全部基于数据库当前状态分支，重启 worker 后任务可继续（无内存态依赖）
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RealTaskWorker {

    private final TaskQueue taskQueue;
    private final TaskService taskService;
    private final DraftRepository draftRepository;
    private final StyleRepository styleRepository;
    private final VideoGenerator videoGenerator;

    /** 提交时刻表：taskId → 提交时间（仅超时判定用；丢失最多导致不超时，可接受） */
    private final Map<Long, Instant> submittedAt = new ConcurrentHashMap<>();

    private static final Duration TIMEOUT = Duration.ofMinutes(10);

    @Scheduled(fixedDelay = 2000)
    public void work() {
        taskQueue.poll().ifPresent(taskId -> {
            try {
                GenerationTask task = taskService.findByIdForWorker(taskId);
                switch (task.getStatus()) {
                    case PENDING -> submitToEngine(task);
                    case AGENTS, RENDERING, COMPILING -> tickFromEngine(task);
                    default -> log.info("任务 {} 已到终态 {}", taskId, task.getStatus());
                }
            } catch (Exception e) {
                log.error("任务 {} 处理异常", taskId, e);
                taskService.fail(taskId, "生成失败: " + e.getMessage());
            }
        });
    }

    /** PENDING → 组装请求 → 提交引擎 → 存 providerTaskId → 进 AGENTS */
    private void submitToEngine(GenerationTask task) {
        Draft draft = draftRepository.findById(task.getDraftId()).orElse(null);
        if (draft == null) {
            taskService.fail(task.getId(), "草稿不存在");
            return;
        }
        Style style = draft.getStyleId() == null ? null
                : styleRepository.findById(draft.getStyleId()).orElse(null);

        // 关键：风格模板填充用户创意 → 描述真正进入生成链路
        String prompt = draft.getIdea() == null ? draft.getTitle() : draft.getIdea();
        if (style != null && style.getPromptTemplate() != null
                && style.getPromptTemplate().contains("{idea}")) {
            prompt = style.getPromptTemplate().replace("{idea}", prompt);
        }

        GenerateRequest request = new GenerateRequest(
                task.getUserId(), task.getDraftId(), prompt,
                style != null ? style.getName() : "默认",
                draft.getDuration() == null ? 5 : draft.getDuration(),
                draft.getRatio() == null ? "9:16" : draft.getRatio());

        String providerTaskId = videoGenerator.submit(request);
        // 游离实体 set 不落库 —— 必须走 TaskService 事务内持久化（第 7 课实战坑）
        taskService.saveProviderTaskId(task.getId(), providerTaskId);
        taskService.advanceAgent(task.getId(), AgentStage.SCREENWRITER, 50,
                "已提交生成引擎，编剧智能体构思剧情…");
        submittedAt.put(task.getId(), Instant.now());
        taskQueue.enqueue(task.getId());
    }

    /** AGENTS/RENDERING/COMPILING：查引擎进度，按当前状态分支推进（重启安全） */
    private void tickFromEngine(GenerationTask task) {
        if (submittedAt.containsKey(task.getId())
                && Instant.now().isAfter(submittedAt.get(task.getId()).plus(TIMEOUT))) {
            taskService.fail(task.getId(), "生成超时（>10 分钟），请重试");
            return;
        }
        if (task.getProviderTaskId() == null) {
            taskService.fail(task.getId(), "引擎任务丢失，请重新生成");
            return;
        }

        GeneratorStatus gs = videoGenerator.query(task.getProviderTaskId());
        switch (gs.status()) {
            case FAILED -> taskService.fail(task.getId(), gs.message());
            case SUCCEEDED -> {
                switch (task.getStatus()) {
                    case AGENTS -> {
                        taskService.advance(task.getId(), TaskStatus.RENDERING, 85, "渲染合成中…");
                        taskQueue.enqueue(task.getId());
                    }
                    case RENDERING -> {
                        taskService.advance(task.getId(), TaskStatus.COMPILING, 92, "剪辑/音效合成中…");
                        taskQueue.enqueue(task.getId());
                    }
                    case COMPILING -> taskService.complete(task.getId(), gs.videoUrl());
                    default -> { /* 理论不可达 */ }
                }
            }
            default -> {
                // RUNNING/SUBMITTED：推进 7 智能体进度文案（SSE 体验），继续等模型
                tickAgents(task);
                taskQueue.enqueue(task.getId());
            }
        }
    }

    /** 智能体流水线推进：当前 50%→100%，完成切下一个；走完停在导演等待模型（与模拟 worker 同款） */
    private void tickAgents(GenerationTask task) {
        AgentStage cur = task.getAgentStage();
        if (cur == null) {
            taskService.advanceAgent(task.getId(), AgentStage.SCREENWRITER, 50, "编剧智能体构思剧情…");
        } else if (task.getAgentProgress() < 100) {
            taskService.advanceAgent(task.getId(), cur, 100, cur.getLabel() + "智能体完成 ✓");
        } else {
            AgentStage next = cur.next();
            if (next != null) {
                taskService.advanceAgent(task.getId(), next, 50, next.getLabel() + "智能体创作中…");
            } else {
                // 导演完成、模型仍生成中：保持状态，下轮再查
                log.info("任务 {} 导演完成，等待生成引擎…", task.getId());
            }
        }
    }
}
