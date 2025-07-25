package com.retail.smart.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class SecurityMonitorDTO {

    private List<SecurityEventEntry> events;
    private List<SecurityAlertEntry> alerts;

    public SecurityMonitorDTO() {
    }

    public SecurityMonitorDTO(List<SecurityEventEntry> events, List<SecurityAlertEntry> alerts) {
        this.events = events;
        this.alerts = alerts;
    }

    public List<SecurityEventEntry> getEvents() {
        return events;
    }

    public void setEvents(List<SecurityEventEntry> events) {
        this.events = events;
    }

    public List<SecurityAlertEntry> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<SecurityAlertEntry> alerts) {
        this.alerts = alerts;
    }

    public static class SecurityEventEntry {

        @NotBlank(message = "Camera ID is required")
        private String cameraId;

        @NotBlank(message = "Timestamp is required")
        private String timestamp;

        @NotBlank(message = "Detected behavior is required")
        private String detectedBehavior;

        public SecurityEventEntry() {
        }

        public SecurityEventEntry(String cameraId, String timestamp, String detectedBehavior) {
            this.cameraId = cameraId;
            this.timestamp = timestamp;
            this.detectedBehavior = detectedBehavior;
        }

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getDetectedBehavior() {
            return detectedBehavior;
        }

        public void setDetectedBehavior(String detectedBehavior) {
            this.detectedBehavior = detectedBehavior;
        }
    }

    public static class SecurityAlertEntry {

        @NotBlank(message = "Alert level is required")
        private String alertLevel;

        @NotBlank(message = "Message is required")
        private String message;

        @NotBlank(message = "Location is required")
        private String location;

        public SecurityAlertEntry() {
        }

        public SecurityAlertEntry(String alertLevel, String message, String location) {
            this.alertLevel = alertLevel;
            this.message = message;
            this.location = location;
        }

        public String getAlertLevel() {
            return alertLevel;
        }

        public void setAlertLevel(String alertLevel) {
            this.alertLevel = alertLevel;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
