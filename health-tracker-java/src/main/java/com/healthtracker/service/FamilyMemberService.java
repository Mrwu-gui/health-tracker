package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.FamilyMember;
import com.healthtracker.mapper.FamilyMemberMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FamilyMemberService extends ServiceImpl<FamilyMemberMapper, FamilyMember> {
    public List<FamilyMember> listByUser(Long userId) {
        LambdaQueryWrapper<FamilyMember> query = new LambdaQueryWrapper<>();
        query.eq(FamilyMember::getUserId, userId).orderByDesc(FamilyMember::getCreatedAt);
        return list(query);
    }
}
