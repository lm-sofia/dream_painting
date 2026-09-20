import { Button, Form, Input, Typography, App } from 'antd';
import { LockOutlined, MailOutlined, MobileOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import AuthShell from '../components/AuthShell';

interface RegisterForm {
  username: string;
  email: string;
  phone?: string;
  password: string;
  confirm: string;
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();

  const onFinish = async (values: RegisterForm) => {
    try {
      await authApi.register({
        username: values.username,
        email: values.email,
        phone: values.phone || undefined,
        password: values.password,
      });
      message.success('注册成功，请登录');
      navigate('/login');
    } catch {
      // 错误提示已由 axios 拦截器统一处理
    }
  };

  return (
    <AuthShell
      title="注册账号"
      subtitle="加入绘梦工坊，开启 AI 动画创作"
      footer={
        <>
          <Typography.Text>已有账号？</Typography.Text> <Link to="/login">去登录</Link>
        </>
      }
    >
      <Form<RegisterForm> onFinish={onFinish} size="large">
        <Form.Item
          name="username"
          rules={[
            { required: true, message: '请输入用户名' },
            { min: 3, max: 50, message: '用户名长度 3-50' },
          ]}
        >
          <Input prefix={<UserOutlined />} placeholder="用户名（3-50 位）" />
        </Form.Item>
        <Form.Item
          name="email"
          rules={[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '邮箱格式不合法' },
          ]}
        >
          <Input prefix={<MailOutlined />} placeholder="邮箱" />
        </Form.Item>
        <Form.Item
          name="phone"
          rules={[{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不合法' }]}
        >
          <Input
            prefix={<MobileOutlined />}
            placeholder="手机号（可选，登录可用手机号）"
            maxLength={11}
          />
        </Form.Item>
        <Form.Item
          name="password"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 8, max: 64, message: '密码长度 8-64' },
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="密码（至少 8 位）" />
        </Form.Item>
        <Form.Item
          name="confirm"
          dependencies={['password']}
          rules={[
            { required: true, message: '请确认密码' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('两次输入的密码不一致'));
              },
            }),
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>
            注册
          </Button>
        </Form.Item>
      </Form>
    </AuthShell>
  );
}
