package com.yb.api.client;

import com.yb.api.dto.req.UserProfileReq;
import com.yb.api.dto.resp.UserProfileResp;
import com.yb.api.dto.resp.UserSimpleResp;
import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务 Feign 接口
 */
@FeignClient(
        name = "yb-user",
        path = "/api/user",
        fallbackFactory = com.yb.api.fallback.UserClientFallbackFactory.class
)
public interface UserClient {

    @GetMapping("/{userId}")
    R<UserSimpleResp> getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/profile")
    R<UserProfileResp> getProfile(@RequestBody UserProfileReq req);
}
