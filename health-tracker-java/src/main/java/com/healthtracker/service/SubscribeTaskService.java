package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.mapper.SubscribeTaskMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SubscribeTaskService extends ServiceImpl<SubscribeTaskMapper, SubscribeTask> {
    public List<SubscribeTask> listDue(LocalDateTime now, int limit) {
        LambdaQueryWrapper<SubscribeTask> query = new LambdaQueryWrapper<>();
        query.eq(SubscribeTask::getStatus, 0)
            .le(SubscribeTask::getSendTime, now)
            .orderByAsc(SubscribeTask::getSendTime)
            .last("limit " + limit);
        return list(query);
    }
}
