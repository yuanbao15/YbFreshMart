package com.yb.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.user.dto.UserPageQuery;
import com.yb.user.dto.UserSaveReq;
import com.yb.user.entity.UserEntity;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 根据 ID 查询 */
    UserEntity getById(Long id);

    /** 根据手机号查询 */
    UserEntity getByPhone(String phone);

    /** 分页查询 */
    Page<UserEntity> page(UserPageQuery query);

    /** 新增用户 */
    UserEntity save(UserSaveReq req);

    /** 更新用户信息 */
    UserEntity update(Long id, UserSaveReq req);

    /** 删除用户（逻辑删除） */
    boolean delete(Long id);
}
