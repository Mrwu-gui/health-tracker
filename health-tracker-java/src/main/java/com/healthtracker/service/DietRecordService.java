package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.DietRecord;
import com.healthtracker.mapper.DietRecordMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DietRecordService extends ServiceImpl<DietRecordMapper, DietRecord> {
    public List<DietRecord> listByUserAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<DietRecord> query = new LambdaQueryWrapper<>();
        query.eq(DietRecord::getUserId, userId).eq(DietRecord::getDate, date);
        return list(query);
    }
}
