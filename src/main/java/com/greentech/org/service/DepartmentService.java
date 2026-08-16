package com.greentech.org.service;

import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.org.domain.Department;
import com.greentech.org.dto.req.DepartmentCreateReq;
import com.greentech.org.dto.req.DepartmentUpdateReq;
import com.greentech.org.dto.res.DepartmentRes;
import com.greentech.org.dto.res.DepartmentTreeRes;
import com.greentech.org.repository.DepartmentRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 부서 등록·수정·조회 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentRes> findAll(boolean activeOnly) {
        List<Department> departments = activeOnly
                ? departmentRepository.findByActiveTrueOrderBySortOrderAscIdAsc()
                : departmentRepository.findAllByOrderBySortOrderAscIdAsc();
        return departments.stream().map(DepartmentRes::from).toList();
    }

    /**
     * 부서 계층 구조 조회
     *
     * NOTE: 부서 수가 수십 건 규모라 전체를 한 번에 읽어 메모리에서 트리 구성
     */
    @Transactional(readOnly = true)
    public List<DepartmentTreeRes> findTree() {
        List<DepartmentRes> flat = findAll(false);

        Map<Long, DepartmentTreeRes> nodes = new LinkedHashMap<>();
        for (DepartmentRes item : flat) {
            nodes.put(item.id(), DepartmentTreeRes.of(item));
        }

        List<DepartmentTreeRes> roots = new ArrayList<>();
        for (DepartmentRes item : flat) {
            DepartmentTreeRes node = nodes.get(item.id());
            DepartmentTreeRes parent = item.parentId() != null ? nodes.get(item.parentId()) : null;
            if (parent != null) {
                parent.children().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    public DepartmentRes findById(Long id) {
        return DepartmentRes.from(getOrThrow(id));
    }

    @Transactional
    public DepartmentRes create(DepartmentCreateReq request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CODE, "이미 사용 중인 부서코드입니다: " + request.code());
        }

        Department parent = resolveParent(request.parentId());
        Department department = Department.builder()
                .code(request.code())
                .name(request.name())
                .parent(parent)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .active(true)
                .build();

        return DepartmentRes.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentRes update(Long id, DepartmentUpdateReq request) {
        Department department = getOrThrow(id);

        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "자기 자신을 상위 부서로 지정할 수 없습니다");
        }

        department.setName(request.name());
        department.setParent(resolveParent(request.parentId()));
        if (request.sortOrder() != null) {
            department.setSortOrder(request.sortOrder());
        }
        if (request.active() != null) {
            department.setActive(request.active());
        }
        return DepartmentRes.from(department);
    }

    /**
     * 부서 비활성화
     *
     * NOTE: 사원·발령 이력이 참조하므로 물리 삭제 대신 active=false 처리
     */
    @Transactional
    public void deactivate(Long id) {
        Department department = getOrThrow(id);
        if (departmentRepository.existsByParentId(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "하위 부서가 있어 비활성화할 수 없습니다");
        }
        department.setActive(false);
    }

    private Department resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return departmentRepository.findById(parentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND, parentId));
    }

    private Department getOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND, id));
    }
}
