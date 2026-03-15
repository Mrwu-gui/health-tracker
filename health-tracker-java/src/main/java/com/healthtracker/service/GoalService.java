package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.Goal;
import com.healthtracker.mapper.GoalMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoalService extends ServiceImpl<GoalMapper, Goal> {
    public List<Goal> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId));
    }

    public List<Goal> listByUserAndPeriod(Long userId, String period) {
        LambdaQueryWrapper<Goal> query = new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId);
        if (period != null && !period.isBlank()) {
            if ("day".equalsIgnoreCase(period)) {
                query.in(Goal::getPeriod, "day", "daily", "每日");
            } else if ("week".equalsIgnoreCase(period)) {
                query.in(Goal::getPeriod, "week", "weekly", "每周");
            } else {
                query.eq(Goal::getPeriod, period);
            }
        }
        return list(query);
    }
}
