package com.aimsp.intelligence.domain.source;

import com.aimsp.intelligence.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    // 전체 소스 목록
    @Transactional(readOnly = true)
    public List<SourceDto.Response> getAllSources() {
        return sourceRepository.findAll().stream()
                .map(SourceDto.Response::from)
                .collect(Collectors.toList());
    }

    // 활성 소스 목록
    @Transactional(readOnly = true)
    public List<Source> getActiveSources() {
        return sourceRepository.findByActiveTrue();
    }

    // 활성 소스 수
    @Transactional(readOnly = true)
    public long getActiveSourceCount() {
        return sourceRepository.findByActiveTrue().size();
    }

    // 소스 활성/비활성 토글
    @Transactional
    public SourceDto.Response toggleSource(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("소스를 찾을 수 없습니다: " + id));
        source.setActive(!source.getActive());
        return SourceDto.Response.from(sourceRepository.save(source));
    }

    // 소스 추가
    @Transactional
    public SourceDto.Response addSource(SourceDto.CreateRequest request) {
        if (sourceRepository.existsByUrl(request.getUrl())) {
            throw new IllegalArgumentException("이미 등록된 URL입니다: " + request.getUrl());
        }
        Source source = new Source();
        source.setName(request.getName());
        source.setUrl(request.getUrl());
        source.setType(request.getType());
        source.setCompetitor(request.getCompetitor());
        source.setActive(true);
        return SourceDto.Response.from(sourceRepository.save(source));
    }

    // 마지막 크롤링 시간 업데이트
    @Transactional
    public void updateLastCrawled(Long id) {
        sourceRepository.findById(id).ifPresent(source -> {
            source.setLastCrawledAt(java.time.LocalDateTime.now());
            source.setCrawlCount(source.getCrawlCount() + 1);
            sourceRepository.save(source);
        });
    }

    // 에러 카운트 증가
    @Transactional
    public void incrementErrorCount(Long id) {
        sourceRepository.findById(id).ifPresent(source -> {
            source.setErrorCount(source.getErrorCount() + 1);
            sourceRepository.save(source);
        });
    }
}
