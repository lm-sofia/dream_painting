package com.aiproject.style;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StyleRepository extends JpaRepository<Style, Long> {

    /** 上架列表（公开接口用）：按 sortOrder 升序 */
    List<Style> findByActiveTrueOrderBySortOrderAsc();

    Optional<Style> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);
}
