/** 统一接口类型定义，与 database/API_CONTRACT.md 对齐 */

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  phone?: string;
  level: number;
  continuousDays: number;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

/** 风格库（第 2 课） */
export interface Style {
  id: number;
  name: string;
  description?: string;
  category: string;
  coverUrl?: string;
  promptTemplate?: string;
  sortOrder: number;
  active: boolean;
  createdAt: string;
}

export interface StylePayload {
  name: string;
  description?: string;
  category: string;
  coverUrl?: string;
  promptTemplate?: string;
  sortOrder?: number;
  active?: boolean;
}

/** 创作草稿（第 3 课：三步向导） */
export interface Draft {
  id: number;
  userId: number;
  title: string;
  idea?: string;
  styleId?: number;
  duration: number;
  ratio: '9:16' | '16:9' | '1:1';
  voiceover: boolean;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface DraftPayload {
  title: string;
  idea?: string;
  styleId?: number;
  duration?: number;
  ratio?: string;
  voiceover?: boolean;
}

/** 生成任务（第 4 课：6 状态状态机） */
export type TaskStatus = 'PENDING' | 'AGENTS' | 'RENDERING' | 'COMPILING' | 'SUCCESS' | 'FAILED';

export interface GenerationTask {
  id: number;
  draftId: number;
  status: TaskStatus;
  progress: number;
  /** 当前智能体（AGENTS 状态） */
  agentStage?: AgentStage;
  /** 智能体内部进度 0-100 */
  agentProgress?: number;
  stageMessage?: string;
  errorMessage?: string;
  videoUrl?: string;
  createdAt: string;
  updatedAt: string;
}

/** 7 智能体流水线（第 5 课） */
export type AgentStage =
  'SCREENWRITER' | 'CHARACTER' | 'SCENE' | 'ANIMATION' | 'EDITING' | 'SOUND' | 'DIRECTOR';

/** 智能体展示元信息（顺序即流水线顺序） */
export const AGENT_STAGE_META: { key: AgentStage; label: string; icon: string }[] = [
  { key: 'SCREENWRITER', label: '编剧', icon: '✍️' },
  { key: 'CHARACTER', label: '角色', icon: '🧑‍🎨' },
  { key: 'SCENE', label: '场景', icon: '🏞️' },
  { key: 'ANIMATION', label: '动画', icon: '🎬' },
  { key: 'EDITING', label: '剪辑', icon: '✂️' },
  { key: 'SOUND', label: '音效', icon: '🎵' },
  { key: 'DIRECTOR', label: '导演', icon: '🎥' },
];

/** 作品（第 6 课：任务 SUCCESS 自动落库） */
export interface Work {
  id: number;
  draftId: number;
  taskId: number;
  title: string;
  videoUrl?: string;
  coverUrl?: string;
  version: number;
  createdAt: string;
}

/** 状态 → 展示文案/颜色（前端统一映射，不散落各处） */
export const TASK_STATUS_META: Record<TaskStatus, { label: string; color: string }> = {
  PENDING: { label: '排队中', color: 'default' },
  AGENTS: { label: '智能体创作中', color: 'processing' },
  RENDERING: { label: '渲染中', color: 'processing' },
  COMPILING: { label: '合成中', color: 'processing' },
  SUCCESS: { label: '已完成', color: 'success' },
  FAILED: { label: '失败', color: 'error' },
};

export interface ArticleListItem {
  id: number;
  title: string;
  author: string;
  published: boolean;
  createdAt: string;
}

export interface Article extends ArticleListItem {
  content: string;
  authorId: number;
  updatedAt: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface ArticlePayload {
  title: string;
  content: string;
  published: boolean;
}

/** 统一响应信封 */
export interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
}

export interface HealthStatus {
  status: string;
  service: string;
  db: string;
  redis?: string;
  timestamp: string;
}
