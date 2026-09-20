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
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,           -- BCrypt 哈希
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    phone           VARCHAR(20),                     -- 手机号（产品化：可空=兼容老用户）
    level           INT          NOT NULL DEFAULT 1, -- 创作等级（游戏化）
    continuous_days INT          NOT NULL DEFAULT 0, -- 连续创作天数
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_phone    UNIQUE (phone),
    CONSTRAINT ck_users_role     CHECK (role IN ('USER', 'ADMIN'))
);

-- ============================================================
-- 旧库升级迁移（第 1 课产品化：phone/level/continuous_days）
-- 表已存在时上方 CREATE IF NOT EXISTS 不生效，必须用幂等 ALTER 补列
-- ============================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS level INT NOT NULL DEFAULT 1;
ALTER TABLE users ADD COLUMN IF NOT EXISTS continuous_days INT NOT NULL DEFAULT 0;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_users_phone') THEN
        ALTER TABLE users ADD CONSTRAINT uq_users_phone UNIQUE (phone);
    END IF;
END $$;

-- 存量库迁移（已有 users 表时执行一次）：
-- ALTER TABLE users ADD COLUMN phone VARCHAR(20), ADD COLUMN level INT NOT NULL DEFAULT 1,
--                   ADD COLUMN continuous_days INT NOT NULL DEFAULT 0;
-- CREATE UNIQUE INDEX uq_users_phone ON users (phone);

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

-- =====================================================================
-- 表：styles 风格库（第 2 课：风格选择模块）
-- =====================================================================
CREATE TABLE IF NOT EXISTS styles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL,
    description     VARCHAR(500),
    category        VARCHAR(30)  NOT NULL,          -- 国风/赛博/治愈/热血/搞笑/奇幻
    cover_url       VARCHAR(500),
    prompt_template TEXT,                           -- 生成时用户创意填充 {idea}
    sort_order      INT          NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,  -- 上架/下架（软删除）
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_styles_name UNIQUE (name),
    CONSTRAINT ck_styles_category CHECK (category IN
        ('国风', '赛博', '治愈', '热血', '搞笑', '奇幻'))
);

CREATE INDEX IF NOT EXISTS idx_styles_active_sort ON styles (active, sort_order);

DROP TRIGGER IF EXISTS trg_styles_updated_at ON styles;
CREATE TRIGGER trg_styles_updated_at
    BEFORE UPDATE ON styles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 表：drafts 创作草稿（第 3 课：三步向导）
-- 说明：草稿为私有数据，user_id 做逻辑关联（无外键，性能优先、应用层守卫越权）
-- =====================================================================
CREATE TABLE IF NOT EXISTS drafts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    idea       TEXT,
    style_id   BIGINT,                          -- 逻辑关联 styles.id（可空=未选风格）
    duration   INT          NOT NULL DEFAULT 30,  -- 秒：15/30/60
    ratio      VARCHAR(10)  NOT NULL DEFAULT '9:16',  -- 9:16 / 16:9 / 1:1
    voiceover  BOOLEAN      NOT NULL DEFAULT TRUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT（第 4 课生成时转任务）
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_drafts_ratio  CHECK (ratio IN ('9:16', '16:9', '1:1')),
    CONSTRAINT ck_drafts_status CHECK (status IN ('DRAFT'))
);

CREATE INDEX IF NOT EXISTS idx_drafts_user_updated ON drafts (user_id, updated_at DESC);

DROP TRIGGER IF EXISTS trg_drafts_updated_at ON drafts;
CREATE TRIGGER trg_drafts_updated_at
    BEFORE UPDATE ON drafts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 第 4 课：生成任务状态机 + 额度账户
-- ============================================================

-- 生成任务表（6 状态：PENDING/AGENTS/RENDERING/COMPILING/SUCCESS/FAILED）
CREATE TABLE IF NOT EXISTS generation_tasks (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id),
    draft_id      BIGINT       NOT NULL REFERENCES drafts(id),
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    progress      INTEGER      NOT NULL DEFAULT 0,
    stage_message VARCHAR(200),
    error_message VARCHAR(500),
    video_url     VARCHAR(500),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_tasks_status CHECK (status IN ('PENDING','AGENTS','RENDERING','COMPILING','SUCCESS','FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_tasks_user_created ON generation_tasks (user_id, created_at DESC);

-- 幂等核心：部分唯一索引 —— 同一草稿同时只能有 1 个"运行中"任务（应用层判断 + DB 双保险）
CREATE UNIQUE INDEX IF NOT EXISTS uq_tasks_draft_running
    ON generation_tasks (draft_id)
    WHERE status IN ('PENDING', 'AGENTS', 'RENDERING', 'COMPILING');

DROP TRIGGER IF EXISTS trg_tasks_updated_at ON generation_tasks;
CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON generation_tasks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 额度账户（每用户一行；扣减用 SELECT FOR UPDATE 行锁防超扣）
CREATE TABLE IF NOT EXISTS credit_accounts (
    id            BIGSERIAL   PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(id),
    balance       INTEGER     NOT NULL DEFAULT 0,
    monthly_quota INTEGER     NOT NULL DEFAULT 10,   -- 免费用户每月 10 次
    billing_month VARCHAR(7)  NOT NULL,               -- 账单月 yyyy-MM，跨月重置
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_credit_accounts_user UNIQUE (user_id)
);

DROP TRIGGER IF EXISTS trg_credit_accounts_updated_at ON credit_accounts;
CREATE TRIGGER trg_credit_accounts_updated_at
    BEFORE UPDATE ON credit_accounts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 额度流水（幂等键唯一约束 = 防重复扣费的最后一道闸）
CREATE TABLE IF NOT EXISTS credit_transactions (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id),
    task_id         BIGINT      NOT NULL REFERENCES generation_tasks(id),
    amount          INTEGER     NOT NULL,             -- 扣费为负（-1）
    type            VARCHAR(20) NOT NULL,             -- TASK_CREATE / RECHARGE / MONTH_RESET
    idempotency_key VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_credit_tx_key UNIQUE (idempotency_key)
);

-- ============================================================
-- 第 5 课：7 智能体阶段字段（generation_tasks 追加列）
-- ============================================================
ALTER TABLE generation_tasks ADD COLUMN IF NOT EXISTS agent_stage   VARCHAR(20);  -- 当前智能体
ALTER TABLE generation_tasks ADD COLUMN IF NOT EXISTS agent_progress INTEGER     NOT NULL DEFAULT 0;  -- 智能体内部进度

-- ============================================================
-- 第 6 课：作品表（任务 SUCCESS 自动落一条）
-- ============================================================
CREATE TABLE IF NOT EXISTS works (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    draft_id   BIGINT       NOT NULL REFERENCES drafts(id),
    task_id    BIGINT       NOT NULL REFERENCES generation_tasks(id),
    title      VARCHAR(100) NOT NULL,
    video_url  VARCHAR(500),
    cover_url  VARCHAR(500),
    version    INTEGER      NOT NULL DEFAULT 1,   -- 同草稿第几次成功生成
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_works_draft_version UNIQUE (draft_id, version)
);

CREATE INDEX IF NOT EXISTS idx_works_user_created ON works (user_id, created_at DESC);

DROP TRIGGER IF EXISTS trg_works_updated_at ON works;
CREATE TRIGGER trg_works_updated_at
    BEFORE UPDATE ON works
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- AI-Project 数据库初始化脚本 02：种子数据
-- 说明：密码哈希为 BCrypt（成本因子 10），明文密码见注释
-- =====================================================================
\connect ai_project

-- 演示用户（明文密码：admin123 / user123，哈希为 BCrypt 成本因子 10，已用 bcrypt 5.0 验证）
INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin', 'admin@example.com', '$2b$10$NBlssNHwuYqVlbHGsq9S8OmbqB0r56kXDVbB.ym4fLUBHCLWk265W', 'ADMIN'),
    ('alice', 'alice@example.com', '$2b$10$XrkLNIwhH7/NGV02Zd/owuX6Os/tt5/65X5ZU42rC5KTQdTy2JKDa', 'USER')
ON CONFLICT (username) DO NOTHING;

-- 示例文章
INSERT INTO articles (user_id, title, content, published)
SELECT u.id, '欢迎使用 AI-Project', '这是一个由 Java 与 .NET 双后端驱动的示例文章。', TRUE
FROM users u WHERE u.username = 'alice'
ON CONFLICT DO NOTHING;

-- 风格库种子（第 2 课：6 分类 8 风格；prompt_template 中 {idea} 由用户创意填充）
INSERT INTO styles (name, description, category, cover_url, prompt_template, sort_order) VALUES
    ('新国风', '水墨丹青，东方美学，让创意自带古韵', '国风', '/covers/guofeng.jpg',
     '以中国水墨画风格呈现：{idea}，留白构图，淡雅配色，毛笔笔触', 10),
    ('武侠江湖', '刀光剑影，快意恩仇，少年侠客行', '国风', '/covers/wuxia.jpg',
     '以武侠动画风格呈现：{idea}，衣袂飘飘，竹林打斗，电影级运镜', 20),
    ('赛博霓虹', '霓虹闪烁的科技都市，赛博朋克视觉冲击', '赛博', '/covers/saibo.jpg',
     '以赛博朋克风格呈现：{idea}，霓虹灯光，雨夜街道，未来科技感', 30),
    ('软萌治愈', '圆润线条，治愈配色，治愈一切不开心', '治愈', '/covers/zhiyu.jpg',
     '以软萌治愈画风呈现：{idea}，圆润可爱，柔和配色，温馨日常', 40),
    ('少年热血', '燃烧吧！中二之魂与少年热血', '热血', '/covers/rexue.jpg',
     '以热血动漫风格呈现：{idea}，高燃动作，夸张特效，热血配乐', 50),
    ('沙雕搞笑', '表情包级搞笑，快乐源泉', '搞笑', '/covers/gaoxiao.jpg',
     '以搞笑动画风格呈现：{idea}，夸张表情，弹幕梗，节奏明快', 60),
    ('奇幻魔法', '魔法学院、龙与勇者的冒险', '奇幻', '/covers/mofa.jpg',
     '以奇幻动画风格呈现：{idea}，魔法特效，神秘氛围，史诗感', 70),
    ('机械机甲', '钢铁巨兽，机械浪漫', '奇幻', '/covers/jijia.jpg',
     '以机甲动画风格呈现：{idea}，金属质感，变形合体，燃爆战斗', 80)
ON CONFLICT (name) DO NOTHING;
