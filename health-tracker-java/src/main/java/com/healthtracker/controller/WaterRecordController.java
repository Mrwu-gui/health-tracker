package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthtracker.dto.WaterRecordRequest;
import com.healthtracker.dto.WaterRecordUpdateRequest;
import com.healthtracker.entity.WaterRecord;
import com.healthtracker.service.WaterRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water")
public class WaterRecordController {
    private final WaterRecordService waterRecordService;

    public WaterRecordController(WaterRecordService waterRecordService) {
        this.waterRecordService = waterRecordService;
    }

    @PostMapping("/add")
    public WaterRecord add(@Valid @RequestBody WaterRecordRequest request) {
        WaterRecord record = new WaterRecord();
        record.setUserId(request.getUserId());
        record.setMl(request.getMl());
        record.setDrinkTime(request.getDrinkTime());
        record.setCreatedAt(LocalDateTime.now());
        waterRecordService.save(record);
        return record;
    }

    @PostMapping("/update")
    public WaterRecord update(@Valid @RequestBody WaterRecordUpdateRequest request) {
        WaterRecord record = waterRecordService.getById(request.getId());
        if (record == null) {
            throw new IllegalArgumentException("饮水记录不存在");
        }
        record.setMl(request.getMl());
        record.setDrinkTime(request.getDrinkTime());
        waterRecordService.updateById(record);
        return record;
    }

    @PostMapping("/delete")
    public void delete(@RequestParam Long id) {
        waterRecordService.removeById(id);
    }

    @GetMapping("/list")
    public List<WaterRecord> list(@RequestParam Long userId,
                                  @RequestParam(required = false) String date) {
        if (date != null && !date.isBlank()) {
            return waterRecordService.listByUserAndDate(userId, LocalDate.parse(date));
        }
        return waterRecordService.list(new LambdaQueryWrapper<WaterRecord>()
            .eq(WaterRecord::getUserId, userId)
            .orderByDesc(WaterRecord::getDrinkTime)
            .last("limit 200"));
    }
}
