-- =====================================================================
-- AI-Project 数据库初始化脚本 01：Schema 定义
-- 数据库：PostgreSQL 16
-- 用途：Java 与 .NET 双后端共用同一套 Schema
-- =====================================================================

-- 创建数据库（需以超级用户执行一次；docker-compose 中由 POSTGRES_DB 自动创建）
-- CREATE DATABASE ai_project;

\connect ai_project

-- ---------- 扩展 ----------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- 用于 gen_random_uuid()

-- ---------- 自动更新 updated_at 的通用函数 ----------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- 表：users 用户表
-- =====================================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,           -- BCrypt 哈希
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role     CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 表：articles 文章表
-- =====================================================================
CREATE TABLE IF NOT EXISTS articles (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL DEFAULT '',
    published  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_articles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_articles_user_id    ON articles (user_id);
CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles (created_at DESC);

DROP TRIGGER IF EXISTS trg_articles_updated_at ON articles;
CREATE TRIGGER trg_articles_updated_at
    BEFORE UPDATE ON articles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 表：audit_logs 审计日志表（安全基线：记录关键操作）
-- =====================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT,
    action     VARCHAR(50) NOT NULL,               -- 如 LOGIN / CREATE_ARTICLE / DELETE_ARTICLE
    detail     JSONB,
    ip         INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_user_id    ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs (created_at DESC);
