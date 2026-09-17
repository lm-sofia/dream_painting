import { Layout, Menu, Dropdown, Avatar, Button, Space, Typography } from 'antd';
import { UserOutlined, LogoutOutlined, HomeOutlined } from '@ant-design/icons';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/auth';

const { Header, Content } = Layout;

/** 全局布局：顶部导航 + 内容区 */
export default function AppLayout() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const selectedKey = location.pathname.startsWith('/articles') ? '/articles' : '/';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
            AI-Project
          </Typography.Title>
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[selectedKey]}
            items={[
              { key: '/', icon: <HomeOutlined />, label: <Link to="/">首页</Link> },
              { key: '/articles', icon: <span>📄</span>, label: <Link to="/articles">文章</Link> },
            ]}
            style={{ flex: 1, minWidth: 0 }}
          />
        </div>

        {user ? (
          <Dropdown
            menu={{
              items: [
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: handleLogout,
                },
              ],
            }}
          >
            <Space style={{ cursor: 'pointer', color: '#fff' }}>
              <Avatar icon={<UserOutlined />} />
              <span>{user.username}</span>
            </Space>
          </Dropdown>
        ) : (
          <Button type="primary" onClick={() => navigate('/login')}>
            登录
          </Button>
        )}
      </Header>
      <Content style={{ padding: 24, maxWidth: 960, width: '100%', margin: '0 auto' }}>
        <Outlet />
      </Content>
    </Layout>
  );
}
