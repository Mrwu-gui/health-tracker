package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WaterRecordRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Integer ml;

    @NotNull
    private LocalDateTime drinkTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
