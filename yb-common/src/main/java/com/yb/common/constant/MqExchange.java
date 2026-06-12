package com.yb.common.constant;

/**
 * RabbitMQ 交换机常量
 */
public interface MqExchange {

    /** 订单事件交换机（Topic） */
    String ORDER_EVENT = "order.event.exchange";

    /** 商品同步交换机（Fanout） */
    String PRODUCT_SYNC = "product.sync.exchange";

    /** 日志交换机（Topic） */
    String LOG = "log.exchange";
}
