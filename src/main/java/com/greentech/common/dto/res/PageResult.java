package com.greentech.common.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

// NOTE: Page 직접 노출 시 버전별로 직렬화 구조가 달라져 래핑
@Schema(description = "페이지 응답")
public record PageResult<T>(
        @Schema(description = "현재 페이지 내용")
        List<T> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 건수", example = "80")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "4")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean last) {

    public static <E, T> PageResult<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
