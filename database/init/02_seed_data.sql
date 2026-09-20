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
