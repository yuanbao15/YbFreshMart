SET NAMES utf8mb4;

-- ===========================================
-- 用户服务数据库 (yb_user)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_user;

-- 用户信息表
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT PRIMARY KEY COMMENT '用户ID',
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password    VARCHAR(255) NOT NULL COMMENT '加密密码',
    nickname    VARCHAR(50)  COMMENT '昵称',
    avatar      VARCHAR(500) COMMENT '头像URL',
    email       VARCHAR(100) COMMENT '邮箱',
    gender      TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS t_address (
    id              BIGINT PRIMARY KEY COMMENT '地址ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    receiver_name   VARCHAR(50)  NOT NULL COMMENT '收货人',
    receiver_phone  VARCHAR(20)  NOT NULL COMMENT '收货人手机号',
    province        VARCHAR(50)  COMMENT '省份',
    city            VARCHAR(50)  COMMENT '城市',
    district        VARCHAR(50)  COMMENT '区/县',
    detail          VARCHAR(255) COMMENT '详细地址',
    is_default      TINYINT DEFAULT 0 COMMENT '默认地址: 0-否, 1-是',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';
