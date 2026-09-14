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
