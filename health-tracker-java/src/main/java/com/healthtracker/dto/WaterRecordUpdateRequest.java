package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WaterRecordUpdateRequest {
    @NotNull
    private Long id;

    @NotNull
    private Integer ml;

    @NotNull
    private LocalDateTime drinkTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMl() {
        return ml;
    }

    public void setMl(Integer ml) {
        this.ml = ml;
    }

    public LocalDateTime getDrinkTime() {
        return drinkTime;
    }

    public void setDrinkTime(LocalDateTime drinkTime) {
        this.drinkTime = drinkTime;
    }
}
