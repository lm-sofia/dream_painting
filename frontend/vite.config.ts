import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    // 开发代理：/api 转发到后端（默认 Java 8080，可用 VITE_API_BASE 覆盖）
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 代码分包：antd / react / 业务代码分离，利用浏览器缓存（Vite 8 Rolldown 语法）
    rolldownOptions: {
      output: {
        codeSplitting: {
          groups: [
            {
              name: 'react',
              test: /node_modules[\\/](react|react-dom|react-router-dom)[\\/]/,
            },
            {
              name: 'antd',
              test: /node_modules[\\/](antd|@ant-design)[\\/]/,
            },
            {
              name: 'vendor',
              test: /node_modules[\\/](axios|zustand)[\\/]/,
            },
          ],
        },
      },
    },
    // antd 是各懒加载页面的共享依赖，必须整包提前加载；原始 748KB / gzip 239KB 属正常体积
    chunkSizeWarningLimit: 800,
  },
});
