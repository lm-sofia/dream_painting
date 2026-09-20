package com.aiproject.work;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work, Long> {

    List<Work> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 越权防护（同草稿/任务模式） */
    Optional<Work> findByIdAndUserId(Long id, Long userId);

    /** 版本号计算：该草稿已有的成功作品数 */
    long countByDraftId(Long draftId);
}
