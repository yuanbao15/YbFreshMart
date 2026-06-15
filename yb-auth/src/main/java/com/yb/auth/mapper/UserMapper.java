package com.yb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yb.auth.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户认证 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
