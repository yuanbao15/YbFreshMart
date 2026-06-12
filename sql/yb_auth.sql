SET NAMES utf8mb4;

-- ===========================================
-- 认证授权数据库 (yb_auth)
-- ===========================================
CREATE DATABASE IF NOT EXISTS yb_auth
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE yb_auth;

-- 用户认证表
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT PRIMARY KEY COMMENT '用户ID',
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password    VARCHAR(255) NOT NULL COMMENT '加密密码',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    last_login  DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';
