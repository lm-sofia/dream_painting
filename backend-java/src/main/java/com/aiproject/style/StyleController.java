package com.aiproject.style;

import com.aiproject.common.ApiResponse;
import com.aiproject.style.dto.StyleRequest;
import com.aiproject.style.dto.StyleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/styles")
@RequiredArgsConstructor
public class StyleController {

    private final StyleService styleService;

    /** 公开：风格库列表（游客可看，获客入口） */
    @GetMapping
    public ApiResponse<List<StyleResponse>> list() {
        return ApiResponse.ok(styleService.list());
    }

    /** 公开：风格详情 */
    @GetMapping("/{id}")
    public ApiResponse<StyleResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(styleService.detail(id));
    }

    /** 管理端：新增风格（仅 ADMIN） */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StyleResponse> create(@Valid @RequestBody StyleRequest request) {
        return ApiResponse.ok(styleService.create(request));
    }

    /** 管理端：更新风格（仅 ADMIN） */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StyleResponse> update(@PathVariable Long id, @Valid @RequestBody StyleRequest request) {
        return ApiResponse.ok(styleService.update(id, request));
    }

    /** 管理端：下架风格（仅 ADMIN，软删除） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        styleService.delete(id);
        return ApiResponse.ok(null);
    }
}
