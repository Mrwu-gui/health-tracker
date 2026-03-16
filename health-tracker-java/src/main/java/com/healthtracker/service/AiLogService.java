package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.AiLog;
import com.healthtracker.mapper.AiLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AiLogService extends ServiceImpl<AiLogMapper, AiLog> {}
