package com.aiproject.credit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 创作额度账户：每个用户一行。
 * 设计：免费用户每月 10 次；会员/充值走 balance 增量（后续课）。
 * 扣减用悲观锁（Repository @Lock(PESSIMISTIC_WRITE)）防并发超扣。
 */
@Entity
@Table(name = "credit_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_credit_accounts_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CreditAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 当前可用额度 */
    @Column(nullable = false)
    private Integer balance = 0;

    /** 每月基础配额（免费 10 次；会员在月重置任务中提高） */
    @Column(name = "monthly_quota", nullable = false)
    private Integer monthlyQuota = 10;

    /** 当前账单月（yyyy-MM，月重置任务按此判断） */
    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
