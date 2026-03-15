package com.healthtracker.controller;

import com.healthtracker.dto.DietRecordRequest;
import com.healthtracker.entity.DietRecord;
import com.healthtracker.service.DietRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diet")
public class DietController {
    private final DietRecordService dietRecordService;

    public DietController(DietRecordService dietRecordService) {
        this.dietRecordService = dietRecordService;
    }

    @PostMapping("/add")
    public DietRecord add(@Valid @RequestBody DietRecordRequest request) {
        DietRecord record = new DietRecord();
        record.setUserId(request.getUserId());
        record.setMealType(request.getMealType());
        record.setFoodName(request.getFoodName());
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setCarbs(request.getCarbs());
        record.setFat(request.getFat());
        record.setNote(request.getNote());
        record.setDate(request.getDate());
        dietRecordService.save(record);
        return record;
    }

    @GetMapping("/list")
    public List<DietRecord> list(@RequestParam Long userId, @RequestParam String date) {
        return dietRecordService.listByUserAndDate(userId, LocalDate.parse(date));
    }
}
