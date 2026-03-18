package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.User;
import com.healthtracker.mapper.UserMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    public User register(String username, String encodedPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());
        save(user);
        return user;
    }

    public User findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    public User findByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    public User findByWxOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getWxOpenid, openid));
    }

    public User findByWxUnionid(String unionid) {
        if (unionid == null || unionid.isBlank()) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<User>().eq(User::getWxUnionid, unionid));
    }

    public User registerByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setUsername(phone);
        user.setCreatedAt(LocalDateTime.now());
        save(user);
        return user;
    }

    public User registerByWeChat(String openid, String unionid, String nickname, String avatar) {
        User user = new User();
        user.setWxOpenid(openid);
        user.setWxUnionid(unionid);
        user.setWxNickname(nickname);
        user.setWxAvatar(avatar);
        user.setCreatedAt(LocalDateTime.now());
        save(user);
        return user;
    }

    public void bindWeChatPhone(User user, String phone) {
        user.setWxPhone(phone);
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            user.setPhone(phone);
        }
        updateById(user);
    }
}
