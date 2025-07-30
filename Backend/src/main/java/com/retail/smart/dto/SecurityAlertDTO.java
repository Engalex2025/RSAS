package com.retail.smart.dto;

import java.time.LocalDateTime;

public class SecurityAlertDTO {

    private Long id;
    private String area;
    private String level;
    private String description;
    private LocalDateTime timestamp;

    public SecurityAlertDTO() {
    }

    public SecurityAlertDTO(Long id, String area, String level, String description, LocalDateTime timestamp) {
        this.id = id;
        this.area = area;
        this.level = level;
        this.description = description;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
