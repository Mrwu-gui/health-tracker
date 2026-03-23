package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.AiVisionRecord;
import com.healthtracker.mapper.AiVisionRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class AiVisionRecordService extends ServiceImpl<AiVisionRecordMapper, AiVisionRecord> {}
