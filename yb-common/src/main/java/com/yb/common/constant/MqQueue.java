package com.yb.common.constant;

/**
 * RabbitMQ 队列常量
 */
public interface MqQueue {

    /** 订单创建 -> 扣库存 */
    String ORDER_CREATE = "queue.order.create";

    /** 订单支付超时（DLX） */
    String ORDER_PAY_TIMEOUT = "queue.order.pay.timeout";

    /** 订单已支付 */
    String ORDER_PAID = "queue.order.paid";

    /** 订单已取消 */
    String ORDER_CANCELLED = "queue.order.cancelled";

    /** 商品同步到 ES */
    String PRODUCT_SYNC_SEARCH = "queue.product.sync.search";

    /** 行为日志 */
    String LOG_BEHAVIOR = "queue.log.behavior";

    /** 审计日志 */
    String LOG_AUDIT = "queue.log.audit";
}
