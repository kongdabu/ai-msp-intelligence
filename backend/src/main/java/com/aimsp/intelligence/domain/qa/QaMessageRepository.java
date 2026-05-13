package com.aimsp.intelligence.domain.qa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaMessageRepository extends JpaRepository<QaMessage, Long> {

    List<QaMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    // 이전 대화 3턴(6메시지) — 최신순으로 가져온 뒤 뒤집어서 시간순 정렬
    List<QaMessage> findTop6BySessionIdOrderByCreatedAtDesc(Long sessionId);
}
