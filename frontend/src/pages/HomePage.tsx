import { Button, Card, Tag, Typography, Row, Col } from 'antd';
import {
  PlusOutlined,
  PlayCircleOutlined,
  PictureOutlined,
  BookOutlined,
  RightOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/auth';

const { Title, Paragraph, Text } = Typography;

/**
 * 首页工作台：深色创作平台风格
 * 布局：顶部大标题 → 居中创作卡片 → 功能入口 → 底部精选作品
 */
export default function HomePage() {
  const user = useAuthStore((s) => s.user);
  const navigate = useNavigate();

  const features = [
    {
      icon: <PlusOutlined />,
      title: 'AI 创作',
      desc: '三步生成动画视频',
      path: '/wizard',
      color: '#e8d44d',
    },
    {
      icon: <PlayCircleOutlined />,
      title: '我的作品',
      desc: '管理与下载生成的视频',
      path: '/works',
      color: '#8bc8ea',
    },
    {
      icon: <PictureOutlined />,
      title: '风格库',
      desc: '8 种动画风格一键切换',
      path: '/styles',
      color: '#94d8c3',
    },
    {
      icon: <BookOutlined />,
      title: '创作教程',
      desc: '提示词技巧与最佳实践',
      path: '/articles',
      color: '#eaa7b2',
    },
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* 顶部大标题区 */}
      <div style={{ textAlign: 'center', marginBottom: 48 }}>
        <Title level={2} style={{ color: '#fff', marginBottom: 12, fontWeight: 600 }}>
          ✨ 绘梦工坊 · AI 动画创作 Agent
        </Title>
        <Paragraph style={{ color: '#888', fontSize: 15 }}>
          一句话创意 → 7 位智能体协作 → 1 分钟生成动画短视频
        </Paragraph>
        <div style={{ marginTop: 16 }}>
          <Tag color="gold" style={{ marginRight: 8 }}>
            免费用户每月 10 次创作
          </Tag>
          <Tag color="blue">{user?.level ? `${user.level} 级用户` : '新用户'}</Tag>
        </div>
      </div>

      {/* 居中创作大卡片 */}
      <Card
        style={{
          background: '#1a1a1d',
          border: '1px solid #2a2a2d',
          borderRadius: 16,
          marginBottom: 40,
        }}
        styles={{ body: { padding: '40px 32px' } }}
      >
        <div style={{ textAlign: 'center' }}>
          <Title level={4} style={{ color: '#fff', marginTop: 0 }}>
            🎬 开始你的动画创作
          </Title>
          <Paragraph style={{ color: '#888', marginBottom: 32 }}>
            写下你的创意，选择风格，AI 自动完成剧本 → 分镜 → 画面 → 配音
          </Paragraph>
          <Button
            type="primary"
            size="large"
            icon={<PlusOutlined />}
            onClick={() => navigate('/wizard')}
            style={{
              background: '#e8d44d',
              border: 'none',
              color: '#1a1a1d',
              fontWeight: 600,
              height: 48,
              padding: '0 40px',
              fontSize: 16,
            }}
          >
            立即创作
          </Button>
        </div>
      </Card>

      {/* 功能入口网格 */}
      <Row gutter={[20, 20]} style={{ marginBottom: 48 }}>
        {features.map((f) => (
          <Col key={f.path} xs={12} sm={12} md={6}>
            <Card
              hoverable
              onClick={() => navigate(f.path)}
              style={{
                background: '#1a1a1d',
                border: '1px solid #2a2a2d',
                borderRadius: 12,
                textAlign: 'center',
              }}
              styles={{ body: { padding: '24px 16px' } }}
            >
              <div
                style={{
                  width: 48,
                  height: 48,
                  borderRadius: 12,
                  background: `${f.color}20`,
                  color: f.color,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 20,
                  margin: '0 auto 12px',
                }}
              >
                {f.icon}
              </div>
              <Title level={5} style={{ color: '#fff', marginBottom: 4 }}>
                {f.title}
              </Title>
              <Text style={{ color: '#666', fontSize: 12 }}>{f.desc}</Text>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 底部：快速开始指南 */}
      <div>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 16,
          }}
        >
          <Title level={5} style={{ color: '#fff', margin: 0 }}>
            🚀 快速上手三步
          </Title>
          <Button type="text" style={{ color: '#888', fontSize: 12 }}>
            查看完整教程 <RightOutlined />
          </Button>
        </div>
        <Row gutter={[16, 16]}>
          {[
            { step: '1', title: '写下创意', desc: '用一句话描述你想做的动画' },
            { step: '2', title: '选择风格', desc: '8 种风格库任选，一键切换' },
            { step: '3', title: 'AI 生成', desc: '7 智能体协作，1 分钟出片' },
          ].map((s) => (
            <Col key={s.step} span={8}>
              <Card
                style={{
                  background: '#1a1a1d',
                  border: '1px solid #2a2a2d',
                  borderRadius: 12,
                }}
                styles={{ body: { padding: '20px' } }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                  <div
                    style={{
                      width: 28,
                      height: 28,
                      borderRadius: '50%',
                      background: '#e8d44d',
                      color: '#1a1a1d',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 700,
                      flexShrink: 0,
                    }}
                  >
                    {s.step}
                  </div>
                  <div>
                    <div style={{ color: '#fff', fontWeight: 500, marginBottom: 4 }}>{s.title}</div>
                    <Text style={{ color: '#666', fontSize: 12 }}>{s.desc}</Text>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  );
}
