package com.aiproject.draft.dto;

import com.aiproject.draft.Draft;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class DraftResponse {

    private Long id;
    private Long userId;
    private String title;
    private String idea;
    private Long styleId;
    private Integer duration;
    private String ratio;
    private Boolean voiceover;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static DraftResponse from(Draft draft) {
        return new DraftResponse(
                draft.getId(), draft.getUserId(), draft.getTitle(), draft.getIdea(),
                draft.getStyleId(), draft.getDuration(), draft.getRatio(), draft.getVoiceover(),
                draft.getStatus(), draft.getCreatedAt(), draft.getUpdatedAt());
    }
}
