import { Button, Card, Form, Input, Typography, App } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import { useAuthStore } from '../stores/auth';

interface LoginForm {
  username: string;
  password: string;
}

export default function LoginPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const { message } = App.useApp();

  const onFinish = async (values: LoginForm) => {
    try {
      const { data } = await authApi.login(values);
      setAuth(data.data.token, data.data.user);
      message.success(`欢迎回来，${data.data.user.username}`);
      navigate('/');
    } catch {
      // 错误提示已由 axios 拦截器统一处理
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '80px auto' }}>
      <Card>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          登录 AI-Project
        </Typography.Title>
        <Form<LoginForm> onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          <Typography.Text>还没有账号？</Typography.Text> <Link to="/register">立即注册</Link>
        </div>
        <Typography.Paragraph
          type="secondary"
          style={{ marginTop: 16, textAlign: 'center', marginBottom: 0 }}
        >
          演示账号：admin / admin123 或 alice / user123
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
