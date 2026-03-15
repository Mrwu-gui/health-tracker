package com.healthtracker.controller;

import com.healthtracker.dto.PeriodRecordRequest;
import com.healthtracker.entity.PeriodRecord;
import com.healthtracker.service.PeriodRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/period")
public class PeriodRecordController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final PeriodRecordService periodRecordService;

    public PeriodRecordController(PeriodRecordService periodRecordService) {
        this.periodRecordService = periodRecordService;
    }

    @GetMapping("/list")
    public List<PeriodRecord> list(@RequestParam Long userId) {
        return periodRecordService.listByUser(userId);
    }

    @PostMapping("/add")
    public PeriodRecord add(@Valid @RequestBody PeriodRecordRequest request) {
        PeriodRecord record = new PeriodRecord();
        record.setUserId(request.getUserId());
        record.setStartDate(parseDate(request.getStartDate()));
        record.setEndDate(parseDate(request.getEndDate() == null || request.getEndDate().isBlank()
            ? request.getStartDate()
            : request.getEndDate()));
        record.setFlow(parseFlow(request.getFlow()));
        record.setNote(trim(request.getNote()));
        record.setCreatedAt(LocalDateTime.now());
        periodRecordService.save(record);
        return record;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE_FORMAT);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseFlow(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "light":
                return 1;
            case "medium":
                return 2;
            case "heavy":
                return 3;
            default:
                try {
                    return Integer.valueOf(normalized);
                } catch (NumberFormatException ex) {
                    return null;
                }
        }
    }
}
