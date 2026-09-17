/** 统一接口类型定义，与 database/API_CONTRACT.md 对齐 */

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

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
