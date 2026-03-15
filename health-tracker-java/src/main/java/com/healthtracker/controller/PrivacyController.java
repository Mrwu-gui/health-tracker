package com.healthtracker.controller;

import com.healthtracker.dto.PrivacyUpdateRequest;
import com.healthtracker.entity.PrivacySetting;
import com.healthtracker.service.PrivacySettingService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
public class PrivacyController {
    private final PrivacySettingService privacySettingService;

    public PrivacyController(PrivacySettingService privacySettingService) {
        this.privacySettingService = privacySettingService;
    }

    @GetMapping("/get")
    public PrivacySetting get(@RequestParam Long userId) {
        return privacySettingService.getOrCreate(userId);
    }

    @PostMapping("/update")
    public PrivacySetting update(@Valid @RequestBody PrivacyUpdateRequest request) {
        PrivacySetting setting = privacySettingService.getOrCreate(request.getUserId());
        if (request.getAllowSubscribe() != null) {
            setting.setAllowSubscribe(request.getAllowSubscribe());
        }
        if (request.getAllowCloudSync() != null) {
            setting.setAllowCloudSync(request.getAllowCloudSync());
        }
        if (request.getAllowFamilyShare() != null) {
            setting.setAllowFamilyShare(request.getAllowFamilyShare());
        }
        setting.setUpdatedAt(LocalDateTime.now());
        privacySettingService.updateById(setting);
        return setting;
    }
}
