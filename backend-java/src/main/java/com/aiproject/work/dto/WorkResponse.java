package com.aiproject.work.dto;

import com.aiproject.work.Work;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** 作品响应 */
@Data
@Builder
public class WorkResponse {

    private Long id;
    private Long draftId;
    private Long taskId;
    private String title;
    private String videoUrl;
    private String coverUrl;
    private Integer version;
    private Instant createdAt;

    public static WorkResponse from(Work w) {
        return WorkResponse.builder()
                .id(w.getId())
                .draftId(w.getDraftId())
                .taskId(w.getTaskId())
                .title(w.getTitle())
                .videoUrl(w.getVideoUrl())
                .coverUrl(w.getCoverUrl())
                .version(w.getVersion())
                .createdAt(w.getCreatedAt())
                .build();
    }
}
