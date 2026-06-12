package com.yb.common.constant;

/**
 * RabbitMQ 路由 Key 常量
 */
public interface MqRoutingKey {

    String ORDER_CREATE = "order.create";
    String ORDER_PAID = "order.paid";
    String ORDER_CANCELLED = "order.cancelled";
    String LOG_BEHAVIOR = "log.behavior.#";
    String LOG_AUDIT = "log.audit.#";
}
