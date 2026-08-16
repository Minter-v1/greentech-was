package com.greentech.org.repository;

import com.greentech.org.domain.JobPosition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

    Optional<JobPosition> findByCode(String code);

    boolean existsByCode(String code);

    List<JobPosition> findAllByOrderByLevelNoAsc();
}
