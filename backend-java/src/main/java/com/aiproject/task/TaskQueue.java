package com.aiproject.task;

import java.util.Optional;

/**
 * 任务队列抽象：提交任务后异步处理，接口隔离实现。
 * - 生产（!test）：Redis Stream（XADD/XREAD），跨实例共享、可持久化
 * - 测试（test）：进程内 ConcurrentLinkedQueue，零依赖跑通流程
 * 业务代码只依赖本接口——换队列实现不碰 TaskService。
 */
public interface TaskQueue {

    /** 入队任务 id */
    void enqueue(Long taskId);

    /** 非阻塞取一个待处理任务（worker 轮询调用） */
    Optional<Long> poll();
}
