import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../stores/auth';

/** 路由守卫：未登录跳转登录页 */
export function RequireAuth() {
  const token = useAuthStore((s) => s.token);
  if (!token) {
    // 无 token：重定向到登录页（replace 替换历史记录，避免回退又回到这里）
    return <Navigate to="/login" replace />;
  }
  // <Outlet />：渲染当前匹配到的子路由页面（即守卫放行，让受保护的页面显示出来）
  return <Outlet />;
}

/** 已登录用户访问登录/注册页时重定向到首页 */
export function RedirectIfAuthed() {
  const token = useAuthStore((s) => s.token);
  if (token) {
    // 已有 token：说明已登录，跳首页，不让再进登录/注册页
    return <Navigate to="/" replace />;
  }
  // <Outlet />：放行，渲染子路由（登录页/注册页）
  return <Outlet />;
}
