package com.aiproject.task;

import com.aiproject.task.dto.TaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 进度推送器（观察者模式）：worker 推进任务 → publish 推给订阅的前端。
 * 注册表 key = "userId:taskId"：订阅时已校验归属，推送按 key 精准投递。
 * 安全设计：推送失败（客户端断开）只移除订阅，绝不影响生成主流程。
 */
@Slf4j
@Component
public class TaskProgressNotifier {

    private static final long EMITTER_TIMEOUT_MS = 300_000L; // 5 分钟（超过任务最长期限）

    private final Map<String, List<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    private static String key(Long userId, Long taskId) {
        return userId + ":" + taskId;
    }

    /** 订阅进度流（Controller 调用；校验已在 Controller 完成） */
    public SseEmitter subscribe(Long userId, Long taskId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        subscribers.computeIfAbsent(key(userId, taskId), k -> new CopyOnWriteArrayList<>())
                .add(emitter);
        emitter.onCompletion(() -> remove(userId, taskId, emitter));
        emitter.onTimeout(() -> remove(userId, taskId, emitter));
        emitter.onError(e -> remove(userId, taskId, emitter));
        return emitter;
    }

    /** 发布最新任务状态给订阅者（worker 每次推进后调用） */
    public void publish(Long userId, Long taskId, TaskResponse response) {
        List<SseEmitter> list = subscribers.get(key(userId, taskId));
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("progress").data(response));
                if (response.getStatus() == TaskStatus.SUCCESS || response.getStatus() == TaskStatus.FAILED) {
                    emitter.complete(); // 终态：关闭连接，前端收到后停止
                }
            } catch (Exception e) {
                // 客户端断开/推送失败：移除订阅，静默处理（推送绝不能影响生成主流程）
                log.debug("SSE 推送失败（客户端可能已断开）: {}", e.getMessage());
                list.remove(emitter);
            }
        }
    }

    private void remove(Long userId, Long taskId, SseEmitter emitter) {
        List<SseEmitter> list = subscribers.get(key(userId, taskId));
        if (list != null) {
            list.remove(emitter);
        }
    }
}
