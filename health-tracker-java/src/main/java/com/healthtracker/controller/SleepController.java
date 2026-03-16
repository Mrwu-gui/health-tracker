package com.healthtracker.controller;

import com.healthtracker.dto.SleepRecordRequest;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.service.SleepRecordService;
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
@RequestMapping("/api/sleep")
public class SleepController {
    private final SleepRecordService sleepRecordService;

    public SleepController(SleepRecordService sleepRecordService) {
        this.sleepRecordService = sleepRecordService;
    }

    @PostMapping("/add")
    public SleepRecord add(@Valid @RequestBody SleepRecordRequest request) {
        SleepRecord record = new SleepRecord();
        record.setUserId(request.getUserId());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        } else if (request.getStartTime() != null) {
            record.setRecordDate(request.getStartTime().toLocalDate());
        }
        record.setDeepSleepMinutes(request.getDeepSleepMinutes());
        record.setLightSleepMinutes(request.getLightSleepMinutes());
        record.setQuality(request.getQuality());
        record.setRoutine(request.getRoutine());
        sleepRecordService.save(record);
        return record;
    }

    @GetMapping("/list")
    public List<SleepRecord> list(@RequestParam Long userId, @RequestParam String date) {
        return sleepRecordService.listByUserAndDate(userId, LocalDate.parse(date));
    }
}
