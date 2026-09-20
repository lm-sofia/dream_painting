package com.aiproject.task;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 进程内队列（测试实现）：零依赖，跑通"提交→入队→消费"全流程。
 * 仅用于本地测试/CI；生产走 RedisTaskQueue。
 */
@Component
@Profile("test")
public class InMemoryTaskQueue implements TaskQueue {

    private final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void enqueue(Long taskId) {
        queue.offer(taskId);
    }

    @Override
    public Optional<Long> poll() {
        Long taskId = queue.poll();
        return Optional.ofNullable(taskId);
    }
}
