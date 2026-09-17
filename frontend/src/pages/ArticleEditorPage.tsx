import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, Switch, Space, App, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { articleApi } from '../api';
import { useAuthStore } from '../stores/auth';
import type { ArticlePayload } from '../types';

/** 新建/编辑文章共用页面：/articles/new 与 /articles/:id/edit */
export default function ArticleEditorPage() {
  const [form] = Form.useForm<ArticlePayload>();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const { message } = App.useApp();
  const [saving, setSaving] = useState(false);
  const isEdit = Boolean(id);

  useEffect(() => {
    if (isEdit && id) {
      articleApi.detail(Number(id)).then((res) => {
        const article = res.data.data;
        form.setFieldsValue({
          title: article.title,
          content: article.content,
          published: article.published,
        });
      });
    }
  }, [id, isEdit, form]);

  // 非作者不可编辑
  if (isEdit && user) {
    // 编辑权限在提交时由后端校验，这里不做阻断，仅提示
  }

  const onFinish = async (values: ArticlePayload) => {
    setSaving(true);
    try {
      if (isEdit && id) {
        await articleApi.update(Number(id), values);
        message.success('文章已更新');
      } else {
        await articleApi.create(values);
        message.success('文章已创建');
      }
      navigate('/articles');
    } catch {
      // 错误提示已由拦截器处理
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card title={isEdit ? '编辑文章' : '写文章'}>
      <Typography.Paragraph type="secondary">
        作者：{user?.username ?? '未登录'}（后端将校验你的身份）
      </Typography.Paragraph>
      <Form<ArticlePayload>
        form={form}
        layout="vertical"
        onFinish={onFinish}
        initialValues={{ published: false }}
      >
        <Form.Item
          name="title"
          label="标题"
          rules={[
            { required: true, message: '请输入标题' },
            { max: 200, message: '标题不能超过 200 字' },
          ]}
        >
          <Input placeholder="文章标题" />
        </Form.Item>
        <Form.Item name="content" label="正文">
          <Input.TextArea rows={12} placeholder="支持纯文本内容" />
        </Form.Item>
        <Form.Item name="published" label="是否发布" valuePropName="checked">
          <Switch checkedChildren="发布" unCheckedChildren="草稿" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>
              {isEdit ? '保存修改' : '创建文章'}
            </Button>
            <Button onClick={() => navigate('/articles')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}
