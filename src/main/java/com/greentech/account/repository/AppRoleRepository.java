package com.greentech.account.repository;

import com.greentech.account.domain.AppRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByCode(String code);

    List<AppRole> findAllByCodeIn(Collection<String> codes);

    List<AppRole> findAllByOrderByIdAsc();
}
