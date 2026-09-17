import { apiClient } from './client';
import type {
  Article,
  ArticlePayload,
  AuthResponse,
  HealthStatus,
  PageResult,
  User,
} from '../types';

export const authApi = {
  register: (data: { username: string; email: string; password: string }) =>
    apiClient.post<{ code: number; data: User }>('/auth/register', data),

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
