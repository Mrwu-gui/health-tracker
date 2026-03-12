package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.mapper.SleepRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SleepRecordService extends ServiceImpl<SleepRecordMapper, SleepRecord> {
    public List<SleepRecord> listByUserAndDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        LambdaQueryWrapper<SleepRecord> query = new LambdaQueryWrapper<>();
        query.eq(SleepRecord::getUserId, userId)
            .ge(SleepRecord::getStartTime, start)
            .lt(SleepRecord::getStartTime, end);
        return list(query);
    }
}
