package com.yb.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页查询请求
 */
@Data
public class UserPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为 1")
    private Long page;

    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小最小为 1")
    @Max(value = 100, message = "每页大小最大为 100")
    private Long size;

    /** 搜索关键词（模糊匹配昵称） */
    private String keyword;
}
