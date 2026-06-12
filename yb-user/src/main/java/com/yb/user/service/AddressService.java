package com.yb.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.user.entity.AddressEntity;

import java.util.List;

/**
 * 地址服务接口
 */
public interface AddressService {

    /** 查询用户的所有地址 */
    List<AddressEntity> listByUserId(Long userId);

    /** 根据 ID 查询 */
    AddressEntity getById(Long id);

    /** 新增地址 */
    AddressEntity save(AddressEntity address);

    /** 更新地址 */
    AddressEntity update(Long id, AddressEntity address);

    /** 删除地址 */
    boolean delete(Long id);

    /** 设置默认地址 */
    boolean setDefault(Long userId, Long addressId);
}
