import { create } from 'zustand';
// persist：Zustand 持久化中间件，把 store 状态自动写入 localStorage，
// 页面刷新后自动恢复，避免登录态(token/user)丢失
import { persist } from 'zustand/middleware';
import { authApi } from '../api';
import type { User } from '../types';

interface AuthState {
  token: string | null;
  user: User | null;
  setAuth: (token: string, user: User) => void;
  setUser: (user: User) => void;
  logout: () => void;
  /** 拉取最新用户信息（登录态刷新/进入首页时调用，同步等级/天数） */
  fetchMe: () => Promise<void>;
}

/** 认证状态：token 持久化到 localStorage，刷新不丢失 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      setAuth: (token, user) => set({ token, user }),
      setUser: (user) => set({ user }),
      logout: () => set({ token: null, user: null }),
      fetchMe: async () => {
        if (!get().token) return;
        try {
          const { data } = await authApi.me();
          set({ user: data.data });
        } catch {
          // 401 已由拦截器统一处理（登出跳转）
        }
      },
    }),
    {
      name: 'ai-project-auth',
      // 这行代码是 persist 中间件的配置项 partialize，意思是：只把 store 中的 token 和 user 这两个字段持久化到 localStorage，其他字段不存。
      partialize: (state) => ({ token: state.token, user: state.user }),
    },
  ),
);
