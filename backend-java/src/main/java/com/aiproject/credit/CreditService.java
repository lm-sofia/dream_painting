package com.aiproject.credit;

import com.aiproject.common.BizException;
import com.aiproject.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 额度扣减服务。
 * 事务 + 悲观锁 + 幂等键 三重保障，任何一环失守都不会超扣。
 */
@Service
@RequiredArgsConstructor
public class CreditService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final CreditAccountRepository accountRepository;
    private final CreditTransactionRepository transactionRepository;

    /**
     * 扣减 1 次创作额度（随任务创建一起，同事务）。
     * @return true=扣费成功；false=幂等命中（该任务已扣过，视为成功）
     */
    @Transactional
    public boolean deduct(Long userId, Long taskId) {
        String idempotencyKey = "task:create:" + taskId;

        // ① 幂等检查：同一任务只扣一次（重试/重复提交直接返回成功，不重复扣）
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return false;
        }

        // ② 悲观锁：SELECT ... FOR UPDATE —— 并发扣费在此排队，杜绝超扣
        CreditAccount account = accountRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createAccount(userId));

        // 月重置：跨月自动恢复月度配额（完整版=定时任务批量重置，此处懒重置）
        String currentMonth = LocalDate.now().format(MONTH_FMT);
        if (!currentMonth.equals(account.getBillingMonth())) {
            account.setBalance(account.getMonthlyQuota());
            account.setBillingMonth(currentMonth);
        }

        // ③ 余额校验 + 扣减
        if (account.getBalance() <= 0) {
            throw new BizException(ErrorCode.INSUFFICIENT_QUOTA,
                    "本月创作额度已用完（免费 10 次/月），升级会员或下月再来");
        }
        account.setBalance(account.getBalance() - 1);
        accountRepository.save(account);

        // ④ 记流水（幂等键唯一约束 = 最后一道闸，应用层漏判时 DB 拒绝）
        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        tx.setTaskId(taskId);
        tx.setAmount(-1);
        tx.setType("TASK_CREATE");
        tx.setIdempotencyKey(idempotencyKey);
        transactionRepository.save(tx);

        return true;
    }

    /** 懒创建账户：首次扣费时建（默认免费 10 次/月） */
    private CreditAccount createAccount(Long userId) {
        CreditAccount account = new CreditAccount();
        account.setUserId(userId);
        account.setBalance(10);
        account.setMonthlyQuota(10);
        account.setBillingMonth(LocalDate.now().format(MONTH_FMT));
        try {
            return accountRepository.save(account);
        } catch (Exception e) {
            // 并发创建撞唯一约束：重查已有账户（罕见路径，兜底即可）
            return accountRepository.findByUserId(userId)
                    .orElseThrow(() -> new BizException(ErrorCode.INTERNAL_ERROR, "额度账户初始化失败"));
        }
    }
}
