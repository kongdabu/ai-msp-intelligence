package com.aimsp.intelligence.domain.source;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findByActiveTrue();

    long countByActiveTrue();

    boolean existsByUrl(String url);
}
