SET NAMES utf8mb4;

-- ===========================================
-- 订单服务数据库 (yb_order)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_order
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_order;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id              BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no        VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount      DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    status          TINYINT DEFAULT 0 COMMENT '状态: 0-待支付, 1-已支付, 2-已发货, 3-已收货, 4-已取消, 5-退款中, 6-已退款',
    receiver_name   VARCHAR(50) COMMENT '收货人',
    receiver_phone  VARCHAR(20) COMMENT '收货人手机号',
    receiver_addr   VARCHAR(500) COMMENT '收货地址',
    remark          VARCHAR(255) COMMENT '备注',
    pay_time        DATETIME COMMENT '支付时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_create (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS t_order_item (
    id          BIGINT PRIMARY KEY,
    order_id    BIGINT NOT NULL COMMENT '订单ID',
    order_no    VARCHAR(32) NOT NULL COMMENT '订单号',
    sku_id      BIGINT NOT NULL COMMENT 'SKU ID',
    sku_name    VARCHAR(200) COMMENT '商品名称',
    sku_image   VARCHAR(500) COMMENT '商品图片',
    price       DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity    INT NOT NULL COMMENT '数量',
    amount      DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';
