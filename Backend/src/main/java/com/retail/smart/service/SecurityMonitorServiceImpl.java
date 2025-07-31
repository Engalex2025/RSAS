package com.retail.smart.service;

import com.retail.smart.dto.SecurityAlertDTO;
import com.retail.smart.dto.SecurityMonitorDTO;
import com.retail.smart.dto.SecuritySummaryDTO;
import com.retail.smart.entity.SecurityEvent;
import com.retail.smart.grpc.security.SecurityMonitorGrpc;
import com.retail.smart.repository.SecurityMonitorRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityMonitorServiceImpl extends SecurityMonitorGrpc.SecurityMonitorImplBase {

    @Autowired
    private SecurityMonitorRepository eventRepository;

    @Scheduled(fixedRate = 21600000) // 6 hours
    public void generateRandomSecurityEvent() {
        String[] areas = {"A101", "B202", "C303", "D404"};
        String area = areas[new Random().nextInt(areas.length)];
        int hour = LocalDateTime.now().getHour();

        List<String> behaviors = new ArrayList<>();
        if (hour >= 0 && hour < 6) {
            behaviors.addAll(List.of("unauthorized access", "intruder", "loitering", "suspicious behavior"));
        } else if (hour >= 6 && hour < 12) {
            behaviors.addAll(List.of("wandering", "loitering", "delivery person waiting", "customer complaint"));
        } else if (hour >= 12 && hour < 18) {
            behaviors.addAll(List.of("crowding", "unusual gathering", "suspicious behavior", "customer altercation"));
        } else {
            behaviors.addAll(List.of("suspicious behavior", "theft attempt", "loitering", "unauthorized access"));
        }

        behaviors.addAll(List.of("normal activity", "camera obstruction", "maintenance visit"));

        String behavior = behaviors.get(new Random().nextInt(behaviors.size()));

        String level;
        if (behavior.contains("unauthorized") || behavior.contains("intruder") || behavior.contains("theft")) {
            level = "HIGH";
        } else if (behavior.contains("loitering") || behavior.contains("wandering") || behavior.contains("altercation")) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        String recommendation;
        switch (behavior) {
            case "unauthorized access", "intruder" -> recommendation = "Block access and alert security immediately.";
            case "suspicious behavior", "theft attempt" -> recommendation = "Monitor cameras and notify personnel.";
            case "crowding", "unusual gathering" -> recommendation = "Dispatch staff to manage area.";
            case "loitering", "wandering" -> recommendation = "Send patrol to investigate.";
            case "camera obstruction" -> recommendation = "Check camera hardware and visibility.";
            case "customer altercation" -> recommendation = "Send security to de-escalate the situation.";
            case "delivery person waiting" -> recommendation = "Verify credentials and provide escort.";
            case "customer complaint" -> recommendation = "Dispatch floor supervisor.";
            default -> recommendation = "No action needed.";
        }

        String message = switch (level) {
            case "HIGH" -> "⚠ Critical Alert: " + behavior;
            case "MEDIUM" -> "⚠ Unusual Activity: " + behavior;
            default -> "Info: " + behavior;
        };

        SecurityEvent event = new SecurityEvent();
        event.setCameraId(area + "_CAM");
        event.setLocation(area);
        event.setDetectedBehavior(behavior);
        event.setAlertLevel(level);
        event.setMessage(message + " | Recommendation: " + recommendation);
        event.setEventTime(LocalDateTime.now());

        eventRepository.save(event);

        System.out.println("[AUTO-GENERATED] " + behavior + " in " + area + " | Level: " + level);
    }

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
                eventEntity.setEventTime(LocalDateTime.now());

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

                eventEntity.setAlertLevel(alertLevel);
                eventEntity.setMessage(message);

                eventRepository.save(eventEntity);

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

    public List<SecurityAlertDTO> getAllAlerts() {
        return eventRepository.findAll().stream()
                .map(alert -> new SecurityAlertDTO(
                        alert.getId(),
                        alert.getLocation(),
                        alert.getAlertLevel(),
                        alert.getMessage(),
                        alert.getEventTime()
                ))
                .collect(Collectors.toList());
    }

    public List<SecurityAlertDTO> getFilteredAlerts(String level, String location) {
    return eventRepository.findAll().stream()
            .filter(alert -> (level == null || alert.getAlertLevel().equalsIgnoreCase(level)))
            .filter(alert -> {
                if (location == null) return true;
                String normalized = alert.getLocation().toUpperCase();
                return normalized.contains(location.toUpperCase());
            })
            .map(alert -> new SecurityAlertDTO(
                    alert.getId(),
                    alert.getLocation(),
                    alert.getAlertLevel(),
                    alert.getMessage(),
                    alert.getEventTime()
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
                        event.getEventTime()
                ))
                .collect(Collectors.toList());
    }

    public List<SecurityMonitorDTO> getFilteredEvents(String behavior, String cameraId) {
    return eventRepository.findAll().stream()
            .filter(event -> (behavior == null || event.getDetectedBehavior().equalsIgnoreCase(behavior)))
            .filter(event -> {
                if (cameraId == null) return true;
                String normalized = event.getCameraId().toUpperCase();
                return normalized.contains(cameraId.toUpperCase());
            })
            .map(event -> new SecurityMonitorDTO(
                    event.getId(),
                    event.getCameraId(),
                    event.getDetectedBehavior(),
                    event.getLocation(),
                    event.getEventTime()
            ))
            .collect(Collectors.toList());
}

    public Map<String, Long> getAlertCountsByLevel() {
        return eventRepository.findAll().stream()
                .collect(Collectors.groupingBy(SecurityEvent::getAlertLevel, Collectors.counting()));
    }

    public List<String> getTopLocations() {
        return eventRepository.findAll().stream()
                .collect(Collectors.groupingBy(SecurityEvent::getLocation, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public double getAverageAlertsPerDay() {
        Map<LocalDate, Long> alertsByDay = eventRepository.findAll().stream()
                .collect(Collectors.groupingBy(alert -> alert.getEventTime().toLocalDate(), Collectors.counting()));

        if (alertsByDay.isEmpty()) return 0.0;

        long total = alertsByDay.values().stream().mapToLong(Long::longValue).sum();
        return (double) total / alertsByDay.size();
    }

    public SecuritySummaryDTO getSecuritySummary() {
        SecuritySummaryDTO summary = new SecuritySummaryDTO();
        summary.setTotalAlerts(eventRepository.count());
        summary.setTopLocations(getTopLocations());
        summary.setAveragePerDay(getAverageAlertsPerDay());
        summary.setCountsByLevel(getAlertCountsByLevel());
        return summary;
    }
}
