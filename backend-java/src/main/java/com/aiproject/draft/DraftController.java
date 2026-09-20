package com.aiproject.draft;

import com.aiproject.common.ApiResponse;
import com.aiproject.draft.dto.DraftRequest;
import com.aiproject.draft.dto.DraftResponse;
import com.aiproject.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    /** 创建草稿 */
    @PostMapping
    public ApiResponse<DraftResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DraftRequest request) {
        return ApiResponse.ok(draftService.create(principal.getId(), request));
    }

    /** 我的草稿列表 */
    @GetMapping
    public ApiResponse<List<DraftResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(draftService.list(principal.getId()));
    }

    /** 草稿详情（仅自己） */
    @GetMapping("/{id}")
    public ApiResponse<DraftResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(draftService.detail(principal.getId(), id));
    }

    /** 更新草稿（仅自己，自动保存） */
    @PutMapping("/{id}")
    public ApiResponse<DraftResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id, @Valid @RequestBody DraftRequest request) {
        return ApiResponse.ok(draftService.update(principal.getId(), id, request));
    }

    /** 删除草稿（仅自己） */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        draftService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }
}
