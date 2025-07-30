package com.retail.smart.repository;

import com.retail.smart.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SecurityMonitorRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByLocation(String location);

    @Query("SELECT a.location, COUNT(a) FROM SecurityAlert a GROUP BY a.location ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertsByLocation();
}
