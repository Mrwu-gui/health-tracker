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
}
