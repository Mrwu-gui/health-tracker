package com.healthtracker.controller;

import com.healthtracker.dto.ExerciseRecordRequest;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.service.ExerciseRecordService;
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
@RequestMapping("/api/exercise")
public class ExerciseController {
    private final ExerciseRecordService exerciseRecordService;

    public ExerciseController(ExerciseRecordService exerciseRecordService) {
        this.exerciseRecordService = exerciseRecordService;
    }

    @PostMapping("/add")
    public ExerciseRecord add(@Valid @RequestBody ExerciseRecordRequest request) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(request.getUserId());
        record.setType(request.getType());
        record.setSteps(request.getSteps());
        record.setDuration(request.getDuration());
        record.setCalories(request.getCalories());
        record.setDate(request.getDate());
        exerciseRecordService.save(record);
        return record;
    }

    @GetMapping("/list")
    public List<ExerciseRecord> list(@RequestParam Long userId, @RequestParam String date) {
        return exerciseRecordService.listByUserAndDate(userId, LocalDate.parse(date));
    }
}
