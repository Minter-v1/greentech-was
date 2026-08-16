package com.greentech.account.repository;

import com.greentech.account.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // NOTE: 로그인 직후 토큰 클레임 구성에 roles/employee 필요 - N+1 방지용 EntityGraph
    @EntityGraph(attributePaths = {"roles", "employee"})
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
