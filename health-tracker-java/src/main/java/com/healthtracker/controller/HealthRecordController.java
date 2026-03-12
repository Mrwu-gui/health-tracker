package com.healthtracker.controller;

import com.healthtracker.dto.HealthRecordRequest;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.service.HealthRecordService;
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
@RequestMapping("/api/health-record")
public class HealthRecordController {
    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PostMapping("/add")
    public HealthRecord add(@Valid @RequestBody HealthRecordRequest request) {
        HealthRecord record = new HealthRecord();
        record.setUserId(request.getUserId());
        record.setSystolic(request.getSystolic());
        record.setDiastolic(request.getDiastolic());
        record.setHeartRate(request.getHeartRate());
        record.setDate(request.getDate());
        healthRecordService.save(record);
        return record;
    }

    @GetMapping("/list")
    public List<HealthRecord> list(@RequestParam Long userId, @RequestParam String date) {
        return healthRecordService.listByUserAndDate(userId, LocalDate.parse(date));
    }
}
