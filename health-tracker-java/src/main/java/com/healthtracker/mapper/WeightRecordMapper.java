package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeightRecordMapper extends BaseMapper<WeightRecord> {
}
