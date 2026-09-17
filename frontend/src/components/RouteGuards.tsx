import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../stores/auth';

/** 路由守卫：未登录跳转登录页 */
export function RequireAuth() {
  const token = useAuthStore((s) => s.token);
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

/** 已登录用户访问登录/注册页时重定向到首页 */
export function RedirectIfAuthed() {
  const token = useAuthStore((s) => s.token);
  if (token) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
