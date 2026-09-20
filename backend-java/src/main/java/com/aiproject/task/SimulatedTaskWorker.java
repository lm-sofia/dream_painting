package com.aiproject.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 模拟生成 worker（仅测试环境）：
 * 每 1 秒推进一格 —— 7 智能体流水线（每智能体 2 格：50%→100%）→ 渲染 → 合成 → 完成。
 * 全程约 18 秒，用于演示 SSE 实时推送；生产由真实 AI 服务链替换（接口不变）。
 */
@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class SimulatedTaskWorker {

    private final TaskQueue taskQueue;
    private final TaskService taskService;

    @Scheduled(fixedDelay = 1000)
    public void work() {
        taskQueue.poll().ifPresent(taskId -> {
            try {
                GenerationTask task = find(taskId);
                switch (task.getStatus()) {
                    case PENDING -> {
                        taskService.advanceAgent(taskId, AgentStage.SCREENWRITER, 50,
                                "编剧智能体构思剧情…");
                        taskQueue.enqueue(taskId);
                    }
                    case AGENTS -> tickAgents(taskId, task);
                    case RENDERING -> {
                        taskService.advance(taskId, TaskStatus.COMPILING, 85, "音效/剪辑智能体合成中…");
                        taskQueue.enqueue(taskId);
                    }
                    case COMPILING -> taskService.complete(taskId,
                            "https://www.w3schools.com/html/mov_bbb.mp4"); // 演示视频；生产=对象存储 URL
                    default -> log.info("任务 {} 已到终态 {}", taskId, task.getStatus());
                }
                log.info("[模拟worker] 任务 {} → {} {}", taskId, task.getStatus(), task.getStageMessage());
            } catch (Exception e) {
                taskService.fail(taskId, "模拟生成失败: " + e.getMessage());
            }
        });
    }

    /** 智能体流水线推进：当前智能体 50%→100%，完成则切下一个，7 个走完进渲染 */
    private void tickAgents(Long taskId, GenerationTask task) {
        AgentStage cur = task.getAgentStage();
        if (cur == null) {
            // 防御：理论不会走到（PENDING 已设置第一个智能体）
            taskService.advanceAgent(taskId, AgentStage.SCREENWRITER, 50, "编剧智能体构思剧情…");
        } else if (task.getAgentProgress() < 100) {
            // 当前智能体 50 → 100
            taskService.advanceAgent(taskId, cur, 100, cur.getLabel() + "智能体完成 ✓");
        } else {
            // 当前智能体已完成 → 下一个；没有下一个则进入渲染
            AgentStage next = cur.next();
            if (next != null) {
                taskService.advanceAgent(taskId, next, 50, next.getLabel() + "智能体工作中…");
            } else {
                taskService.advance(taskId, TaskStatus.RENDERING, 60, "渲染画面…");
            }
        }
        taskQueue.enqueue(taskId);
    }

    private GenerationTask find(Long taskId) {
        return taskService.findByIdForWorker(taskId);
    }
}
