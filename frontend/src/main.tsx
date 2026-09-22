import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ConfigProvider, App as AntApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import App from './App';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#e8d44d',
          colorBgBase: '#0d0d0f',
          colorBgContainer: '#1a1a1d',
          colorBgElevated: '#2a2a2d',
          colorText: '#fff',
          colorTextSecondary: '#888',
          colorTextTertiary: '#666',
          colorBorder: '#2a2a2d',
          colorBorderSecondary: '#1e1e21',
          colorTextPlaceholder: '#666',
        },
        components: {
          Layout: {
            bodyBg: '#0d0d0f',
            headerBg: '#141416',
            siderBg: '#141416',
          },
          Menu: {
            darkItemBg: 'transparent',
            darkItemSelectedBg: '#2a2a2d',
          },
          Input: {
            colorBgContainer: '#1a1a1d',
            colorBorder: '#333',
            colorTextPlaceholder: '#666',
            colorText: '#fff',
          },
          Steps: {
            colorText: '#fff',
            colorTextDescription: '#888',
          },
          Form: {
            labelColor: '#fff',
          },
          Card: {
            colorBgContainer: '#1a1a1d',
            colorBorderSecondary: '#2a2a2d',
          },
        },
      }}
    >
      <AntApp>
        <App />
      </AntApp>
    </ConfigProvider>
  </StrictMode>,
);
