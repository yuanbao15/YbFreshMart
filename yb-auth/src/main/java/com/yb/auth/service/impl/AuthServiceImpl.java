package com.yb.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yb.api.client.UserClient;
import com.yb.api.dto.req.UserCreateReq;
import com.yb.api.dto.resp.UserProfileResp;
import com.yb.auth.dto.LoginReq;
import com.yb.auth.dto.LoginResp;
import com.yb.auth.dto.RegisterReq;
import com.yb.auth.entity.UserEntity;
import com.yb.auth.mapper.UserMapper;
import com.yb.auth.service.AuthService;
import com.yb.common.dto.R;
import com.yb.common.exception.BizException;
import com.yb.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.yb.common.enums.ErrorCode.*;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;

    private static final String TOKEN_BLACKLIST = "token:blacklist:";

    @Override
    public LoginResp login(LoginReq req) {
        // 1. 查询用户
        UserEntity user = getByPhone(req.getPhone());
        if (user == null) {
            throw new BizException(USER_NOT_FOUND);
        }

        // 2. 校验状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(USER_ACCOUNT_DISABLED);
        }

        // 3. 校验密码
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BizException(USER_PASSWORD_ERROR);
        }

        // 4. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), "user");

        // 5. 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 构造响应
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(getNicknameFromUserService(user.getId()));
        resp.setRole("user");
        resp.setLoginTime(user.getLastLogin());

        log.info("[Auth] 登录成功, userId={}", user.getId());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp register(RegisterReq req) {
        // 1. 检查手机号是否已注册
        UserEntity existing = getByPhone(req.getPhone());
        if (existing != null) {
            throw new BizException(USER_PHONE_EXIST);
        }

        // 2. 创建认证记录
        UserEntity user = new UserEntity();
        user.setPhone(req.getPhone());
        user.setPassword(BCrypt.hashpw(req.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);

        // 3. 调用 yb-user 创建用户档案
        try {
            UserCreateReq createReq = new UserCreateReq();
            createReq.setPhone(req.getPhone());
            createReq.setPassword(req.getPassword());
            createReq.setNickname(req.getNickname() != null ? req.getNickname()
                    : "用户" + req.getPhone().substring(7));
            R<UserProfileResp> result = userClient.createUser(createReq);
            if (result.isSuccess() && result.getData() != null) {
                log.info("[Auth] 同步创建用户档案成功, userId={}", result.getData().getId());
            }
        } catch (Exception e) {
            log.warn("[Auth] 同步创建用户档案失败（不影响注册）: {}", e.getMessage());
        }

        // 4. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), "user");

        // 5. 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 构造响应
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(req.getNickname() != null ? req.getNickname()
                : "用户" + req.getPhone().substring(7));
        resp.setRole("user");
        resp.setLoginTime(user.getLastLogin());

        log.info("[Auth] 注册成功, userId={}", user.getId());
        return resp;
    }

    @Override
    public LoginResp refreshToken(String oldToken) {
        // 1. 解析旧 Token
        Long userId = jwtUtil.getUserId(oldToken);
        String role = jwtUtil.getUserRole(oldToken);
        if (userId == null) {
            throw new BizException(USER_TOKEN_INVALID);
        }

        // 2. 旧 Token 加入黑名单
        long remaining = jwtUtil.getRemainingTime(oldToken);
        if (remaining > 0) {
            String blacklistKey = TOKEN_BLACKLIST + oldToken;
            redisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofMillis(remaining));
        }

        // 3. 生成新 Token
        String newToken = jwtUtil.refreshToken(oldToken);

        // 4. 查询用户信息
        UserEntity user = userMapper.selectById(userId);

        LoginResp resp = new LoginResp();
        resp.setToken(newToken);
        resp.setUserId(userId);
        resp.setPhone(user != null ? user.getPhone() : "");
        resp.setNickname(getNicknameFromUserService(userId));
        resp.setRole(role);
        resp.setLoginTime(LocalDateTime.now());

        log.info("[Auth] Token 刷新成功, userId={}", userId);
        return resp;
    }

    @Override
    public void logout(String token) {
        // Token 加入黑名单
        long remaining = jwtUtil.getRemainingTime(token);
        if (remaining > 0) {
            String blacklistKey = TOKEN_BLACKLIST + token;
            redisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofMillis(remaining));
            log.info("[Auth] 登出成功, Token 已加入黑名单");
        }
    }

    /** 根据手机号查询用户 */
    private UserEntity getByPhone(String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    /** 从 yb-user 服务获取昵称 */
    private String getNicknameFromUserService(Long userId) {
        try {
            R<com.yb.api.dto.resp.UserSimpleResp> result = userClient.getUserById(userId);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().getNickname();
            }
        } catch (Exception e) {
            log.debug("[Auth] 获取用户昵称失败: {}", e.getMessage());
        }
        return "";
    }
}
