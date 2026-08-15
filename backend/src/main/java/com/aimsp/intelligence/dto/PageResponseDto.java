package com.aimsp.intelligence.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Spring Data 내부 Page 구현을 외부 API에 노출하지 않기 위한 고정 페이지 응답 형식이다.
 */
public record PageResponseDto<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getSize(), page.getNumber());
    }
}
