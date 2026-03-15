package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.PeriodRecord;
import com.healthtracker.mapper.PeriodRecordMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PeriodRecordService extends ServiceImpl<PeriodRecordMapper, PeriodRecord> {
    public List<PeriodRecord> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<PeriodRecord>()
            .eq(PeriodRecord::getUserId, userId)
            .orderByDesc(PeriodRecord::getStartDate));
    }
}
