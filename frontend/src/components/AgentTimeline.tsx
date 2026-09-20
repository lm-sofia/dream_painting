import { Steps } from 'antd';
import { AGENT_STAGE_META } from '../types';
import type { GenerationTask } from '../types';

interface Props {
  task: GenerationTask;
}

/**
 * 7 智能体流水线时间线（第 5 课）。
 * 状态判定：按流水线顺序，当前智能体 = process（显示内部进度），之前 = finish，之后 = wait。
 * 任务不在 AGENTS 阶段（PENDING/渲染/合成/终态）时全部显示 wait 或 finish。
 */
export default function AgentTimeline({ task }: Props) {
  const { agentStage, agentProgress, status } = task;

  // 当前智能体的流水线序号（-1 = 还没开始智能体阶段）
  const currentIndex = agentStage ? AGENT_STAGE_META.findIndex((s) => s.key === agentStage) : -1;

  const isAgentsPhase = status === 'AGENTS';

  const items = AGENT_STAGE_META.map((stage, idx) => {
    const finished = isAgentsPhase
      ? idx < currentIndex || (idx === currentIndex && (agentProgress ?? 0) >= 100)
      : status === 'SUCCESS' || status === 'FAILED' || idx < currentIndex;
    const active = isAgentsPhase && idx === currentIndex && (agentProgress ?? 0) < 100;

    return {
      title: (
        <span style={{ fontSize: 13 }}>
          {stage.icon} {stage.label}
          {active && (
            <span style={{ color: '#8BC8EA', fontSize: 11, marginLeft: 6 }}>{agentProgress}%</span>
          )}
          {finished && <span style={{ color: '#52C41A', fontSize: 11, marginLeft: 6 }}>✓</span>}
        </span>
      ),
      status: finished ? ('finish' as const) : active ? ('process' as const) : ('wait' as const),
    };
  });

  return (
    <Steps
      direction="vertical"
      size="small"
      current={Math.max(currentIndex, 0)}
      items={items}
      style={{ margin: '12px 0' }}
    />
  );
}
