package com.retail.smart.repository;

import com.retail.smart.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    // Find all alerts for a specific location (camera or area)
    List<SecurityAlert> findByLocation(String location);

    // Count the number of alerts grouped by location (used for summary/heatmap)
    @Query("SELECT a.location, COUNT(a) FROM SecurityAlert a GROUP BY a.location ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertsByLocation();
}
