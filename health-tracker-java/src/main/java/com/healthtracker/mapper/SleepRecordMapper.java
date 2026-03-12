package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.SleepRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SleepRecordMapper extends BaseMapper<SleepRecord> {
}
