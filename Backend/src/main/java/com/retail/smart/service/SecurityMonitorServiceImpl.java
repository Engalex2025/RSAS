package com.retail.smart.service;

import com.retail.smart.dto.SecurityAlertDTO;
import com.retail.smart.dto.SecurityMonitorDTO;
import com.retail.smart.dto.SecuritySummaryDTO;
import com.retail.smart.entity.SecurityAlert;
import com.retail.smart.entity.SecurityEvent;
import com.retail.smart.grpc.security.SecurityMonitorGrpc;
import com.retail.smart.repository.SecurityAlertRepository;
import com.retail.smart.repository.SecurityMonitorRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityMonitorServiceImpl extends SecurityMonitorGrpc.SecurityMonitorImplBase {

    @Autowired
    private SecurityMonitorRepository eventRepository;

    @Autowired
    private SecurityAlertRepository alertRepository;

    // gRPC method
    @Override
    public StreamObserver<com.retail.smart.grpc.security.SecurityEvent> monitorSuspects(
            StreamObserver<com.retail.smart.grpc.security.SecurityAlert> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(com.retail.smart.grpc.security.SecurityEvent event) {
                SecurityEvent eventEntity = new SecurityEvent();
                eventEntity.setCameraId(event.getCameraId());
                eventEntity.setDetectedBehavior(event.getDetectedBehavior());
                eventEntity.setLocation(event.getCameraId());
                eventEntity.setTimestamp(LocalDateTime.now());
                eventRepository.save(eventEntity);

                String behavior = event.getDetectedBehavior().toLowerCase();
                String alertLevel;
                String message;

                if (behavior.contains("intruder") || behavior.contains("suspicious")) {
                    alertLevel = "HIGH";
                    message = "Critical behavior detected: " + behavior;
                } else if (behavior.contains("loitering") || behavior.contains("wandering")) {
                    alertLevel = "MEDIUM";
                    message = "Unusual behavior: " + behavior;
                } else {
                    alertLevel = "LOW";
                    message = "Non-critical activity detected.";
                }

                SecurityAlert alertEntity = new SecurityAlert();
                alertEntity.setAlertLevel(alertLevel);
                alertEntity.setMessage(message);
                alertEntity.setLocation(event.getCameraId());
                alertEntity.setIssuedAt(LocalDateTime.now());
                alertRepository.save(alertEntity);

                com.retail.smart.grpc.security.SecurityAlert alert = com.retail.smart.grpc.security.SecurityAlert.newBuilder()
                        .setAlertLevel(alertLevel)
                        .setMessage(message)
                        .setLocation(event.getCameraId())
                        .build();

                responseObserver.onNext(alert);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    // REST methods

    public List<SecurityAlertDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(alert -> new SecurityAlertDTO(
                        alert.getId(),
                        alert.getLocation(),
                        alert.getAlertLevel(),
                        alert.getMessage(),
                        alert.getIssuedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<SecurityAlertDTO> getFilteredAlerts(String level, String location) {
        return alertRepository.findAll().stream()
                .filter(alert -> (level == null || alert.getAlertLevel().equalsIgnoreCase(level)))
                .filter(alert -> (location == null || alert.getLocation().equalsIgnoreCase(location)))
                .map(alert -> new SecurityAlertDTO(
                        alert.getId(),
                        alert.getLocation(),
                        alert.getAlertLevel(),
                        alert.getMessage(),
                        alert.getIssuedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<SecurityMonitorDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> new SecurityMonitorDTO(
                        event.getId(),
                        event.getCameraId(),
                        event.getDetectedBehavior(),
                        event.getLocation(),
                        event.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getAlertCountsByLevel() {
        return alertRepository.findAll().stream()
                .collect(Collectors.groupingBy(SecurityAlert::getAlertLevel, Collectors.counting()));
    }

    public List<String> getTopLocations() {
        return alertRepository.findAll().stream()
                .collect(Collectors.groupingBy(SecurityAlert::getLocation, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public double getAverageAlertsPerDay() {
        Map<LocalDate, Long> alertsByDay = alertRepository.findAll().stream()
                .collect(Collectors.groupingBy(alert -> alert.getIssuedAt().toLocalDate(), Collectors.counting()));

        if (alertsByDay.isEmpty()) return 0.0;

        long total = alertsByDay.values().stream().mapToLong(Long::longValue).sum();
        return (double) total / alertsByDay.size();
    }

    public SecuritySummaryDTO getSecuritySummary() {
        SecuritySummaryDTO summary = new SecuritySummaryDTO();
        summary.setTotalAlerts(alertRepository.count());
        summary.setTopLocations(getTopLocations());
        summary.setAveragePerDay(getAverageAlertsPerDay());
        summary.setCountsByLevel(getAlertCountsByLevel());
        return summary;
    }
}
