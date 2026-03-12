package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.mapper.WeightRecordMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeightRecordService extends ServiceImpl<WeightRecordMapper, WeightRecord> {
    public List<WeightRecord> listByUserAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<WeightRecord> query = new LambdaQueryWrapper<>();
        query.eq(WeightRecord::getUserId, userId).eq(WeightRecord::getDate, date);
        return list(query);
    }
}
