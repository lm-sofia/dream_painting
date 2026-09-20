package com.aiproject.credit;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {

    /**
     * 悲观锁查询：SELECT ... FOR UPDATE —— 行锁直到事务提交才释放。
     * 并发扣减时，第二个请求在此等待，杜绝"余额检查通过但扣减时已超"的超扣问题。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CreditAccount c where c.userId = :userId")
    Optional<CreditAccount> findByUserIdForUpdate(@Param("userId") Long userId);

    Optional<CreditAccount> findByUserId(Long userId);
}
