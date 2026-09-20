package com.aiproject.task;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;

import java.util.Map;
import java.util.Set;

/**
 * 生成任务 6 状态机。
 * <pre>
 * PENDING → AGENTS → RENDERING → COMPILING → SUCCESS
 *    └──────────────┴──────────────┴───────────┴→ FAILED（任意状态可失败）
 * </pre>
 * 迁移规则收敛在枚举里：非法迁移抛 409（如 SUCCESS 再变 AGENTS = 程序 bug，立即暴露）
 */
public enum TaskStatus {
    PENDING,    // 排队中（已入队，等待 worker）
    AGENTS,     // 7 智能体创作中（编剧/角色/场景/动画/剪辑/音效/导演）
    RENDERING,  // 渲染中（画面生成）
    COMPILING,  // 合成中（音频/字幕/转场合成）
    SUCCESS,    // 成功（可播放/导出）
    FAILED;     // 失败（记录 errorMessage）

    /** 每个状态允许的下一步（迁移表） */
    private static final Map<TaskStatus, Set<TaskStatus>> TRANSITIONS = Map.of(
            PENDING,   Set.of(AGENTS, FAILED),
            AGENTS,    Set.of(RENDERING, FAILED),
            RENDERING, Set.of(COMPILING, FAILED),
            COMPILING, Set.of(SUCCESS, FAILED),
            SUCCESS,   Set.of(),   // 终态
            FAILED,    Set.of());  // 终态（重试=新建任务，不复活旧任务）

    /** 是否为"运行中"状态（部分唯一索引与幂等判断用） */
    public boolean isRunning() {
        return this == PENDING || this == AGENTS || this == RENDERING || this == COMPILING;
    }

    /** 校验并返回新状态：非法迁移直接抛 409（宁可崩也不要脏状态） */
    public TaskStatus transitionTo(TaskStatus next) {
        if (!TRANSITIONS.getOrDefault(this, Set.of()).contains(next)) {
            throw new BizException(ErrorCode.CONFLICT,
                    String.format("非法状态迁移: %s -> %s", this, next));
        }
        return next;
    }
}
