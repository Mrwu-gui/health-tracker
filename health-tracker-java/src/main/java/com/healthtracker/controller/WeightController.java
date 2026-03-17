package com.healthtracker.controller;

import com.healthtracker.dto.WeightRecordRequest;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.WeightRecordService;
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
@RequestMapping("/api/weight")
public class WeightController {
    private final WeightRecordService weightRecordService;

    public WeightController(WeightRecordService weightRecordService) {
        this.weightRecordService = weightRecordService;
    }

    @PostMapping("/add")
    public WeightRecord add(@Valid @RequestBody WeightRecordRequest request) {
        LocalDate date = request.getDate() == null ? LocalDate.now() : request.getDate();
        List<WeightRecord> existing = weightRecordService.listByUserAndDate(request.getUserId(), date);
        WeightRecord record = existing.isEmpty() ? new WeightRecord() : existing.get(0);
        record.setUserId(request.getUserId());
        record.setWeight(request.getWeight());
        record.setBmi(request.getBmi());
        record.setDate(date);
        if (record.getId() == null) {
            weightRecordService.save(record);
        } else {
            weightRecordService.updateById(record);
        }
        return record;
    }

    @GetMapping("/list")
    public List<WeightRecord> list(@RequestParam Long userId, @RequestParam String date) {
        return weightRecordService.listByUserAndDate(userId, LocalDate.parse(date));
    }
}
