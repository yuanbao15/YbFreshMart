SET NAMES utf8mb4;

-- ===========================================
-- 支付服务数据库 (yb_payment)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_payment
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_payment;

-- 支付记录表
CREATE TABLE IF NOT EXISTS t_payment (
    id              BIGINT PRIMARY KEY,
    pay_no          VARCHAR(32) NOT NULL UNIQUE COMMENT '支付流水号',
    order_no        VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    amount          DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status          TINYINT DEFAULT 0 COMMENT '状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款',
    pay_channel     VARCHAR(20) COMMENT '支付渠道: alipay/wechat/mock',
    pay_time        DATETIME COMMENT '支付时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order (order_no),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';
