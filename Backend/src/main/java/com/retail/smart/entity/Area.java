package com.retail.smart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "areas")
public class Area {

    @Id
    @NotBlank(message = "Area code is required")
    private String code;

    public Area() {
    }

    public Area(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
