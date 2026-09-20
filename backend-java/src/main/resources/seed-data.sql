-- =====================================================================
-- H2 测试环境种子数据（仅 test profile 通过 application-test.yml 加载）
-- 与 database/init/02_seed_data.sql 保持一致（同一套 BCrypt 哈希）
-- 明文密码：admin123 / user123（BCrypt cost 10，哈希经 bcrypt 5.0 验证）
-- 注意：INSERT 列清单必须与 User 实体字段同步（新增 NOT NULL 列要补默认值）
-- =====================================================================
INSERT INTO users (username, email, password_hash, role, level, continuous_days, created_at, updated_at) VALUES
    ('admin', 'admin@example.com', '$2b$10$NBlssNHwuYqVlbHGsq9S8OmbqB0r56kXDVbB.ym4fLUBHCLWk265W', 'ADMIN', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('alice', 'alice@example.com', '$2b$10$XrkLNIwhH7/NGV02Zd/owuX6Os/tt5/65X5ZU42rC5KTQdTy2JKDa', 'USER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 风格库种子（第 2 课：与 database/init/02_seed_data.sql 保持一致）
-- 注意：H2 由 JPA 建表（无 DEFAULT），所有 NOT NULL 列必须显式给出；PG 端有 DEFAULT 无需
INSERT INTO styles (name, description, category, cover_url, prompt_template, sort_order, active, created_at, updated_at) VALUES
    ('新国风', '水墨丹青，东方美学，让创意自带古韵', '国风', '/covers/guofeng.jpg',
     '以中国水墨画风格呈现：{idea}，留白构图，淡雅配色，毛笔笔触', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('武侠江湖', '刀光剑影，快意恩仇，少年侠客行', '国风', '/covers/wuxia.jpg',
     '以武侠动画风格呈现：{idea}，衣袂飘飘，竹林打斗，电影级运镜', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('赛博霓虹', '霓虹闪烁的科技都市，赛博朋克视觉冲击', '赛博', '/covers/saibo.jpg',
     '以赛博朋克风格呈现：{idea}，霓虹灯光，雨夜街道，未来科技感', 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('软萌治愈', '圆润线条，治愈配色，治愈一切不开心', '治愈', '/covers/zhiyu.jpg',
     '以软萌治愈画风呈现：{idea}，圆润可爱，柔和配色，温馨日常', 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('少年热血', '燃烧吧！中二之魂与少年热血', '热血', '/covers/rexue.jpg',
     '以热血动漫风格呈现：{idea}，高燃动作，夸张特效，热血配乐', 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('沙雕搞笑', '表情包级搞笑，快乐源泉', '搞笑', '/covers/gaoxiao.jpg',
     '以搞笑动画风格呈现：{idea}，夸张表情，弹幕梗，节奏明快', 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('奇幻魔法', '魔法学院、龙与勇者的冒险', '奇幻', '/covers/mofa.jpg',
     '以奇幻动画风格呈现：{idea}，魔法特效，神秘氛围，史诗感', 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('机械机甲', '钢铁巨兽，机械浪漫', '奇幻', '/covers/jijia.jpg',
     '以机甲动画风格呈现：{idea}，金属质感，变形合体，燃爆战斗', 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
