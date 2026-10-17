package com.smartlib.repository;

import com.smartlib.domain.Recommendation;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByMemberIdAndGeneratedAtAfterOrderByScoreDesc(Long memberId, Instant generatedAfter);
}
