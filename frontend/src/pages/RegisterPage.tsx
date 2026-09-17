import { Button, Card, Form, Input, Typography, App } from 'antd';
import { LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';

interface RegisterForm {
  username: string;
  email: string;
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
        password: values.password,
      });
      message.success('注册成功，请登录');
      navigate('/login');
    } catch {
      // 错误提示已由 axios 拦截器统一处理
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '80px auto' }}>
      <Card>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          注册账号
        </Typography.Title>
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
        <div style={{ textAlign: 'center' }}>
          <Typography.Text>已有账号？</Typography.Text> <Link to="/login">去登录</Link>
        </div>
      </Card>
    </div>
  );
}
