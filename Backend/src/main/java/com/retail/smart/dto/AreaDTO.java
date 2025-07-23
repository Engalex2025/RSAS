package com.retail.smart.dto;

import jakarta.validation.constraints.NotBlank;

public class AreaDTO {

    @NotBlank(message = "Area code is required")
    private String code;

    public AreaDTO() {
    }

    public AreaDTO(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
