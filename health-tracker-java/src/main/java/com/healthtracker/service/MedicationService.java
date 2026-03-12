package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.Medication;
import com.healthtracker.mapper.MedicationMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MedicationService extends ServiceImpl<MedicationMapper, Medication> {
    public List<Medication> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<Medication>().eq(Medication::getUserId, userId));
    }
}
