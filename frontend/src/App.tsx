import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Spin } from 'antd';
import AppLayout from './components/AppLayout';
import { RedirectIfAuthed, RequireAuth } from './components/RouteGuards';

/**
 * 路由懒加载：React.lazy 按路由切分包
 * - 首屏只加载当前页面需要的 JS（antd 660KB 不再一次性全下载）
 * - Suspense fallback = 切页时的加载占位
 */
const HomePage = lazy(() => import('./pages/HomePage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const ArticleListPage = lazy(() => import('./pages/ArticleListPage'));
const ArticleDetailPage = lazy(() => import('./pages/ArticleDetailPage'));
const ArticleEditorPage = lazy(() => import('./pages/ArticleEditorPage'));
const StylePickerPage = lazy(() => import('./pages/StylePickerPage'));
const WizardPage = lazy(() => import('./pages/WizardPage'));
const GeneratingPage = lazy(() => import('./pages/GeneratingPage'));
const WorksPage = lazy(() => import('./pages/WorksPage'));

function PageFallback() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}>
      <Spin />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      {/* Suspense：异步加载占位。配合下方 lazy() 懒加载路由，
          页面代码块下载完成前，先渲染 fallback 里的 Spin loading */}
      <Suspense fallback={<PageFallback />}>
        <Routes>
          {/* 守卫 RedirectIfAuthed：已登录(有token)则跳首页 /，否则放行子路由
              用途：防止已登录用户再进入登录/注册页 */}
          <Route element={<RedirectIfAuthed />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>

          {/* 守卫 RequireAuth：未登录(无token)则跳 /login，否则放行子路由
              用途：保护需要登录才能访问的页面 */}
          <Route element={<RequireAuth />}>
            {/* AppLayout：全局外壳，渲染顶部导航栏 + <Outlet /> 内容区
                子路由页面会被插入到 Content 的 Outlet 位置 */}
            <Route element={<AppLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/wizard" element={<WizardPage />} />
              <Route path="/generating" element={<GeneratingPage />} />
              <Route path="/works" element={<WorksPage />} />
              <Route path="/styles" element={<StylePickerPage />} />
              <Route path="/articles" element={<ArticleListPage />} />
              <Route path="/articles/new" element={<ArticleEditorPage />} />
              <Route path="/articles/:id" element={<ArticleDetailPage />} />
              <Route path="/articles/:id/edit" element={<ArticleEditorPage />} />
            </Route>
          </Route>

          {/* 兜底 */}
          <Route path="*" element={<LoginPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
