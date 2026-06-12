package com.yb.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yb.user.entity.AddressEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址 Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<AddressEntity> {
}
