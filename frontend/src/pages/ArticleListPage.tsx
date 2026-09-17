import { useEffect, useState } from 'react';
import { Card, List, Pagination, Typography, Tag, Empty, Space, App } from 'antd';
import { Link } from 'react-router-dom';
import { articleApi } from '../api';
import { useAuthStore } from '../stores/auth';
import type { Article } from '../types';

export default function ArticleListPage() {
  const [articles, setArticles] = useState<Article[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const user = useAuthStore((s) => s.user);
  const { message } = App.useApp();

  useEffect(() => {
    articleApi
      .list(page, 10)
      .then((res) => {
        setArticles(res.data.data.items);
        setTotal(res.data.data.total);
      })
      .catch(() => message.error('加载文章失败'))
      .finally(() => setLoading(false));
  }, [page, message]);

  const handlePageChange = (nextPage: number) => {
    setPage(nextPage);
    setLoading(true);
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card
        title="文章列表"
        extra={
          user && (
            <Link to="/articles/new">
              <Typography.Link>+ 写文章</Typography.Link>
            </Link>
          )
        }
      >
        {articles.length === 0 && !loading ? (
          <Empty description="暂无文章" />
        ) : (
          <List
            loading={loading}
            dataSource={articles}
            renderItem={(item) => (
              <List.Item
                actions={[
                  <Link key="detail" to={`/articles/${item.id}`}>
                    查看详情
                  </Link>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      {item.title}
                      {item.published ? (
                        <Tag color="green">已发布</Tag>
                      ) : (
                        <Tag color="orange">草稿</Tag>
                      )}
                    </Space>
                  }
                  description={
                    <Space>
                      <span>作者：{item.author}</span>
                      <span>创建：{new Date(item.createdAt).toLocaleString()}</span>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
        {total > 10 && (
          <Pagination
            current={page}
            total={total}
            pageSize={10}
            onChange={handlePageChange}
            style={{ marginTop: 16, textAlign: 'center' }}
          />
        )}
      </Card>
    </Space>
  );
}
