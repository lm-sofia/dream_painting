import { Button, Form, Input, Typography, App } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import { useAuthStore } from '../stores/auth';
import AuthShell from '../components/AuthShell';

interface LoginForm {
  username: string; // 语义扩展：用户名或手机号（与后端 identifier 对齐）
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
    <AuthShell
      title="绘梦工坊"
      subtitle="30 秒，把任何脑洞变成能火的动画短视频"
      footer={
        <>
          <Typography.Text>还没有账号？</Typography.Text> <Link to="/register">立即注册</Link>
        </>
      }
    >
      <Form<LoginForm> onFinish={onFinish} size="large">
        <Form.Item name="username" rules={[{ required: true, message: '请输入用户名或手机号' }]}>
          <Input prefix={<UserOutlined />} placeholder="用户名 / 手机号" />
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
      <Typography.Paragraph type="secondary" style={{ textAlign: 'center', marginBottom: 0 }}>
        演示账号：admin / admin123 或 alice / user123
      </Typography.Paragraph>
    </AuthShell>
  );
}
