package com.aiproject.generator;

/**
 * 生成引擎请求（worker 组装，真实模型调用只依赖本对象）。
 * 核心教学点：描述（idea）与风格（styleName/模板）在这里真正进入生成链路——
 * 此前第 4-6 课它们只存在数据库里，模拟 worker 从不读取。
 */
public record GenerateRequest(
        Long userId,
        Long draftId,
        String prompt,       // 组装后的最终提示词（风格模板填充用户创意）
        String styleName,    // 风格名（调试/打标用）
        int durationSeconds, // 目标时长（模型支持范围内）
        String ratio         // 画面比例 9:16 / 16:9 / 1:1
) {
}
