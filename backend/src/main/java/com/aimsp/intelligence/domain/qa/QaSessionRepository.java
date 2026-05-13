package com.aimsp.intelligence.domain.qa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QaSessionRepository extends JpaRepository<QaSession, Long> {

    List<QaSession> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT s FROM QaSession s LEFT JOIN FETCH s.messages WHERE s.id = :id")
    Optional<QaSession> findByIdWithMessages(@Param("id") Long id);
}
