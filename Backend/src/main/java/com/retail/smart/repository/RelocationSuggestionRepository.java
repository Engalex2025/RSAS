package com.retail.smart.repository;

import com.retail.smart.entity.RelocationSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelocationSuggestionRepository extends JpaRepository<RelocationSuggestion, Long> {
    List<RelocationSuggestion> findByWeek(int week);
}
