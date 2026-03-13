package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.Reminder;
import com.healthtracker.mapper.ReminderMapper;
import org.springframework.stereotype.Service;

@Service
public class ReminderService extends ServiceImpl<ReminderMapper, Reminder> {}
