package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.mapper.WeRunRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class WeRunRecordService extends ServiceImpl<WeRunRecordMapper, WeRunRecord> {
    public WeRunRecord upsert(Long userId, LocalDate date, Integer steps) {
        WeRunRecord existing = getOne(new LambdaQueryWrapper<WeRunRecord>()
            .eq(WeRunRecord::getUserId, userId)
            .eq(WeRunRecord::getRecordDate, date)
            .last("LIMIT 1"));
        if (existing == null) {
            WeRunRecord record = new WeRunRecord();
            record.setUserId(userId);
            record.setRecordDate(date);
            record.setSteps(steps);
            record.setCreatedAt(LocalDateTime.now());
            save(record);
            return record;
        }
        existing.setSteps(steps);
        updateById(existing);
        return existing;
    }
}
