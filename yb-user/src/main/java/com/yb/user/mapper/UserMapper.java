package com.yb.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yb.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
