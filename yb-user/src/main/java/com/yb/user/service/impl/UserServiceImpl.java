package com.yb.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.common.exception.BizException;
import com.yb.user.dto.UserPageQuery;
import com.yb.user.dto.UserSaveReq;
import com.yb.user.entity.UserEntity;
import com.yb.user.mapper.UserMapper;
import com.yb.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yb.common.enums.ErrorCode.*;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserEntity getById(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserEntity getByPhone(String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public Page<UserEntity> page(UserPageQuery query) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getKeyword() != null, UserEntity::getNickname, query.getKeyword())
                .orderByDesc(UserEntity::getCreateTime);
        Page<UserEntity> page = new Page<>(query.getPage(), query.getSize());
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity save(UserSaveReq req) {
        // 检查手机号是否已注册
        UserEntity existing = getByPhone(req.getPhone());
        if (existing != null) {
            throw new BizException(USER_PHONE_EXIST);
        }

        UserEntity user = new UserEntity();
        user.setPhone(req.getPhone());
        // 密码 BCrypt 加密
        user.setPassword(BCrypt.hashpw(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : "用户" + req.getPhone().substring(7));
        user.setGender(req.getGender() != null ? req.getGender() : 0);
        user.setStatus(1);

        userMapper.insert(user);
        log.info("[User] 新增用户成功, id={}, phone={}", user.getId(), user.getPhone());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity update(Long id, UserSaveReq req) {
        UserEntity user = getById(id);
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        userMapper.updateById(user);
        log.info("[User] 更新用户成功, id={}", id);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        UserEntity user = getById(id);
        userMapper.deleteById(user.getId());  // 逻辑删除
        log.info("[User] 删除用户成功, id={}", id);
        return true;
    }
}
