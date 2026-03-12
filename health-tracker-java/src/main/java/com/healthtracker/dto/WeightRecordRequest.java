package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class WeightRecordRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Double weight;

    private Double bmi;

    @NotNull
    private LocalDate date;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
