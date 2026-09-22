import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { App, Button, Card, Empty, Modal, Space, Tag, Typography } from 'antd';
import { workApi } from '../api';
import type { Work } from '../types';

/**
 * 我的作品库（第 6 课）：任务成功后自动落库的作品卡片网格。
 * 支持：视频预览（Modal）+ 重新生成（复用草稿再提交）+ 下载链接。
 */
export default function WorksPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [works, setWorks] = useState<Work[]>([]);
  const [loading, setLoading] = useState(true);
  const [playing, setPlaying] = useState<Work | null>(null);

  const load = () => {
    // 注意：不再在 effect 内同步 setLoading(true)——初始 state 已是 true，
    // 避免触发 react-hooks/set-state-in-effect（同步 setState 导致级联渲染）
    workApi
      .list()
      .then(({ data }) => setWorks(data.data))
      .catch(() => message.error('作品加载失败'))
      .finally(() => setLoading(false));
  };

  useEffect(load, []); // eslint-disable-line react-hooks/exhaustive-deps

  /** 重新生成：复用原草稿提交新任务 → 跳生成页（SSE 实时进度） */
  const onRegenerate = async (work: Work) => {
    try {
      const { data } = await workApi.regenerate(work.id);
      message.success(`已提交 v${work.version + 1} 生成任务`);
      navigate(`/generating?taskId=${data.data.id}`);
    } catch {
      message.error('重新生成失败（额度不足或草稿正在生成中）');
    }
  };

  return (
    <div style={{ maxWidth: 960, margin: '0 auto' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          我的作品
        </Typography.Title>
        <Button type="primary" onClick={() => navigate('/wizard')}>
          + 开始创作
        </Button>
      </Space>

      {works.length === 0 && !loading ? (
        <Empty description="还没有作品，去创作一个 AI 动画吧">
          <Button type="primary" onClick={() => navigate('/wizard')}>
            开始创作
          </Button>
        </Empty>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(230px, 1fr))',
            gap: 14,
          }}
        >
          {works.map((work) => (
            <Card
              key={work.id}
              loading={loading}
              cover={
                <div
                  onClick={() => setPlaying(work)}
                  style={{
                    height: 130,
                    background: 'linear-gradient(135deg, #8BC8EA, #94D8C3)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    fontSize: 40,
                  }}
                >
                  ▶️
                </div>
              }
              hoverable
            >
              <Card.Meta
                title={
                  <Space>
                    <Typography.Text ellipsis style={{ maxWidth: 130 }}>
                      {work.title}
                    </Typography.Text>
                    <Tag color="blue">v{work.version}</Tag>
                  </Space>
                }
                description={
                  <Space direction="vertical" size={4} style={{ width: '100%' }}>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {new Date(work.createdAt).toLocaleString()}
                    </Typography.Text>
                    <Space>
                      <Button size="small" type="primary" onClick={() => setPlaying(work)}>
                        播放
                      </Button>
                      <Button size="small" onClick={() => onRegenerate(work)}>
                        重新生成
                      </Button>
                      {work.videoUrl && (
                        <Button size="small" href={work.videoUrl} target="_blank">
                          下载
                        </Button>
                      )}
                    </Space>
                  </Space>
                }
              />
            </Card>
          ))}
        </div>
      )}

      {/* 视频播放 Modal（原生 video 标签，antd 无内置） */}
      <Modal
        open={!!playing}
        title={playing?.title}
        footer={null}
        width={560}
        onCancel={() => setPlaying(null)}
      >
        {playing?.videoUrl && (
          <video
            src={playing.videoUrl}
            controls
            autoPlay
            style={{ width: '100%', borderRadius: 8 }}
          />
        )}
      </Modal>
    </div>
  );
}
