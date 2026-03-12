package com.healthtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthtracker.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
