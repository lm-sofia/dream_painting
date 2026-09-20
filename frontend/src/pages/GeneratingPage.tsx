import { useEffect, useRef, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { App, Button, Progress, Result, Space, Tag, Typography } from 'antd';
import { taskApi } from '../api';
import { useAuthStore } from '../stores/auth';
import { subscribeSSE } from '../utils/sse';
import type { GenerationTask } from '../types';
import { TASK_STATUS_META } from '../types';
import AgentTimeline from '../components/AgentTimeline';

/**
 * 生成进度页（第 5 课）：SSE 实时推送 + 自动降级轮询。
 * 进入方式：/generating?draftId=1（首次提交）或 /generating?taskId=1（返回查看）
 *
 * 连接策略（教学点）：
 * 1. 首选 SSE：服务端推送（GET /tasks/{id}/stream），省请求、实时
 * 2. 降级轮询：SSE 连接失败/中断 → 自动回退 2s 轮询（健壮性兜底）
 */
export default function GeneratingPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const token = useAuthStore((s) => s.token);
  const [searchParams] = useSearchParams();
  const draftId = searchParams.get('draftId');
  const taskIdParam = searchParams.get('taskId');

  const [task, setTask] = useState<GenerationTask | null>(null);
  const [submitting, setSubmitting] = useState(!!draftId && !taskIdParam);
  const [usePolling, setUsePolling] = useState(false); // SSE 失败后切轮询
  const cancelSseRef = useRef<(() => void) | null>(null);
  const pollRef = useRef<number | undefined>(undefined);

  const isDone = (t: GenerationTask | null) =>
    !!t && (t.status === 'SUCCESS' || t.status === 'FAILED');

  /** 提交任务（首次进入且有 draftId） */
  useEffect(() => {
    if (draftId && !taskIdParam) {
      taskApi
        .submit(Number(draftId))
        .then(({ data }) => setTask(data.data))
        .catch(() => message.error('提交生成任务失败'))
        .finally(() => setSubmitting(false));
    }
  }, [draftId, taskIdParam, message]);

  /** 任务 id 确定后：订阅 SSE（失败自动降级轮询） */
  useEffect(() => {
    const id = taskIdParam ? Number(taskIdParam) : task?.id;
    if (!id || !token) return;

    // 已有 taskId（返回查看）：先同步查一次，终态直接展示不再订阅
    if (taskIdParam) {
      taskApi
        .detail(id)
        .then(({ data }) => {
          setTask(data.data);
          if (!isDone(data.data)) {
            startSse(id);
          }
        })
        .catch(() => message.error('任务加载失败'));
    } else {
      startSse(id);
    }

    function startSse(taskId: number) {
      if (!token) return;
      cancelSseRef.current = subscribeSSE(
        `/api/v1/tasks/${taskId}/stream`,
        token,
        {
          onData: (t) => {
            setTask(t);
            if (isDone(t)) cancelSseRef.current?.();
          },
          onDone: () => setUsePolling(false),
          onError: () => {
            // SSE 不可用 → 降级轮询兜底
            setUsePolling(true);
          },
        },
      );
    }

    return () => cancelSseRef.current?.();
  }, [taskIdParam, task?.id, token, message]);

  /** 降级轮询：2s 一次（仅 usePolling=true 时启动） */
  useEffect(() => {
    if (!usePolling || !task?.id) return;
    const poll = () => {
      taskApi
        .detail(task.id)
        .then(({ data }) => {
          setTask(data.data);
          if (isDone(data.data)) window.clearInterval(pollRef.current);
        })
        .catch(() => message.error('状态获取失败'));
    };
    poll();
    pollRef.current = window.setInterval(poll, 2000);
    return () => window.clearInterval(pollRef.current);
  }, [usePolling, task?.id, message]);

  // 提交中
  if (submitting) {
    return (
      <div style={{ maxWidth: 560, margin: '48px auto', textAlign: 'center' }}>
        <Typography.Title level={4}>正在提交生成任务…</Typography.Title>
        <Progress percent={100} status="active" showInfo={false} />
      </div>
    );
  }

  // 成功
  if (task?.status === 'SUCCESS') {
    return (
      <Result
        status="success"
        title="生成完成 🎉"
        subTitle="7 位智能体已交付成片，前往作品库查看与下载"
        extra={[
          <Button type="primary" key="view" onClick={() => navigate('/works')}>
            查看作品
          </Button>,
          <Button key="again" onClick={() => navigate('/wizard')}>
            再创作一个
          </Button>,
        ]}
      />
    );
  }

  // 失败
  if (task?.status === 'FAILED') {
    return (
      <Result
        status="error"
        title="生成失败"
        subTitle={task.errorMessage || '未知错误，请重试'}
        extra={
          <Button type="primary" onClick={() => navigate('/wizard')}>
            返回修改草稿
          </Button>
        }
      />
    );
  }

  const meta = task ? TASK_STATUS_META[task.status] : null;

  return (
    <div style={{ maxWidth: 560, margin: '0 auto', paddingTop: 24 }}>
      <Typography.Title level={3}>生成中…</Typography.Title>
      {task && (
        <>
          <Space style={{ marginBottom: 8 }}>
            <Tag color={meta?.color}>{meta?.label}</Tag>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              任务 #{task.id} · {new Date(task.createdAt).toLocaleTimeString()} 提交
              {usePolling && ' · 轮询模式'}
            </Typography.Text>
          </Space>

          <Progress
            percent={task.progress}
            status="active"
            strokeColor={{ from: '#94D8C3', to: '#8BC8EA' }}
          />

          {/* 7 智能体流水线（AGENTS 阶段实时推进） */}
          <AgentTimeline task={task} />

          <div style={{ marginTop: 8, textAlign: 'center' }}>
            <Typography.Text style={{ fontSize: 15, color: '#1A1B1C' }}>
              {task.stageMessage || '排队中…'}
            </Typography.Text>
          </div>
        </>
      )}

      <div style={{ textAlign: 'center', marginTop: 16 }}>
        <Link to="/wizard">← 返回创作</Link>
      </div>
    </div>
  );
}
