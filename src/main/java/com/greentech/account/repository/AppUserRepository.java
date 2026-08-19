package com.greentech.account.repository;

import com.greentech.account.domain.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = {"roles", "employee"})
    Optional<AppUser> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "employee"})
    Optional<AppUser> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"roles", "employee"})
    List<AppUser> findAllByOrderByUsernameAsc();

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(Long employeeId);
}
