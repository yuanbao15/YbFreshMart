package com.yb.common.mq.config;

import com.yb.common.constant.MqExchange;
import com.yb.common.constant.MqQueue;
import com.yb.common.constant.MqRoutingKey;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 交换机、队列、绑定声明
 * <p>
 * 在应用启动时自动在 RabbitMQ 中创建所有预定义的交换机、队列和绑定。
 * RabbitMQ 幂等性保证：已存在的同名资源不会重复创建。
 */
@AutoConfiguration
public class ExchangeQueueDeclarer {

    // ==================== 交换机 ====================

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(MqExchange.ORDER_EVENT, true, false);
    }

    @Bean
    public FanoutExchange productSyncExchange() {
        return new FanoutExchange(MqExchange.PRODUCT_SYNC, true, false);
    }

    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(MqExchange.LOG, true, false);
    }

    // ==================== 队列 ====================

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(MqQueue.ORDER_CREATE).build();
    }

    @Bean
    public Queue orderPayTimeoutQueue() {
        return QueueBuilder.durable(MqQueue.ORDER_PAY_TIMEOUT)
                .ttl(30 * 60 * 1000) // 30 分钟超时
                .build();
    }

    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(MqQueue.ORDER_PAID).build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(MqQueue.ORDER_CANCELLED).build();
    }

    @Bean
    public Queue productSyncSearchQueue() {
        return QueueBuilder.durable(MqQueue.PRODUCT_SYNC_SEARCH).build();
    }

    @Bean
    public Queue logBehaviorQueue() {
        return QueueBuilder.durable(MqQueue.LOG_BEHAVIOR).build();
    }

    @Bean
    public Queue logAuditQueue() {
        return QueueBuilder.durable(MqQueue.LOG_AUDIT).build();
    }

    // ==================== 绑定 ====================

    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue())
                .to(orderEventExchange())
                .with(MqRoutingKey.ORDER_CREATE);
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder.bind(orderPaidQueue())
                .to(orderEventExchange())
                .with(MqRoutingKey.ORDER_PAID);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue())
                .to(orderEventExchange())
                .with(MqRoutingKey.ORDER_CANCELLED);
    }

    @Bean
    public Binding productSyncSearchBinding() {
        return BindingBuilder.bind(productSyncSearchQueue())
                .to(productSyncExchange());
    }

    @Bean
    public Binding logBehaviorBinding() {
        return BindingBuilder.bind(logBehaviorQueue())
                .to(logExchange())
                .with(MqRoutingKey.LOG_BEHAVIOR);
    }

    @Bean
    public Binding logAuditBinding() {
        return BindingBuilder.bind(logAuditQueue())
                .to(logExchange())
                .with(MqRoutingKey.LOG_AUDIT);
    }
}
