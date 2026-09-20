package com.aiproject.draft;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DraftRepository extends JpaRepository<Draft, Long> {

    /** 我的草稿列表（按更新时间倒序） */
    List<Draft> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * 越权防护核心：所有按 id 的操作必须同时带 userId。
     * 找不到 = 草稿不存在 或 不属于你（两者返回同样结果，不泄露是否存在）
     */
    Optional<Draft> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
