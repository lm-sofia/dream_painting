package com.aiproject.draft.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DraftRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    @Size(max = 2000, message = "创意最长 2000 字")
    private String idea;

    private Long styleId;

    /** 时长：15 / 30 / 60 秒（边界校验：客户端只给三档，服务端仍要兜底） */
    @Min(value = 5, message = "时长最短 5 秒")
    @Max(value = 120, message = "时长最长 120 秒")
    private Integer duration = 30;

    /** 画面比例：只允许三档 */
    @Pattern(regexp = "^(9:16|16:9|1:1)$", message = "比例只支持 9:16 / 16:9 / 1:1")
    private String ratio = "9:16";

    private Boolean voiceover = true;
}
