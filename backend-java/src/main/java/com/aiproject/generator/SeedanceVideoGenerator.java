package com.aiproject.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 火山方舟 Seedance 文生视频适配器（provider=seedance）。
 *
 * 【密钥安全铁律】本类绝不硬编码 Key：
 *  - Key 只从环境变量注入（.env → 启动脚本 → ${ARK_API_KEY}）
 *  - .env 已被 .gitignore 排除，仓库只有占位模板 .env.example
 *  - 别人使用本项目：复制 .env.example 为 .env，填自己的 Key 即可，代码零改动
 *
 * 【API 流程】火山方舟为异步任务模型：
 *  submit：POST /api/v3/content/generation/tasks  → 返回任务 id
 *  query ：GET  /api/v3/content/generation/tasks/{id} → status + video_url
 *  （开通/价格/限额见火山引擎控制台；Key 建议开"限额"防止被盗刷）
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.video.provider", havingValue = "seedance")
public class SeedanceVideoGenerator implements VideoGenerator {

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final RestClient client;

    public SeedanceVideoGenerator(
            @Value("${ai.video.seedance.base-url}") String baseUrl,
            @Value("${ai.video.seedance.api-key}") String apiKey,
            @Value("${ai.video.seedance.model}") String model,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** 启动即校验：配了 seedance 却没配 Key → 直接启动失败（fail fast），避免运行时才发现白花钱白等 */
    @PostConstruct
    public void validate() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("""
                [AI 视频] provider=seedance 但未配置 ARK_API_KEY。
                请复制 backend-java/.env.example 为 backend-java/.env 并填入自己的 Key，
                或改用默认 mock（AI_VIDEO_PROVIDER=mock）。""");
        }
        log.info("[AI 视频] Seedance 引擎就绪，model={}，baseUrl={}", model, baseUrl);
    }

    @Override
    public String submit(GenerateRequest request) {
        try {
            // 组装方舟 API 请求体：内容数组 + 画质/时长/随机种子（换 seed 出不同结果）
            String ratioHint = switch (request.ratio()) {
                case "16:9" -> "横屏 16:9";
                case "1:1" -> "方形 1:1";
                default -> "竖屏 9:16";
            };
            String prompt = "【" + ratioHint + "】【风格:" + request.styleName() + "】" + request.prompt();

            Map<String, Object> body = Map.of(
                    "model", model,
                    "content", java.util.List.of(Map.of("type", "text", "text", prompt)),
                    "resolution", "1080p",
                    "duration", Math.max(5, request.durationSeconds()), // 模型下限 5 秒
                    "seed", ThreadLocalRandom.current().nextInt(1_000_000_000));

            JsonNode resp = client.post()
                    .uri("/content/generation/tasks")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsBytes(body))
                    .retrieve()
                    .body(JsonNode.class);

            String taskId = resp.path("id").asText();
            log.info("[AI 视频] 提交成功 draft={} providerTaskId={}", request.draftId(), taskId);
            return taskId;
        } catch (Exception e) {
            log.error("[AI 视频] 提交失败 draft={}", request.draftId(), e);
            throw new IllegalStateException("生成引擎提交失败: " + e.getMessage(), e);
        }
    }

    @Override
    public GeneratorStatus query(String providerTaskId) {
        try {
            JsonNode resp = client.get()
                    .uri("/content/generation/tasks/{id}", providerTaskId)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            String status = resp.path("status").asText();
            return switch (status) {
                case "succeeded" -> GeneratorStatus.succeeded(resp.path("content").path("video_url").asText());
                case "failed" -> GeneratorStatus.failed(resp.path("error").path("message").asText("生成失败"));
                default -> GeneratorStatus.running("生成中(" + status + ")…");
            };
        } catch (Exception e) {
            log.error("[AI 视频] 查询失败 providerTaskId={}", providerTaskId, e);
            return GeneratorStatus.failed("引擎查询失败: " + e.getMessage());
        }
    }
}
