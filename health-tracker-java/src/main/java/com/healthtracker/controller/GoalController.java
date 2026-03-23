package com.healthtracker.controller;

import com.healthtracker.dto.GoalRequest;
import com.healthtracker.dto.GoalUpdateRequest;
import com.healthtracker.entity.Goal;
import com.healthtracker.service.GoalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goal")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/add")
    public Goal add(@Valid @RequestBody GoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(request.getUserId());
        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(0);
        goal.setPeriod(request.getPeriod());
        goal.setStartDate(request.getStartDate() == null ? java.time.LocalDate.now() : request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        goal.setAiStrategy(request.getAiStrategy());
        goalService.save(goal);
        return goal;
    }

    @GetMapping("/list")
    public List<Goal> list(@RequestParam Long userId,
                           @RequestParam(required = false) String period) {
        return goalService.listByUserAndPeriod(userId, period);
    }

    @PostMapping("/update")
    public Goal update(@Valid @RequestBody GoalUpdateRequest request) {
        Goal goal = goalService.getById(request.getId());
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(request.getCurrentValue());
        goal.setPeriod(request.getPeriod());
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            goal.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        if (request.getAiStrategy() != null) {
            goal.setAiStrategy(request.getAiStrategy());
        }
        goalService.updateById(goal);
        return goal;
    }
}
