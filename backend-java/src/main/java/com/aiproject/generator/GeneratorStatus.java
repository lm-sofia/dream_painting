package com.aiproject.generator;

/**
 * 生成引擎查询结果（worker 每轮轮询拿到的最新状态）。
 */
public record GeneratorStatus(
        Status status,
        String message,     // 模型侧文案（如"排队中""生成中 50%"），透传进任务 stageMessage
        String videoUrl     // status=SUCCEEDED 时有值（模型/对象存储的直链）
) {
    public enum Status { SUBMITTED, RUNNING, SUCCEEDED, FAILED }

    public static GeneratorStatus running(String message) {
        return new GeneratorStatus(Status.RUNNING, message, null);
    }

    public static GeneratorStatus succeeded(String videoUrl) {
        return new GeneratorStatus(Status.SUCCEEDED, "生成成功", videoUrl);
    }

    public static GeneratorStatus failed(String message) {
        return new GeneratorStatus(Status.FAILED, message, null);
    }
}
