package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.AiChatMessage;
import com.healthtracker.mapper.AiChatMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class AiChatMessageService extends ServiceImpl<AiChatMessageMapper, AiChatMessage> {}
