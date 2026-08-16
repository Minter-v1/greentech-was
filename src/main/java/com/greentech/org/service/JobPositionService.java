package com.greentech.org.service;

import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.org.domain.JobPosition;
import com.greentech.org.dto.req.JobPositionCreateReq;
import com.greentech.org.dto.res.JobPositionRes;
import com.greentech.org.repository.JobPositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 직위 등록·조회 */
@Service
@RequiredArgsConstructor
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;

    @Transactional(readOnly = true)
    public List<JobPositionRes> findAll() {
        return jobPositionRepository.findAllByOrderByLevelNoAsc().stream()
                .map(JobPositionRes::from)
                .toList();
    }

    @Transactional
    public JobPositionRes create(JobPositionCreateReq request) {
        if (jobPositionRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CODE, "이미 사용 중인 직위코드입니다: " + request.code());
        }

        JobPosition position = JobPosition.builder()
                .code(request.code())
                .name(request.name())
                .levelNo(request.levelNo())
                .active(true)
                .build();

        return JobPositionRes.from(jobPositionRepository.save(position));
    }

    @Transactional
    public void deactivate(Long id) {
        JobPosition position = jobPositionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.POSITION_NOT_FOUND, id));
        position.setActive(false);
    }
}
