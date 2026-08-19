package com.greentech.org.service;

import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.org.domain.JobPosition;
import com.greentech.org.dto.req.JobPositionCreateReq;
import com.greentech.org.dto.req.JobPositionPatchReq;
import com.greentech.org.dto.res.JobPositionRes;
import com.greentech.org.repository.JobPositionRepository;
import java.util.List;
import java.util.function.Consumer;
import org.openapitools.jackson.nullable.JsonNullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 직위 등록·조회 */
@Service
@RequiredArgsConstructor
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;

    @Transactional(readOnly = true)
    public List<JobPositionRes> findAll(boolean activeOnly) {
        return jobPositionRepository.findAllByOrderByLevelNoAsc().stream()
                .filter(position -> !activeOnly || position.isActive())
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
    public JobPositionRes patch(Long id, JobPositionPatchReq request) {
        JobPosition position = getOrThrow(id);

        apply(request.name(), position::setName);
        apply(request.levelNo(), position::setLevelNo);
        apply(request.active(), position::setActive);

        return JobPositionRes.from(position);
    }

    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActive(false);
    }

    private <T> void apply(JsonNullable<T> field, Consumer<T> setter) {
        if (field != null && field.isPresent()) {
            setter.accept(field.get());
        }
    }

    private JobPosition getOrThrow(Long id) {
        return jobPositionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.POSITION_NOT_FOUND, id));
    }
}
