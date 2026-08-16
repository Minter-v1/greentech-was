package com.greentech.payroll.repository;

import com.greentech.payroll.domain.PayItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayItemRepository extends JpaRepository<PayItem, Long> {

    Optional<PayItem> findByCode(String code);

    List<PayItem> findByActiveTrueOrderBySortOrderAsc();
}
