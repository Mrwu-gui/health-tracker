package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
}
