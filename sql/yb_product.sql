SET NAMES utf8mb4;

-- ===========================================
-- 商品服务数据库 (yb_product)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_product
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_product;

-- 商品类目表
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT DEFAULT 0 COMMENT '父类目ID, 0=顶级',
    name        VARCHAR(50) NOT NULL COMMENT '类目名称',
    level       TINYINT DEFAULT 1 COMMENT '层级: 1,2,3',
    sort_order  INT DEFAULT 0 COMMENT '排序',
    icon        VARCHAR(255) COMMENT '图标URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类目';

-- 商品 SPU 表（标准化产品单元）
CREATE TABLE IF NOT EXISTS t_spu (
    id          BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '类目ID',
    name        VARCHAR(200) NOT NULL COMMENT 'SPU名称',
    description TEXT COMMENT '描述',
    brand       VARCHAR(50) COMMENT '品牌',
    main_image  VARCHAR(500) COMMENT '主图',
    images      TEXT COMMENT '图片列表(JSON)',
    unit        VARCHAR(20) COMMENT '单位(斤/袋/箱)',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU';

-- 商品 SKU 表（库存量单位）
CREATE TABLE IF NOT EXISTS t_sku (
    id          BIGINT PRIMARY KEY,
    spu_id      BIGINT NOT NULL COMMENT 'SPU ID',
    name        VARCHAR(200) NOT NULL COMMENT 'SKU名称',
    image       VARCHAR(500) COMMENT 'SKU图片',
    spec        VARCHAR(200) COMMENT '规格(如"500g","2斤")',
    price       DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost_price  DECIMAL(10,2) COMMENT '成本价',
    stock       INT DEFAULT 0 COMMENT '库存',
    sold_count  INT DEFAULT 0 COMMENT '销量',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_spu (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';
