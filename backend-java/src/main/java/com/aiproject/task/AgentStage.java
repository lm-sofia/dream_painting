package com.aiproject.task;

/**
 * 7 智能体创作流水线（PRD §4.4）。
 * 顺序：编剧 → 角色 → 场景 → 动画 → 剪辑 → 音效 → 导演
 * targetProgress：该智能体完成时任务的总进度（用于前端总进度条）
 */
public enum AgentStage {

    SCREENWRITER("编剧", 15),
    CHARACTER("角色", 30),
    SCENE("场景", 45),
    ANIMATION("动画", 60),
    EDITING("剪辑", 72),
    SOUND("音效", 85),
    DIRECTOR("导演", 95);

    private final String label;
    private final int targetProgress;

    AgentStage(String label, int targetProgress) {
        this.label = label;
        this.targetProgress = targetProgress;
    }

    public String getLabel() {
        return label;
    }

    public int getTargetProgress() {
        return targetProgress;
    }

    /** 流水线下一智能体（导演之后返回 null = 进入渲染阶段） */
    public AgentStage next() {
        AgentStage[] all = values();
        int idx = ordinal();
        return idx + 1 < all.length ? all[idx + 1] : null;
    }
}
