package com.yb.api.fallback;

import com.yb.api.client.UserClient;
import com.yb.api.dto.req.UserCreateReq;
import com.yb.api.dto.req.UserProfileReq;
import com.yb.api.dto.resp.UserProfileResp;
import com.yb.api.dto.resp.UserSimpleResp;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * UserClient 降级工厂（Sentinel 熔断后触发）
 */
@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("UserClient 调用失败，触发降级", cause);
        return new UserClient() {
            @Override
            public R<UserSimpleResp> getUserById(Long userId) {
                return R.fail(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserProfileResp> getProfile(UserProfileReq req) {
                return R.fail(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserProfileResp> createUser(UserCreateReq req) {
                return R.fail(500, "用户服务暂时不可用");
            }
        };
    }
}
