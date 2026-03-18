package com.healthtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.FileRecord;
import com.healthtracker.mapper.FileRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class FileRecordService extends ServiceImpl<FileRecordMapper, FileRecord> {}
