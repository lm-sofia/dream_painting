package com.aiproject.draft;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import com.aiproject.draft.dto.DraftRequest;
import com.aiproject.draft.dto.DraftResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DraftService {

    private final DraftRepository draftRepository;

    /** 创建草稿：userId 从登录态注入，不信任客户端传值 */
    @Transactional
    public DraftResponse create(Long userId, DraftRequest request) {
        Draft draft = new Draft();
        draft.setUserId(userId);
        applyRequest(draft, request);
        return DraftResponse.from(draftRepository.save(draft));
    }

    /** 我的草稿列表 */
    public List<DraftResponse> list(Long userId) {
        return draftRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream().map(DraftResponse::from).toList();
    }

    /** 草稿详情（仅自己） */
    public DraftResponse detail(Long userId, Long id) {
        return DraftResponse.from(findOwned(userId, id));
    }

    /** 更新草稿（仅自己）：防抖自动保存的落点 */
    @Transactional
    public DraftResponse update(Long userId, Long id, DraftRequest request) {
        Draft draft = findOwned(userId, id);
        applyRequest(draft, request);
        return DraftResponse.from(draftRepository.save(draft));
    }

    /** 删除草稿（仅自己） */
    @Transactional
    public void delete(Long userId, Long id) {
        Draft draft = findOwned(userId, id);
        draftRepository.delete(draft);
    }

    /**
     * 越权守卫：查 (id, userId) 双条件，找不到统一 404。
     * 注意：不返回 403——403 等于告诉攻击者"这个 id 存在但属于别人"（信息泄露）
     */
    private Draft findOwned(Long userId, Long id) {
        return draftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "草稿不存在"));
    }

    private void applyRequest(Draft draft, DraftRequest request) {
        draft.setTitle(request.getTitle());
        draft.setIdea(request.getIdea());
        draft.setStyleId(request.getStyleId());
        if (request.getDuration() != null) {
            draft.setDuration(request.getDuration());
        }
        if (request.getRatio() != null) {
            draft.setRatio(request.getRatio());
        }
        if (request.getVoiceover() != null) {
            draft.setVoiceover(request.getVoiceover());
        }
    }
}
