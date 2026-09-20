package com.aiproject.task;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis Stream 队列（生产实现）。
 * 入队：XADD gen_tasks * taskId {id}；消费：XRANGE + XDEL（简单队列语义）。
 * 生产进阶（第 5 课）：升级 XREADGROUP 消费组 + XPENDING 重试，保证 at-least-once。
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisTaskQueue implements TaskQueue {

    private static final String STREAM = "gen_tasks";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void enqueue(Long taskId) {
        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(Map.of("taskId", String.valueOf(taskId)))
                .withStreamKey(STREAM);
        redisTemplate.opsForStream().add(record);
    }

    @Override
    public Optional<Long> poll() {
        // 从头读取整个区间（返回按 id 升序），只处理第一条
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .range(STREAM,
                        Range.from(Bound.inclusive("-")).to(Bound.inclusive("+")));
        if (records == null || records.isEmpty()) {
            return Optional.empty();
        }
        MapRecord<String, Object, Object> first = records.get(0);
        Object taskId = first.getValue().get("taskId");
        if (taskId == null) {
            return Optional.empty();
        }
        // 消费后删除该条，避免重复处理（简化版；生产应使用消费组 + 确认机制）
        redisTemplate.opsForStream().delete(STREAM, first.getId());
        return Optional.of(Long.valueOf(taskId.toString()));
    }
}
