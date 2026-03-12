package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.mapper.ExerciseRecordMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExerciseRecordService extends ServiceImpl<ExerciseRecordMapper, ExerciseRecord> {
    public List<ExerciseRecord> listByUserAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<ExerciseRecord> query = new LambdaQueryWrapper<>();
        query.eq(ExerciseRecord::getUserId, userId).eq(ExerciseRecord::getDate, date);
        return list(query);
    }
}
