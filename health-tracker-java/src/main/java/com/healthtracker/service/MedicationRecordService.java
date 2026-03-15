package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.MedicationRecord;
import com.healthtracker.mapper.MedicationRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class MedicationRecordService extends ServiceImpl<MedicationRecordMapper, MedicationRecord> {
}
