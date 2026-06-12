package com.yb.common.constant;

/**
 * Redis Key 常量
 */
public interface RedisKey {

    /** 用户 Token */
    String USER_TOKEN = "token:user:";

    /** 购物车 */
    String CART_PREFIX = "cart:";

    /** 商品库存缓存 */
    String STOCK_SKU = "stock:sku:";

    /** 库存分布式锁 */
    String LOCK_STOCK = "lock:stock:sku:";

    /** 热门商品排行榜 */
    String HOT_PRODUCTS = "hot:products";

    /** 短信验证码 */
    String SMS_CODE = "sms:code:";

    /** 防重复提交 */
    String REPEAT_SUBMIT = "repeat:";

    /** 商品详情缓存 */
    String PRODUCT_DETAIL = "product:detail:";

    /** 商品分类列表缓存 */
    String PRODUCT_CATEGORIES = "product:categories";

    /** 幂等消息去重 */
    String MQ_IDEMPOTENT = "mq:idempotent:";
}
