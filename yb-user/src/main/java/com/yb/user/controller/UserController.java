package com.yb.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.api.dto.resp.UserProfileResp;
import com.yb.api.dto.resp.UserSimpleResp;
import com.yb.common.dto.R;
import com.yb.user.dto.UserPageQuery;
import com.yb.user.dto.UserSaveReq;
import com.yb.user.entity.UserEntity;
import com.yb.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 根据 ID 查询 - 用于 Feign 调用 */
    @GetMapping("/{userId}")
    public R<UserSimpleResp> getUserById(@PathVariable Long userId) {
        UserEntity user = userService.getById(userId);
        UserSimpleResp resp = new UserSimpleResp();
        resp.setId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        return R.ok(resp);
    }

    /** 根据 ID 查询详情 */
    @GetMapping("/{userId}/detail")
    public R<UserProfileResp> getUserDetail(@PathVariable Long userId) {
        UserEntity user = userService.getById(userId);
        UserProfileResp resp = new UserProfileResp();
        resp.setId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setGender(user.getGender());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        return R.ok(resp);
    }

    /** 分页查询 */
    @GetMapping("/page")
    public R<Page<UserEntity>> pageUser(@Valid UserPageQuery query) {
        Page<UserEntity> page = userService.page(query);
        return R.ok(page);
    }

    /** 新增用户（注册） */
    @PostMapping
    public R<UserProfileResp> createUser(@Valid @RequestBody UserSaveReq req) {
        UserEntity user = userService.save(req);
        UserProfileResp resp = new UserProfileResp();
        resp.setId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setGender(user.getGender());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        return R.ok(resp);
    }

    /** 更新用户信息 */
    @PutMapping("/{userId}")
    public R<UserProfileResp> updateUser(@PathVariable Long userId, @Valid @RequestBody UserSaveReq req) {
        UserEntity user = userService.update(userId, req);
        UserProfileResp resp = new UserProfileResp();
        resp.setId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setGender(user.getGender());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        return R.ok(resp);
    }

    /** 删除用户（逻辑删除） */
    @DeleteMapping("/{userId}")
    public R<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return R.ok();
    }
}
