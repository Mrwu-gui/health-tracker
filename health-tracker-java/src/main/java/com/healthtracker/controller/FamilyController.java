package com.healthtracker.controller;

import com.healthtracker.dto.FamilyMemberRequest;
import com.healthtracker.entity.FamilyMember;
import com.healthtracker.service.FamilyMemberService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
    private final FamilyMemberService familyMemberService;

    public FamilyController(FamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
    }

    @GetMapping("/list")
    public List<FamilyMember> list(@RequestParam Long userId) {
        return familyMemberService.listByUser(userId);
    }

    @PostMapping("/add")
    public FamilyMember add(@Valid @RequestBody FamilyMemberRequest request) {
        FamilyMember member = new FamilyMember();
        member.setUserId(request.getUserId());
        member.setName(request.getName());
        member.setRelation(request.getRelation());
        member.setAge(request.getAge());
        member.setConditionText(request.getConditionText());
        member.setRole(request.getRole());
        member.setStatus(request.getStatus());
        member.setAvatar(request.getAvatar());
        member.setCreatedAt(request.getCreatedAt() == null ? LocalDateTime.now() : request.getCreatedAt());
        familyMemberService.save(member);
        return member;
    }

    @PostMapping("/update")
    public FamilyMember update(@Valid @RequestBody FamilyMemberRequest request) {
        FamilyMember member = familyMemberService.getById(request.getId());
        if (member == null) {
            throw new IllegalArgumentException("家庭成员不存在");
        }
        member.setName(request.getName());
        member.setRelation(request.getRelation());
        member.setAge(request.getAge());
        member.setConditionText(request.getConditionText());
        member.setRole(request.getRole());
        member.setStatus(request.getStatus());
        member.setAvatar(request.getAvatar());
        familyMemberService.updateById(member);
        return member;
    }

    @PostMapping("/remove")
    public boolean remove(@RequestBody FamilyMemberRequest request) {
        return familyMemberService.removeById(request.getId());
    }
}
