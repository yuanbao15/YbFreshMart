package com.yb.common.enums;

import lombok.Getter;

/**
 * 业务错误码枚举
 * <p>
 * 错误码分段规则（方便快速定位问题）：
 * 1xxx - 通用错误
 * 2xxx - 用户相关
 * 3xxx - 商品相关
 * 4xxx - 订单相关
 * 5xxx - 库存相关
 * 6xxx - 支付相关
 */
@Getter
public enum ErrorCode {

    // ========== 通用 1xxx ==========
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== 用户 2xxx ==========
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_PASSWORD_ERROR(2002, "密码错误"),
    USER_ACCOUNT_DISABLED(2003, "账号已被禁用"),
    USER_PHONE_EXIST(2004, "手机号已被注册"),
    USER_TOKEN_EXPIRED(2005, "Token 已过期，请重新登录"),
    USER_TOKEN_INVALID(2006, "Token 无效"),

    // ========== 商品 3xxx ==========
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_SKU_NOT_FOUND(3002, "商品 SKU 不存在"),
    PRODUCT_CATEGORY_NOT_FOUND(3003, "商品类目不存在"),
    PRODUCT_OFF_SHELF(3004, "商品已下架"),

    // ========== 订单 4xxx ==========
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态不正确，无法操作"),
    ORDER_CANNOT_CANCEL(4003, "订单无法取消"),
    ORDER_CART_EMPTY(4004, "购物车为空，无法下单"),

    // ========== 库存 5xxx ==========
    STOCK_INSUFFICIENT(5001, "库存不足"),
    STOCK_LOCK_FAILED(5002, "库存锁定失败，请重试"),

    // ========== 支付 6xxx ==========
    PAYMENT_NOT_FOUND(6001, "支付记录不存在"),
    PAYMENT_AMOUNT_ERROR(6002, "支付金额不正确"),
    PAYMENT_ALREADY_PAID(6003, "订单已支付，请勿重复支付"),

    // ========== 搜索 7xxx ==========
    SEARCH_ERROR(7001, "搜索服务异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
