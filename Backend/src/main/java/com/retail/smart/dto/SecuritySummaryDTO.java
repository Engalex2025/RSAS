package com.retail.smart.dto;

import java.util.List;
import java.util.Map;

public class SecuritySummaryDTO {

    private long totalAlerts;
    private List<String> topLocations;
    private double averagePerDay;
    private Map<String, Long> countsByLevel;

    public SecuritySummaryDTO() {
    }

    public SecuritySummaryDTO(long totalAlerts, List<String> topLocations, double averagePerDay, Map<String, Long> countsByLevel) {
        this.totalAlerts = totalAlerts;
        this.topLocations = topLocations;
        this.averagePerDay = averagePerDay;
        this.countsByLevel = countsByLevel;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public List<String> getTopLocations() {
        return topLocations;
    }

    public void setTopLocations(List<String> topLocations) {
        this.topLocations = topLocations;
    }

    public double getAveragePerDay() {
        return averagePerDay;
    }

    public void setAveragePerDay(double averagePerDay) {
        this.averagePerDay = averagePerDay;
    }

    public Map<String, Long> getCountsByLevel() {
        return countsByLevel;
    }

    public void setCountsByLevel(Map<String, Long> countsByLevel) {
        this.countsByLevel = countsByLevel;
    }
}
