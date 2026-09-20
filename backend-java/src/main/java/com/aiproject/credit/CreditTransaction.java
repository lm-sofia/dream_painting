package com.aiproject.credit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 额度流水：每笔扣费/充值一条记录。
 * 幂等键（idempotency_key）保证：同一任务只扣一次（重试不重复扣）。
 */
@Entity
@Table(name = "credit_transactions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_credit_tx_key", columnNames = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 关联任务 */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /** 变动量：扣费为负（-1），充值为正 */
    @Column(nullable = false)
    private Integer amount;

    /** 类型：TASK_CREATE / RECHARGE / MONTH_RESET */
    @Column(nullable = false, length = 20)
    private String type;

    /** 幂等键：如 "task:create:{taskId}" —— 唯一约束兜底，重复扣直接 409 */
    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
