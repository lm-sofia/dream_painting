import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { ApiError, ErrorCodes } from '../utils/error';
import { useAuthStore } from '../stores/auth';
import type { ApiEnvelope } from '../types';

/**
 * Axios 实例：统一 baseURL（可通过 VITE_API_BASE 切换 Java/.NET 后端）
 * - 请求拦截器：自动附加 JWT Bearer Token
 * - 响应拦截器：统一解包 {code, message, data} 信封
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api/v1',
  timeout: 10000,
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const envelope = response.data as ApiEnvelope<unknown>;
    if (envelope && typeof envelope.code === 'number' && envelope.code !== 0) {
      return Promise.reject(new ApiError(envelope.code, envelope.message, response.status));
    }
    return response;
  },
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    // 401：token 失效，清理登录态
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    const serverMessage = error.response?.data?.message;
    const apiError = new ApiError(
      error.response?.data?.code ?? ErrorCodes.InternalError,
      serverMessage ?? error.message,
      error.response?.status ?? 0,
    );
    message.error(apiError.message);
    return Promise.reject(apiError);
  },
);
