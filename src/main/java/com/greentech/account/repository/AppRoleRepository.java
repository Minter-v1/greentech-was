package com.greentech.account.repository;

import com.greentech.account.domain.AppRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByCode(String code);
}
