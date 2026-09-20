package com.aiproject.credit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    /** 幂等查询：该幂等键是否已存在（已扣过） */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
