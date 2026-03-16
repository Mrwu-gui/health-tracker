package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.SystemLog;
import com.healthtracker.mapper.SystemLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService extends ServiceImpl<SystemLogMapper, SystemLog> {}
