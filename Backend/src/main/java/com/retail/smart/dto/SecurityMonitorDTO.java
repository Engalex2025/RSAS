package com.retail.smart.dto;

import java.time.LocalDateTime;

public class SecurityMonitorDTO {

    private Long id;
    private String area;
    private String eventType;
    private String notes;
    private LocalDateTime timestamp;

    public SecurityMonitorDTO() {
    }

    public SecurityMonitorDTO(Long id, String area, String eventType, String notes, LocalDateTime timestamp) {
        this.id = id;
        this.area = area;
        this.eventType = eventType;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
