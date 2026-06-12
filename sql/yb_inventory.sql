SET NAMES utf8mb4;

-- ===========================================
-- 库存服务数据库 (yb_inventory)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_inventory
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_inventory;

-- 库存表
CREATE TABLE IF NOT EXISTS t_inventory (
    id          BIGINT PRIMARY KEY,
    sku_id      BIGINT NOT NULL UNIQUE COMMENT 'SKU ID',
    stock       INT DEFAULT 0 COMMENT '当前库存',
    locked_stock INT DEFAULT 0 COMMENT '锁定库存(已下单未支付)',
    sold_count  INT DEFAULT 0 COMMENT '累计销量',
    version     INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 库存变更日志
CREATE TABLE IF NOT EXISTS t_inventory_log (
    id          BIGINT PRIMARY KEY,
    sku_id      BIGINT NOT NULL COMMENT 'SKU ID',
    order_no    VARCHAR(32) COMMENT '关联订单号',
    change_type TINYINT COMMENT '变更类型: 1-锁定, 2-扣减, 3-回补',
    quantity    INT COMMENT '变更数量',
    before_stock INT COMMENT '变更前库存',
    after_stock  INT COMMENT '变更后库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sku (sku_id),
    INDEX idx_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志';
