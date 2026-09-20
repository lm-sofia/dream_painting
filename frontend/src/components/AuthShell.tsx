import { Card, Typography } from 'antd';
import type { ReactNode } from 'react';

interface AuthShellProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
}

/**
 * AuthShell：登录/注册页共用布局
 * 封装动机：两个页面共享"居中卡片 + 标题 + 底部链接"结构，抽出来避免重复
 * 设计要点：children 放表单，footer 放"去注册/去登录"链接——页面对齐一致
 */
export default function AuthShell({ title, subtitle, children, footer }: AuthShellProps) {
  return (
    <div style={{ maxWidth: 400, margin: '64px auto' }}>
      <Card>
        <Typography.Title level={3} style={{ textAlign: 'center', marginBottom: 4 }}>
          {title}
        </Typography.Title>
        {subtitle && (
          <Typography.Paragraph
            type="secondary"
            style={{ textAlign: 'center', marginBottom: 20, fontSize: 13 }}
          >
            {subtitle}
          </Typography.Paragraph>
        )}
        {children}
        {footer && <div style={{ textAlign: 'center' }}>{footer}</div>}
      </Card>
    </div>
  );
}
