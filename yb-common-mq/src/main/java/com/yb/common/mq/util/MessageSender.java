package com.yb.common.mq.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息发送工具
 * <p>
 * 封装 RabbitTemplate 常用发送方法，简化业务代码中的消息发送。
 * 使用示例：
 * <pre>{@code
 *   messageSender.send(MqExchange.ORDER_EVENT, MqRoutingKey.ORDER_CREATE, orderMsg);
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换机和路由键
     */
    public void send(String exchange, String routingKey, Object message) {
        log.debug("发送消息 → exchange: {}, routingKey: {}, payload: {}", exchange, routingKey, message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送消息到指定交换机和路由键，带延迟（需安装 rabbitmq_delayed_message_exchange 插件）
     */
    public void sendWithDelay(String exchange, String routingKey, Object message, int delayMs) {
        log.debug("发送延迟消息 → exchange: {}, routingKey: {}, delay: {}ms", exchange, routingKey, delayMs);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setDelay(delayMs);
            return msg;
        });
    }

    /**
     * 发送消息到默认交换机（直接指定队列名作为 routingKey）
     */
    public void sendToQueue(String queueName, Object message) {
        log.debug("发送消息 → queue: {}, payload: {}", queueName, message);
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
