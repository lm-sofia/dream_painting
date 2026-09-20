import { useEffect, useMemo, useState } from 'react';
import { Empty, Segmented, Spin, Typography } from 'antd';
import { styleApi } from '../api';
import type { Style } from '../types';
import StyleCard from '../components/StyleCard';

const CATEGORIES = ['全部', '国风', '赛博', '治愈', '热血', '搞笑', '奇幻'];

/** 风格选择页：游客可看（公开接口），选中风格进入创作（第 3 课向导用） */
export default function StylePickerPage() {
  const [styles, setStyles] = useState<Style[]>([]);
  const [loading, setLoading] = useState(true);
  const [category, setCategory] = useState('全部');
  const [selectedId, setSelectedId] = useState<number | null>(null);

  useEffect(() => {
    styleApi
      .list()
      .then(({ data }) => setStyles(data.data))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(
    () => (category === '全部' ? styles : styles.filter((s) => s.category === category)),
    [styles, category],
  );

  return (
    <div style={{ maxWidth: 960, margin: '0 auto', padding: '24px 16px' }}>
      <Typography.Title level={3} style={{ marginBottom: 4 }}>
        选择创作风格
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
        8 种风格 × 7 大 AI 智能体协作，30 秒生成你的动画短视频
      </Typography.Paragraph>

      <Segmented
        options={CATEGORIES}
        value={category}
        onChange={(v) => setCategory(v as string)}
        style={{ marginBottom: 20 }}
      />

      {loading ? (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin />
        </div>
      ) : filtered.length === 0 ? (
        <Empty description="该分类暂无风格" />
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
            gap: 16,
          }}
        >
          {filtered.map((style) => (
            <StyleCard
              key={style.id}
              style={style}
              selected={selectedId === style.id}
              onClick={() => setSelectedId(style.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
