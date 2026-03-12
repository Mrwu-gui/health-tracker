package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.mapper.HealthRecordMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HealthRecordService extends ServiceImpl<HealthRecordMapper, HealthRecord> {
    public List<HealthRecord> listByUserAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<HealthRecord> query = new LambdaQueryWrapper<>();
        query.eq(HealthRecord::getUserId, userId).eq(HealthRecord::getDate, date);
        return list(query);
    }
}
