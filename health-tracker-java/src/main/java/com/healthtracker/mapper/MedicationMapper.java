package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.Medication;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicationMapper extends BaseMapper<Medication> {
}
