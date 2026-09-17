import { useEffect, useState } from 'react';
import { Card, Typography, Tag, Space, Button, Popconfirm, App, Spin } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { articleApi } from '../api';
import { useAuthStore } from '../stores/auth';
import type { Article } from '../types';

export default function ArticleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [article, setArticle] = useState<Article | null>(null);
  const [loading, setLoading] = useState(true);
  const user = useAuthStore((s) => s.user);
  const navigate = useNavigate();
  const { message } = App.useApp();

  useEffect(() => {
    if (!id) return;
    articleApi
      .detail(Number(id))
      .then((res) => setArticle(res.data.data))
      .catch(() => message.error('文章不存在或无权查看'))
      .finally(() => setLoading(false));
  }, [id, message]);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!article) {
    return <Typography.Text type="secondary">文章不存在</Typography.Text>;
  }

  const isOwner = user?.id === article.authorId;
  const isAdmin = user?.role === 'ADMIN';
  const canManage = isOwner || isAdmin;

  const handleDelete = async () => {
    try {
      await articleApi.remove(article.id);
      message.success('文章已删除');
      navigate('/articles');
    } catch {
      // 拦截器已提示
    }
  };

  return (
    <Card
      title={
        <Space>
          {article.title}
          {article.published ? <Tag color="green">已发布</Tag> : <Tag color="orange">草稿</Tag>}
        </Space>
      }
      extra={
        canManage && (
          <Space>
            <Button type="primary" onClick={() => navigate(`/articles/${article.id}/edit`)}>
              编辑
            </Button>
            <Popconfirm
              title="确定删除这篇文章？"
              onConfirm={handleDelete}
              okText="删除"
              cancelText="取消"
            >
              <Button danger>删除</Button>
            </Popconfirm>
          </Space>
        )
      }
    >
      <Typography.Paragraph type="secondary" style={{ marginTop: 0 }}>
        作者：{article.author} ｜ 创建于 {new Date(article.createdAt).toLocaleString()} ｜ 更新于{' '}
        {new Date(article.updatedAt).toLocaleString()}
      </Typography.Paragraph>
      <Typography.Paragraph style={{ whiteSpace: 'pre-wrap', marginTop: 24 }}>
        {article.content || '（暂无内容）'}
      </Typography.Paragraph>
    </Card>
  );
}
