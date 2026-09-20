package com.aiproject.generator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟生成引擎（provider=mock，默认值）：
 * - 无 API Key 也能完整跑通"提交→轮询→完成"真实链路（契约测试/E2E/教学演示）
 * - 提交后约 6 秒返回"成功"，视频为占位演示 URL（与描述无关——真实生成请配 seedance）
 * - 与 SimulatedTaskWorker 的区别：它只存在于 test profile；本类服务于生产 profile 无 key 场景
 */
@Component
@ConditionalOnProperty(name = "ai.video.provider", havingValue = "mock", matchIfMissing = true)
public class MockVideoGenerator implements VideoGenerator {

    /** providerTaskId → 提交时刻（内存态；mock 仅演示用，真实引擎以模型侧状态为准） */
    private final Map<String, Instant> submittedAt = new ConcurrentHashMap<>();

    private static final long MOCK_DURATION_MS = 6000;

    @Override
    public String submit(GenerateRequest request) {
        String id = "mock-" + UUID.randomUUID();
        // 顺手清理 5 分钟前的旧记录（查询是幂等的，绝不删除进行中/刚完成的记录）
        Instant cutoff = Instant.now().minusSeconds(300);
        submittedAt.entrySet().removeIf(e -> e.getValue().isBefore(cutoff));
        submittedAt.put(id, Instant.now());
        return id;
    }

    @Override
    public GeneratorStatus query(String providerTaskId) {
        Instant at = submittedAt.get(providerTaskId);
        if (at == null) {
            return GeneratorStatus.failed("引擎任务不存在");
        }
        if (Instant.now().isBefore(at.plusMillis(MOCK_DURATION_MS))) {
            return GeneratorStatus.running("生成中…");
        }
        // 注意：不 remove —— 调用方（worker）会持续轮询直到任务终态，查询必须幂等
        // 占位视频；生产配置 seedance 后这里会返回真实模型直链
        return GeneratorStatus.succeeded("https://www.w3schools.com/html/mov_bbb.mp4");
    }
}
