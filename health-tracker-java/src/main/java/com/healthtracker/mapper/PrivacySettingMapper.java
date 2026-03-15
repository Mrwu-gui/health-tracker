package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.PrivacySetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivacySettingMapper extends BaseMapper<PrivacySetting> {
}
