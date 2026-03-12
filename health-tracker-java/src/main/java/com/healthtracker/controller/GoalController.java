package com.healthtracker.controller;

import com.healthtracker.dto.GoalRequest;
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
        goalService.save(goal);
        return goal;
    }

    @GetMapping("/list")
    public List<Goal> list(@RequestParam Long userId) {
        return goalService.listByUser(userId);
    }
}
