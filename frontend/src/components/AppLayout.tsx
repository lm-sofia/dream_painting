import { Layout, Menu, Avatar, Typography, Popover, Button } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  PlusOutlined,
  HomeOutlined,
  FireOutlined,
  PictureOutlined,
  BookOutlined,
  DollarOutlined,
  UsergroupAddOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/auth';

const { Sider, Content } = Layout;

/**
 * 全局布局：左侧边栏 + 主内容区（深色创作平台风格）
 * 参考：OiOi 深色工作台设计，保留绘梦工坊品牌
 */
export default function AppLayout() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const selectedKey = location.pathname.startsWith('/articles')
    ? '/articles'
    : location.pathname.startsWith('/styles')
      ? '/styles'
      : location.pathname.startsWith('/wizard')
        ? '/wizard'
        : location.pathname.startsWith('/works')
          ? '/works'
          : '/';

  // 用户悬浮卡片内容（参考 OiOi）
  const userPopoverContent = (
    <div style={{ width: 280, background: '#1a1a1d', borderRadius: 12, padding: 20 }}>
      {/* 头部：头像 + 用户名 + 积分 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
        <Avatar size={48} icon={<UserOutlined />} style={{ background: '#e8d44d' }} />
        <div style={{ flex: 1 }}>
          <div style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>{user?.username}</div>
          <div style={{ color: '#888', fontSize: 12, marginTop: 2 }}>
            {user?.email || user?.phone || '未绑定邮箱'}
          </div>
        </div>
      </div>

      {/* 积分卡片 */}
      <div
        style={{
          background: '#2a2a2d',
          borderRadius: 10,
          padding: '12px 16px',
          marginBottom: 16,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <div>
          <div style={{ color: '#888', fontSize: 11 }}>剩余积分</div>
          <div style={{ color: '#e8d44d', fontSize: 20, fontWeight: 700 }}>
            10 <span style={{ fontSize: 12, color: '#888', fontWeight: 400 }}>次/月</span>
          </div>
        </div>
        <Button
          type="primary"
          size="small"
          style={{ background: '#e8d44d', color: '#1a1a1d', border: 'none' }}
        >
          充值
        </Button>
      </div>

      {/* 菜单列表 */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
        <div style={menuItemStyle}>
          <UserOutlined />
          <span>个人中心与作品</span>
        </div>
        <div style={menuItemStyle}>
          <UsergroupAddOutlined />
          <span>邀请好友</span>
          <span style={{ color: '#e8d44d', fontSize: 11 }}>+20 积分</span>
        </div>
        <div style={menuItemStyle}>
          <QuestionCircleOutlined />
          <span>帮助与反馈</span>
        </div>
        <div style={{ ...menuItemStyle, color: '#ff4d4f' }} onClick={handleLogout}>
          <LogoutOutlined />
          <span>退出登录</span>
        </div>
      </div>
    </div>
  );

  return (
    <Layout style={{ minHeight: '100vh', background: '#0d0d0f' }}>
      {/* 左侧边栏 */}
      <Sider
        width={220}
        style={{
          background: '#141416',
          borderRight: '1px solid #262629',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          zIndex: 100,
          overflowY: 'auto',
        }}
      >
        {/* Logo */}
        <div style={{ padding: '24px 20px 16px' }}>
          <Typography.Title level={4} style={{ color: '#e8d44d', margin: 0, fontWeight: 700 }}>
            ✨ 绘梦工坊
          </Typography.Title>
          <Typography.Text style={{ color: '#666', fontSize: 12 }}>
            AI 动画短视频创作平台
          </Typography.Text>
        </div>

        {/* 新建项目按钮 */}
        <div style={{ padding: '0 16px 16px' }}>
          <Link to="/wizard">
            <div
              style={{
                background: '#2a2a2d',
                borderRadius: 8,
                padding: '10px 14px',
                color: '#fff',
                fontSize: 14,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                transition: 'background 0.2s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#3a3a3d')}
              onMouseLeave={(e) => (e.currentTarget.style.background = '#2a2a2d')}
            >
              <PlusOutlined />
              新建创作
            </div>
          </Link>
        </div>

        {/* 导航菜单 */}
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          style={{ background: 'transparent', border: 'none' }}
          items={[
            {
              key: '/',
              icon: <HomeOutlined />,
              label: <Link to="/">首页工作台</Link>,
            },
            {
              key: '/wizard',
              icon: <span>✨</span>,
              label: <Link to="/wizard">AI 创作</Link>,
            },
            {
              key: '/works',
              icon: <FireOutlined />,
              label: <Link to="/works">我的作品</Link>,
            },
            {
              key: '/styles',
              icon: <PictureOutlined />,
              label: <Link to="/styles">风格库</Link>,
            },
            {
              key: '/articles',
              icon: <BookOutlined />,
              label: <Link to="/articles">创作教程</Link>,
            },
          ]}
        />

        {/* 底部用户信息（悬浮弹出详细卡片） */}
        <div style={{ position: 'absolute', bottom: 16, left: 0, right: 0, padding: '0 16px' }}>
          {user && (
            <Popover
              content={userPopoverContent}
              trigger="hover"
              placement="topRight"
              overlayInnerStyle={{ background: 'transparent', padding: 0, boxShadow: 'none' }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 10,
                  padding: '10px 12px',
                  borderRadius: 8,
                  cursor: 'pointer',
                  background: '#1e1e21',
                  transition: 'background 0.2s',
                }}
                onMouseEnter={(e) => (e.currentTarget.style.background = '#2a2a2d')}
                onMouseLeave={(e) => (e.currentTarget.style.background = '#1e1e21')}
              >
                <Avatar size={36} icon={<UserOutlined />} style={{ background: '#e8d44d' }} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ color: '#fff', fontSize: 13, fontWeight: 500 }}>
                    {user.username}
                  </div>
                  <div
                    style={{
                      color: '#e8d44d',
                      fontSize: 11,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 4,
                    }}
                  >
                    <DollarOutlined />
                    剩余积分: 10
                  </div>
                </div>
              </div>
            </Popover>
          )}
        </div>
      </Sider>

      {/* 主内容区（避开侧边栏） */}
      <Layout style={{ marginLeft: 220, background: '#0d0d0f' }}>
        <Content style={{ padding: '32px 40px', minHeight: '100vh' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}

const menuItemStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 10,
  padding: '10px 12px',
  borderRadius: 8,
  color: '#ccc',
  fontSize: 13,
  cursor: 'pointer',
  transition: 'background 0.2s',
};
