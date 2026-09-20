import { Tag, Typography } from 'antd';
import type { Style } from '../types';

interface StyleCardProps {
  style: Style;
  selected?: boolean;
  onClick?: () => void;
}

/**
 * StyleCard：风格卡片（风格选择页/模板库复用）
 * - selected：选中态高亮（选择器用）
 * - onClick：点击回调
 */
export default function StyleCard({ style, selected, onClick }: StyleCardProps) {
  return (
    <div
      role="button"
      data-testid="style-card"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={(e) => e.key === 'Enter' && onClick?.()}
      style={{
        position: 'relative',
        borderRadius: 12,
        overflow: 'hidden',
        cursor: 'pointer',
        border: selected ? '2px solid #1677ff' : '2px solid rgba(0,0,0,0.06)',
        transition: 'all 0.2s',
        boxSizing: 'border-box',
        background: '#fff',
      }}
    >
      {/* 封面区：无图时用分类色块占位 */}
      <div
        style={{
          height: 96,
          background: style.coverUrl
            ? `url(${style.coverUrl}) center/cover`
            : 'linear-gradient(135deg, #1677ff22, #69b1ff33)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <span style={{ fontSize: 28, opacity: 0.55 }}>🎨</span>
      </div>
      <div style={{ padding: '10px 12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Typography.Text strong style={{ fontSize: 14 }}>
            {style.name}
          </Typography.Text>
          <Tag color="processing" style={{ fontSize: 11, lineHeight: '18px', marginInlineEnd: 0 }}>
            {style.category}
          </Tag>
        </div>
        {style.description && (
          <Typography.Paragraph
            type="secondary"
            ellipsis={{ rows: 1 }}
            style={{ fontSize: 12, margin: '4px 0 0' }}
          >
            {style.description}
          </Typography.Paragraph>
        )}
      </div>
    </div>
  );
}
