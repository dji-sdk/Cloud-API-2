-- FH Skeleton 数据库初始化脚本
-- 由 MySQL 官方镜像在首次启动时自动执行（/docker-entrypoint-initdb.d/）
-- 后续重启不会重复执行（数据已持久化到 volume）

-- 建库（compose 已通过 MYSQL_DATABASE 创建，此处兜底）
CREATE DATABASE IF NOT EXISTS fh_skeleton DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fh_skeleton;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    email         VARCHAR(128),
    email_verified TINYINT(1)  NOT NULL DEFAULT 0,
    nickname      VARCHAR(64),
    role          VARCHAR(16)  NOT NULL DEFAULT 'user',
    status        VARCHAR(16)  NOT NULL DEFAULT 'pending',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);
