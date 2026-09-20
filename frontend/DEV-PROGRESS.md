# 🎨 前端开发进度（绘梦工坊）

> 此文件记录前端开发到哪一步了。**每次开发完必须更新**，下次继续开发前先读本文件。
> 最后更新：2026-09-20（第 6 课 作品页 + 测试 完成，M1 六课全部收官）

---

## 📌 当前进度

| 里程碑                 | 状态    | 说明                                                 |
| ---------------------- | ------- | ---------------------------------------------------- |
| 第 1 课 认证模块产品化 | ✅ 完成 | 手机号/用户名双登录 + 游戏化字段                     |
| 第 2 课 风格库模块     | ✅ 完成 | 类型/API/组件/页面/路由懒加载 + 660KB 警告解决       |
| 第 3 课 三步向导       | ✅ 完成 | WizardPage 三步 + 防抖自动保存 + 草稿续创            |
| 第 4 课 生成任务状态机 | ✅ 完成 | GeneratingPage 提交 + 2s 轮询 + 进度条 + 结果/失败态 |
| 第 5 课 7 智能体 + SSE | ✅ 完成 | SSE 实时推送（降级轮询）+ AgentTimeline 组件         |
| 第 6 课 作品页 + 测试  | ✅ 完成 | 作品库 + 重新生成 + Playwright 全链路 3/3            |

---

## ✅ 已完成内容

### 第 1 课：认证模块产品化（已完成）

- `src/types/index.ts`：User 类型 + `phone?/level/continuousDays`
- `src/api/index.ts`：`authApi.register` 支持 `phone?`；login 语义扩展（用户名或手机号）
- `src/stores/auth.ts`：+`fetchMe()` 异步 action（刷新后同步服务器端等级/天数）
- `src/components/AuthShell.tsx`（新）：认证页共用布局组件（消除登录/注册页重复）
- `src/pages/LoginPage.tsx`：复用 AuthShell，placeholder「用户名 / 手机号」，品牌名「绘梦工坊」
- `src/pages/RegisterPage.tsx`：复用 AuthShell，+手机号选填输入（正则 `/^$|^1[3-9]\d{9}$/`）

### 第 2 课：风格库（已完成）

- `src/types/index.ts`：+`Style`/`StylePayload` 接口
- `src/api/index.ts`：+`styleApi`（list/detail/create/update/remove）
- `src/components/StyleCard.tsx`（新）：风格卡片（封面占位/选中态/键盘可访问）
- `src/pages/StylePickerPage.tsx`（新）：风格选择页（Segmented 分类筛选 + 自适应网格 + 三态渲染）
- `src/App.tsx`：路由懒加载改造（React.lazy + Suspense + PageFallback），+`/styles` 路由
- `src/components/AppLayout.tsx`：品牌改「绘梦工坊」+ 风格库导航入口
- `vite.config.ts`：`chunkSizeWarningLimit` 600→700（antd 共享依赖整包体积 gzip 215KB 属正常）

### 第 3 课：三步向导（已完成）

- `src/types/index.ts`：+`Draft`/`DraftPayload` 接口
- `src/api/index.ts`：+`draftApi`（list/detail/create/update/remove）
- `src/pages/WizardPage.tsx`（新）：三步向导（Steps：创意→风格→配置）
  - **防抖自动保存**：useRef 定时器 + onValuesChange → 停手 1.5s 自动落库
  - **增量保存**：有 draftId 走 PUT，无则 POST 后记住 id
  - 风格选择复用 StyleCard（Form.Item shouldUpdate 联动）
  - `?draftId=` 断点续创（加载已有草稿）
  - 最后一步保存后跳 `/generating`（第 4 课占位）
- `src/App.tsx`：+`/wizard` 路由（懒加载）
- `src/components/AppLayout.tsx`：+「创作」导航入口

### 第 4 课：生成任务页（已完成）

- `src/types/index.ts`：+`GenerationTask`（6 状态）+ `TASK_STATUS_META`（状态→文案/颜色映射，收敛一处）
- `src/api/index.ts`：+`taskApi`（submit/list/detail）
- `src/pages/GeneratingPage.tsx`（新）：`/generating`
  - 首次进入（?draftId=）→ 自动提交任务；返回继续看（?taskId=）→ 直接轮询
  - **2s 轮询**（第 5 课升级 SSE）；组件卸载清理定时器（防内存泄漏）
  - Progress 进度条 + 状态 Tag + 阶段文案；SUCCESS/FAILED 走 Result 结果页
- `src/pages/WizardPage.tsx`：保存后跳 `/generating?draftId=xx`
- `src/App.tsx`：+`/generating` 路由（懒加载）
- `vite.config.ts`：chunkSizeWarningLimit 700→800（antd 748KB/gzip 239KB 正常）

### 第 5 课：SSE + 7 智能体时间线（已完成）

- `src/types/index.ts`：+`AgentStage`（7 智能体）+ `AGENT_STAGE_META`（顺序即流水线）
- `src/utils/sse.ts`（新）：**subscribeSSE 工具**（fetch + ReadableStream 手解 SSE 线协议；为什么不用 EventSource——无法带 Authorization header；onData/onDone/onError 三回调 + 取消函数）
- `src/components/AgentTimeline.tsx`（新）：7 智能体垂直 Steps（finish/process/wait 三态 + 当前智能体内部进度 %）
- `src/pages/GeneratingPage.tsx`：**SSE 主通道 + 失败自动降级 2s 轮询**（健壮性）；AgentTimeline 展示；终态停止订阅 + 卸载清理

### 第 6 课：作品页 + 测试（已完成，M1 收官）

- `src/types/index.ts`：+`Work` 接口（id/draftId/taskId/title/videoUrl/version/createdAt）
- `src/api/index.ts`：+`workApi`（list/detail/regenerate）
- `src/pages/WorksPage.tsx`（新）：`/works` 我的作品库
  - **卡片网格**（`repeat(auto-fill, minmax(220px,1fr))` 自适应）+ 版本 Tag（v1/v2…）+ 创建时间
  - **视频播放**：原生 `<video>` + antd Modal（antd 无内置播放器）；下载按钮直链视频
  - **重新生成**：`workApi.regenerate` → 复用草稿提交新任务 → 跳生成页（SSE 实时）
  - 空态引导（Empty + 开始创作按钮）
- `src/components/StyleCard.tsx`：+`data-testid="style-card"`（E2E 稳定定位，不依赖 UI 文本/样式）
- `src/pages/GeneratingPage.tsx`：SUCCESS 结果页 +「查看作品」按钮（跳 /works）；subTitle 固定文案避免与标题重复
- `src/pages/WizardPage.tsx`：**修复两个真实 bug**（见踩坑记录：antd getFieldsValue 无参坑 + navigate 闭包旧 id）
- `src/App.tsx`：+`/works` 路由（懒加载）；`src/components/AppLayout.tsx`：+「作品」导航
- `e2e/user-journey.spec.ts`：新增「AI 创作全链路」describe——**注册→创作→SSE 生成→作品库可见 3/3 通过**

---

## 🔨 待办（下一步）

### M2 展望（等用户指示开课）

- [ ] 社区广场（作品公开 + 点赞/收藏 + 瀑布流）
- [ ] 模板/挑战赛/会员/企业 API（PRD v2.0 范围）
- [ ] .NET 后端（backend-dotnet）同步第 1-6 课全部改动

---

## 🚀 常用命令

```bash
# 开发启动（注意用 Node 22，勿用 D:\360Install\nodejs 的 v20）
cd D:\AI-project\frontend
& "C:\Program Files\nodejs\npm.cmd" run dev        # 开发（5173，/api 代理到 8080）
& "C:\Program Files\nodejs\npm.cmd" run build      # 生产构建（tsc + vite build）
& "C:\Program Files\nodejs\npm.cmd" run lint       # ESLint
npx playwright test                                 # E2E 测试
```

## ⚠️ 踩坑记录

- **Node 版本**：必须 Node ≥ 20.19（用 C:\Program Files\nodejs，v22.14.0）。`D:\360Install\nodejs` 的 v20.12.2 会导致 Rolldown 报 `ERR_INVALID_ARG_VALUE`。
- **构建警告**：antd chunk 660KB > 600KB 阈值 → 第 2 课用路由懒加载解决。
- **antd Form.getFieldsValue() 无参坑（第 6 课实战）**：无参只返回**当前已挂载**字段的值！三步向导切到 Step3 后，Step0 的 title 字段已卸载 → 取值丢 title → 保存静默失败。**修法：`form.getFieldsValue(['title','idea',...])` 按字段名显式取**（值仍保留在 store）。
- **React 闭包旧值（第 6 课实战）**：`navigate()` 里用 state（如 draftId）会读到**本次渲染闭包**的旧值（setState 是异步的）。修法：把异步操作返回的值直接用于跳转，不依赖闭包 state。
- **antd 双字按钮插空格（E2E 断言）**：antd 会在双字按钮文本中间插空格（「保存并开始生成」→「保 存 并 开 始 生 成」），E2E 正则必须写 `/保\s*存\s*并\s*开\s*始\s*生\s*成/`。
- **Playwright strict mode**：`getByText` 匹配到多个元素直接报错（如 Result 标题+副标题都含同一文案）→ 用 `.first()` 或保证文案唯一。
- **react-hooks/set-state-in-effect（CI lint 实战）**：新版 eslint-plugin-react-hooks 禁止 effect 内**同步** setState（级联渲染）。`useEffect(load, [])` 里 load 首行 `setLoading(true)` 被拦 → 初始 state 已是 true，删冗余调用即可（异步回调里的 setState 不受限）。
