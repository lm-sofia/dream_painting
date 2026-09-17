import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Tag, Typography, Space, App } from 'antd';
import { healthApi } from '../api';
import { useAuthStore } from '../stores/auth';
import type { HealthStatus } from '../types';

export default function HomePage() {
  const user = useAuthStore((s) => s.user);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const { message } = App.useApp();

  useEffect(() => {
    healthApi
      .check()
      .then((res) => setHealth(res.data.data))
      .catch(() => {
        // 后端未启动时静默降级
        message.warning('后端服务未启动，无法获取健康状态');
      });
  }, [message]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card>
        <Typography.Title level={3} style={{ marginTop: 0 }}>
          欢迎使用 AI-Project 🎉
        </Typography.Title>
        <Typography.Paragraph>
          这是一个 <Tag color="blue">Java</Tag> + <Tag color="green">.NET</Tag>{' '}
          双后端驱动的全栈脚手架。
          {user ? (
            <>
              当前登录：<Tag color="gold">{user.username}</Tag>（{user.role}）
            </>
          ) : (
            '请先登录后体验完整功能。'
          )}
        </Typography.Paragraph>
      </Card>

      <Card title="系统健康状态">
        <Row gutter={16}>
          <Col span={6}>
            <Statistic
              title="服务"
              value={health?.status ?? '-'}
              valueStyle={{ color: health?.status === 'UP' ? '#52c41a' : '#999' }}
            />
          </Col>
          <Col span={6}>
            <Statistic title="后端" value={health?.service ?? '-'} valueStyle={{ fontSize: 16 }} />
          </Col>
          <Col span={6}>
            <Statistic
              title="数据库"
              value={health?.db ?? '-'}
              valueStyle={{ color: health?.db === 'UP' ? '#52c41a' : '#ff4d4f' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Redis"
              value={health?.redis ?? 'N/A'}
              valueStyle={{ color: health?.redis === 'UP' ? '#52c41a' : '#999' }}
            />
          </Col>
        </Row>
      </Card>
    </Space>
  );
}
