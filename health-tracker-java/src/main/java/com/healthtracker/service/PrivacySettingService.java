package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.PrivacySetting;
import com.healthtracker.mapper.PrivacySettingMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PrivacySettingService extends ServiceImpl<PrivacySettingMapper, PrivacySetting> {
    public PrivacySetting getOrCreate(Long userId) {
        LambdaQueryWrapper<PrivacySetting> query = new LambdaQueryWrapper<>();
        query.eq(PrivacySetting::getUserId, userId);
        PrivacySetting setting = getOne(query);
        if (setting == null) {
            setting = new PrivacySetting();
            setting.setUserId(userId);
            setting.setAllowSubscribe(1);
            setting.setAllowCloudSync(1);
            setting.setAllowFamilyShare(1);
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            save(setting);
        }
        return setting;
    }
}
