package com.retail.smart.repository;

import com.retail.smart.entity.RelocationSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelocationSuggestionRepository extends JpaRepository<RelocationSuggestion, Long> {
    // You can add custom queries here later if needed
}
