package com.aiproject.generator;

/**
 * 视频生成引擎抽象（可插拔，和 TaskQueue 同一设计思路）。
 * - provider=mock（默认）：无 key 也能跑通全流程，视频为占位演示 URL
 * - provider=seedance：火山方舟 Seedance 真实生成（需自行购买 API Key，见 .env.example）
 * 换任何厂商（可灵/即梦/Runway…）只需新增一个实现类 + 配置切 provider，业务代码零改动。
 */
public interface VideoGenerator {

    /** 提交生成任务，返回提供方任务 ID（后续 query 用） */
    String submit(GenerateRequest request);

    /** 查询生成进度（worker 每轮轮询调用） */
    GeneratorStatus query(String providerTaskId);
}
