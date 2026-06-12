package com.yb.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体 - 所有微服务对外暴露的 API 统一使用此格式
 *
 * @param <T> 数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    // ==================== 构造器（Lombok 替代） ====================

    public R() {}

    public R(Integer code, String message, T data, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // ==================== 成功 ====================

    public static <T> R<T> ok() {
        return new R<>(200, "success", null, System.currentTimeMillis());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, System.currentTimeMillis());
    }

    // ==================== 失败 ====================

    public static <T> R<T> fail() {
        return new R<>(500, "服务器内部错误", null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null, System.currentTimeMillis());
    }

    // ==================== 判读 ====================

    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
