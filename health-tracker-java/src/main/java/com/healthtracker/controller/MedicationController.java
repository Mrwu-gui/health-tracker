package com.healthtracker.controller;

import com.healthtracker.dto.MedicationRequest;
import com.healthtracker.dto.MedicationUpdateRequest;
import com.healthtracker.entity.Medication;
import com.healthtracker.service.MedicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medication")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/add")
    public Medication add(@Valid @RequestBody MedicationRequest request) {
        Medication medication = new Medication();
        medication.setUserId(request.getUserId());
        medication.setDrugName(request.getDrugName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setRemindTime(request.getRemindTime());
        medication.setStock(request.getStock());
        medication.setStockThreshold(request.getStockThreshold());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setNotes(request.getNotes());
        medicationService.save(medication);
        return medication;
    }

    @GetMapping("/list")
    public List<Medication> list(@RequestParam Long userId) {
        return medicationService.listByUser(userId);
    }

    @PostMapping("/update")
    public Medication update(@Valid @RequestBody MedicationUpdateRequest request) {
        Medication medication = medicationService.getById(request.getId());
        if (medication == null) {
            throw new IllegalArgumentException("药物不存在");
        }
        medication.setDrugName(request.getDrugName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setRemindTime(request.getRemindTime());
        medication.setStock(request.getStock());
        medication.setStockThreshold(request.getStockThreshold());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setNotes(request.getNotes());
        medicationService.updateById(medication);
        return medication;
    }
}
