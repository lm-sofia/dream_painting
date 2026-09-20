import { apiClient } from './client';
import type {
  Article,
  ArticlePayload,
  AuthResponse,
  Draft,
  DraftPayload,
  GenerationTask,
  HealthStatus,
  PageResult,
  Style,
  StylePayload,
  User,
  Work,
} from '../types';

export const authApi = {
  register: (data: { username: string; email: string; password: string; phone?: string }) =>
    apiClient.post<{ code: number; data: User }>('/auth/register', data),

  /** login 的 username 字段语义扩展：用户名或手机号（与后端 identifier 对齐） */
  login: (data: { username: string; password: string }) =>
    apiClient.post<{ code: number; data: AuthResponse }>('/auth/login', data),

  me: () => apiClient.get<{ code: number; data: User }>('/auth/me'),
};

export const articleApi = {
  list: (page = 1, pageSize = 10) =>
    apiClient.get<{ code: number; data: PageResult<Article> }>('/articles', {
      params: { page, pageSize },
    }),

  detail: (id: number) => apiClient.get<{ code: number; data: Article }>(`/articles/${id}`),

  create: (payload: ArticlePayload) =>
    apiClient.post<{ code: number; data: Article }>('/articles', payload),

  update: (id: number, payload: ArticlePayload) =>
    apiClient.put<{ code: number; data: Article }>(`/articles/${id}`, payload),

  remove: (id: number) => apiClient.delete<{ code: number; data: null }>(`/articles/${id}`),
};

export const healthApi = {
  check: () => apiClient.get<{ code: number; data: HealthStatus }>('/health'),
};

/** 风格库（公开读；管理写需 ADMIN，一般前端不用） */
export const styleApi = {
  list: () => apiClient.get<{ code: number; data: Style[] }>('/styles'),
  detail: (id: number) => apiClient.get<{ code: number; data: Style }>(`/styles/${id}`),
  create: (payload: StylePayload) =>
    apiClient.post<{ code: number; data: Style }>('/styles', payload),
  update: (id: number, payload: StylePayload) =>
    apiClient.put<{ code: number; data: Style }>(`/styles/${id}`, payload),
  remove: (id: number) => apiClient.delete<{ code: number; data: null }>(`/styles/${id}`),
};

/** 创作草稿（三步向导自动保存） */
export const draftApi = {
  list: () => apiClient.get<{ code: number; data: Draft[] }>('/drafts'),
  detail: (id: number) => apiClient.get<{ code: number; data: Draft }>(`/drafts/${id}`),
  create: (payload: DraftPayload) =>
    apiClient.post<{ code: number; data: Draft }>('/drafts', payload),
  update: (id: number, payload: DraftPayload) =>
    apiClient.put<{ code: number; data: Draft }>(`/drafts/${id}`, payload),
  remove: (id: number) => apiClient.delete<{ code: number; data: null }>(`/drafts/${id}`),
};

/** 生成任务（提交草稿 → 轮询状态） */
export const taskApi = {
  submit: (draftId: number) =>
    apiClient.post<{ code: number; data: GenerationTask }>('/tasks', { draftId }),
  list: () => apiClient.get<{ code: number; data: GenerationTask[] }>('/tasks'),
  detail: (id: number) => apiClient.get<{ code: number; data: GenerationTask }>(`/tasks/${id}`),
};

/** 作品库（第 6 课） */
export const workApi = {
  list: () => apiClient.get<{ code: number; data: Work[] }>('/works'),
  detail: (id: number) => apiClient.get<{ code: number; data: Work }>(`/works/${id}`),
  regenerate: (id: number) =>
    apiClient.post<{ code: number; data: GenerationTask }>(`/works/${id}/regenerate`),
};
