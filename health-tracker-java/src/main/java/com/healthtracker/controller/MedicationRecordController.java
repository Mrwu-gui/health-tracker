package com.healthtracker.controller;

import com.healthtracker.dto.MedicationRecordRequest;
import com.healthtracker.entity.MedicationRecord;
import com.healthtracker.service.MedicationRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medication/record")
public class MedicationRecordController {
    private final MedicationRecordService medicationRecordService;

    public MedicationRecordController(MedicationRecordService medicationRecordService) {
        this.medicationRecordService = medicationRecordService;
    }

    @PostMapping("/add")
    public MedicationRecord add(@Valid @RequestBody MedicationRecordRequest request) {
        MedicationRecord record = new MedicationRecord();
        record.setUserId(request.getUserId());
        record.setMedicationId(request.getMedicationId());
        record.setDate(request.getDate());
        record.setTime(request.getTime());
        record.setStatus(request.getStatus());
        medicationRecordService.save(record);
        return record;
    }
}
