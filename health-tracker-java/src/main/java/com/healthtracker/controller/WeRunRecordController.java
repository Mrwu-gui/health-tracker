package com.healthtracker.controller;

import com.healthtracker.dto.WeRunRecordRequest;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.service.WeRunRecordService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/werun")
public class WeRunRecordController {
    private final WeRunRecordService weRunRecordService;

    public WeRunRecordController(WeRunRecordService weRunRecordService) {
        this.weRunRecordService = weRunRecordService;
    }

    @PostMapping("/add")
    public WeRunRecord add(@Valid @RequestBody WeRunRecordRequest request) {
        WeRunRecord record = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, request.getUserId())
            .eq(WeRunRecord::getRecordDate, request.getDate())
            .one();
        if (record == null) {
            record = new WeRunRecord();
            record.setUserId(request.getUserId());
            record.setRecordDate(request.getDate());
            record.setCreatedAt(LocalDateTime.now());
        }
        record.setSteps(request.getSteps());
        weRunRecordService.saveOrUpdate(record);
        return record;
    }
}
