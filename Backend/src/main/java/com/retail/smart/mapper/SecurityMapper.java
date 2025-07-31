package com.retail.smart.mapper;

import com.retail.smart.dto.SecurityAlertDTO;
import com.retail.smart.dto.SecurityMonitorDTO;
import com.retail.smart.dto.SecuritySummaryDTO;
import com.retail.smart.entity.SecurityEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SecurityMapper {

    public static SecurityAlertDTO toAlertDTO(SecurityEvent entity) {
        return new SecurityAlertDTO(
            entity.getId(),
            entity.getLocation(),
            entity.getAlertLevel(),
            entity.getMessage(),
            entity.getEventTime()
        );
    }

    public static SecurityMonitorDTO toMonitorDTO(SecurityEvent entity) {
        return new SecurityMonitorDTO(
            entity.getId(),
            entity.getCameraId(),
            entity.getDetectedBehavior(),
            entity.getLocation(),
            entity.getEventTime()
        );
    }

    public static SecuritySummaryDTO toSummaryDTO(
            long totalAlerts,
            List<String> topLocations,
            double averagePerDay,
            Map<String, Long> countsByLevel
    ) {
        SecuritySummaryDTO dto = new SecuritySummaryDTO();
        dto.setTotalAlerts(totalAlerts);
        dto.setTopLocations(topLocations);
        dto.setAveragePerDay(averagePerDay);
        dto.setCountsByLevel(countsByLevel);
        return dto;
    }

    public static List<SecurityAlertDTO> toAlertDTOList(List<SecurityEvent> entities) {
        return entities.stream()
                .map(SecurityMapper::toAlertDTO)
                .collect(Collectors.toList());
    }

    public static List<SecurityMonitorDTO> toEventDTOList(List<SecurityEvent> entities) {
        return entities.stream()
                .map(SecurityMapper::toMonitorDTO)
                .collect(Collectors.toList());
    }
}
