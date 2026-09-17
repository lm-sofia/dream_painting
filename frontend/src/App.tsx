import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import { RedirectIfAuthed, RequireAuth } from './components/RouteGuards';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ArticleListPage from './pages/ArticleListPage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import ArticleEditorPage from './pages/ArticleEditorPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 已登录访问登录/注册页 -> 重定向首页 */}
        <Route element={<RedirectIfAuthed />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        {/* 需要登录的页面 */}
        <Route element={<RequireAuth />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/articles" element={<ArticleListPage />} />
            <Route path="/articles/new" element={<ArticleEditorPage />} />
            <Route path="/articles/:id" element={<ArticleDetailPage />} />
            <Route path="/articles/:id/edit" element={<ArticleEditorPage />} />
          </Route>
        </Route>

        {/* 兜底 */}
        <Route path="*" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  );
}
