package com.aiproject.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<GenerationTask, Long> {

    List<GenerationTask> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 越权防护：所有按 id 的操作带 userId（同草稿模式） */
    Optional<GenerationTask> findByIdAndUserId(Long id, Long userId);

    /** 幂等判断：该草稿是否已有运行中任务（配合 PG 部分唯一索引双保险） */
    boolean existsByDraftIdAndStatusIn(Long draftId, List<TaskStatus> statuses);
}
