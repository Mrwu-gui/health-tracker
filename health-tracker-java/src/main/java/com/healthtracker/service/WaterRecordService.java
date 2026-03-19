package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.WaterRecord;
import com.healthtracker.mapper.WaterRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WaterRecordService extends ServiceImpl<WaterRecordMapper, WaterRecord> {
    public List<WaterRecord> listByUserAndDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return list(new LambdaQueryWrapper<WaterRecord>()
            .eq(WaterRecord::getUserId, userId)
            .ge(WaterRecord::getDrinkTime, start)
            .lt(WaterRecord::getDrinkTime, end)
            .orderByDesc(WaterRecord::getDrinkTime));
    }
}
