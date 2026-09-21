# 🖥️ 后端开发进度（绘梦工坊：Java + .NET 双后端）

> 此文件记录后端开发到哪一步了。**每次开发完必须更新**，下次继续开发前先读本文件。
> 最后更新：2026-09-20（第 6 课 作品模块 Java 完成，M1 六课全部收官）

---

## 📌 当前进度

| 里程碑 | 状态 | 说明 |
|---|---|---|
| 第 1 课 认证模块产品化 | ✅ 完成 | 手机号注册/登录 + 游戏化字段（Java 已改，.NET 待同步） |
| 第 2 课 风格库模块 | ✅ Java 完成 | CRUD + 缓存可插拔 + 权限矩阵已验证；.NET 待同步 |
| 第 3 课 三步向导 | ✅ Java 完成 | drafts CRUD + 越权防护已验证；.NET 待同步 |
| 第 4 课 生成任务状态机 | ✅ Java 完成 | 6 状态机 + 幂等 + 额度扣减 + 队列可插拔已验证；.NET 待同步 |
| 第 5 课 7 智能体 + SSE | ✅ Java 完成 | SSE 实时推送 + 7 智能体流水线已验证；.NET 待同步 |
| 第 6 课 作品模块 | ✅ Java 完成 | works CRUD + 版本递增 + 契约测试 44/44 已验证；.NET 待同步 |

---

## ✅ 已完成内容

### 第 1 课：认证模块产品化（Java 完成）
- `user/User.java`：+`phone`（可空+唯一约束 uq_users_phone）、`level=1`、`continuousDays=0`
- `user/UserRepository.java`：+`findByPhone`、`findByUsernameOrPhone`（用户名或手机号）、`existsByPhone`
- `user/dto/RegisterRequest.java`：+`phone` 可选字段 + 手机号正则 `^$|^1[3-9]\d{9}$`
- `user/UserService.java`：注册支持手机号唯一校验 + 游戏化字段初始化
- `user/AuthController.java`：login 改用 `findByUsernameOrPhone`（username 字段语义扩展为 identifier）
- `user/dto/UserResponse.java`：+`phone/level/continuousDays`
- `src/main/resources/seed-data.sql`：INSERT 列清单同步（H2 无 DEFAULT 必须显式给列）

### 第 2 课：风格库（Java 完成）
- `style/Style.java`（新）：风格实体（name/description/category/coverUrl/promptTemplate/sortOrder/active）
- `style/StyleRepository.java`（新）：`findByActiveTrueOrderBySortOrderAsc` 等
- `style/dto/StyleRequest.java`、`style/dto/StyleResponse.java`（新）
- `style/StyleService.java`（新）：`@Cacheable` 列表/详情 + `@CacheEvict` 管理写 + 软删除（下架）
- `style/StyleController.java`（新）：`GET /api/v1/styles`（公开）、`GET /styles/{id}`（公开）、POST/PUT/DELETE（`@PreAuthorize("hasRole('ADMIN')")`）
- `config/CacheConfig.java`（新）：缓存可插拔——生产 `RedisCacheManager`（TTL 1h + JSON 序列化 + 禁 null），test `ConcurrentMapCacheManager`
- `config/SecurityConfig.java`：白名单 + `GET /api/v1/styles/**` permitAll
- `database/init/01_create_schema.sql`：+styles 表（含 ck_styles_category CHECK 约束 + 索引）
- `database/init/02_seed_data.sql`：+8 风格种子（6 分类，ON CONFLICT 幂等）
- `seed-data.sql`（H2）：+styles 种子（显式带 active/created_at/updated_at 列）
- **验证通过**：公开读 8 风格 ✓ / 未登录 401 ✓ / 普通用户 403 ✓ / ADMIN 创建 ✓ / @CacheEvict 刷新 ✓ / 契约测试 23/23 ✓

### 第 3 课：三步向导草稿（Java 完成）
- `draft/Draft.java`（新）：草稿实体（userId/title/idea/styleId/duration/ratio/voiceover/status）+ 复合索引
- `draft/DraftRepository.java`（新）：`findByUserIdOrderByUpdatedAtDesc` + **`findByIdAndUserId`（越权防护核心）**
- `draft/dto/DraftRequest.java`（新）：@Min/@Max 时长 + @Pattern 比例（服务端兜底校验）
- `draft/dto/DraftResponse.java`（新）
- `draft/DraftService.java`（新）：create/list/detail/update/delete 全带 userId 守卫；**越权统一返回 404（不泄露资源存在性）**
- `draft/DraftController.java`（新）：`/api/v1/drafts` CRUD，userId 从 `@AuthenticationPrincipal` 注入（不信任客户端传值）
- `database/init/01_create_schema.sql`：+drafts 表（CHECK ratio/status + idx_drafts_user_updated）
- **验证通过**：创建/更新/列表/删除 ✓ / 未登录 401 ✓ / **越权读改删全部 404 ✓（admin 攻击 alice 草稿被拒）** / 契约测试 23/23 ✓

### 第 4 课：生成任务状态机（Java 完成）
- `common/ErrorCode.java`：+`INSUFFICIENT_QUOTA(40201, PAYMENT_REQUIRED)`（额度耗尽）
- `task/TaskStatus.java`（新）：**6 状态枚举 + 迁移表 + transitionTo() 统一守门**（非法迁移 409）
- `task/GenerationTask.java`（新）：任务实体（userId/draftId/status/progress/stageMessage/errorMessage/videoUrl）
- `task/TaskRepository.java`（新）：findByIdAndUserId（越权）+ existsByDraftIdAndStatusIn（幂等）
- `task/TaskQueue.java` + `RedisTaskQueue` + `InMemoryTaskQueue`（新）：**队列可插拔**（生产 Redis Stream / 测试进程内）
- `task/TaskService.java`（新）：submit（草稿归属→幂等→建任务→扣费→**事务提交后入队**）+ list/get + advance/fail（worker 内部）
- `task/TaskController.java`（新）：POST /tasks / GET /tasks / GET /tasks/{id}
- `task/SimulatedTaskWorker.java`（新，test profile）：@Scheduled 每 2 秒推进一格，**未终态重新入队**（队列语义坑已修复）
- `credit/CreditAccount.java` / `CreditTransaction.java`（新）：额度账户 + 流水（幂等键唯一约束）
- `credit/CreditAccountRepository.java`（新）：**@Lock(PESSIMISTIC_WRITE) 悲观锁查账户**（防并发超扣）
- `credit/CreditService.java`（新）：幂等检查→行锁→余额校验→扣减→记流水；懒创建账户 + 跨月懒重置
- `AiProjectApplication.java`：+`@EnableScheduling`
- `database/init/01_create_schema.sql`：+3 表（generation_tasks / credit_accounts / credit_transactions）+ **部分唯一索引 uq_tasks_draft_running**
- **踩坑记录**：① `month` 是 SQL 保留字 → 列名改 `billing_month`；② worker 消费后未重新入队 → 状态卡死 → 修复"未终态 re-enqueue"
- **验证通过**：提交任务 201+PENDING ✓ / 运行中重复提交 409 ✓ / 状态机完整流转（PENDING→AGENTS→RENDERING→COMPILING→SUCCESS）✓ / 越权读 404 ✓ / 完成任务后可重提交 ✓ / **额度 10 次精确耗尽，第 11 次 402** ✓ / 契约测试 23/23 ✓

### 第 5 课：7 智能体 + SSE（Java 完成）
- `task/AgentStage.java`（新）：7 智能体枚举（编剧/角色/场景/动画/剪辑/音效/导演）+ 中文 label + 阶段目标进度 + next()
- `task/GenerationTask.java`：+agentStage/agentProgress 字段（AGENTS 状态细粒度进度）
- `task/TaskProgressNotifier.java`（新）：**SseEmitter 注册表（观察者模式）**，key=userId:taskId，推送失败全捕获（不影响主流程）
- `task/TaskService.java`：+advanceAgent（**PENDING→AGENTS 迁移 + 智能体推进**，每次推进后 publish）+ advance/fail 集成 SSE 推送
- `task/TaskController.java`：+`GET /tasks/{id}/stream`（text/event-stream，先校验归属再订阅）
- `task/SimulatedTaskWorker.java`：升级 1 秒节拍 7 智能体流水线（50%→100% 每格）
- `config/SecurityConfig.java`：+`dispatcherTypeMatchers(ASYNC, ERROR).permitAll()`（**SSE 异步 dispatch 安全坑**）
- `database/init/01_create_schema.sql`：+agent_stage/agent_progress 列（ALTER TABLE）
- **踩坑记录**：① **SSE + Spring Security 异步坑**：SseEmitter 完成时 async dispatch 再走过滤链 → AccessDenied → 放行 ASYNC/ERROR 分发；② **publish 推送异常穿透**：curl 断开触发 IOException 未捕获 → 任务被误标 FAILED → catch 全异常；③ **advanceAgent 忘迁移 PENDING→AGENTS**（状态机守门在 service 补上）
- **验证通过**：SSE 实时收到 7 智能体事件流（SCREENWRITER 50→100→CHARACTER...）✓ / **越权订阅 404** ✓ / 契约测试 23/23 ✓

### 第 6 课：作品模块（Java 完成，M1 收官）
- `work/Work.java`（新）：作品实体——userId/draftId/taskId/title/videoUrl/coverUrl/**version** + 唯一约束 `uq_works_draft_version (draft_id, version)` + 索引 `idx_works_user_created`
- `work/WorkRepository.java`（新）：`findByUserIdOrderByCreatedAtDesc` / `findByIdAndUserId`（越权防护）/ `countByDraftId`（版本号计算）
- `work/WorkService.java`（新）：list / detail（越权=404）/ **regenerate（复用草稿再提交任务，闭环）**
- `work/WorkController.java`（新）：`GET /works`、`GET /works/{id}`、`POST /works/{id}/regenerate`（201）
- `task/TaskService.java`：+**`complete(taskId, videoUrl)`**——COMPILING→SUCCESS + 进度 100 + **同事务自动落作品**（标题取自草稿、version=该草稿已有作品数+1）
- `task/SimulatedTaskWorker.java`：COMPILING → complete(演示视频外链，生产=对象存储 URL)
- `database/init/01_create_schema.sql`：+works 表（三处同步铁律再执行）
- **验证通过**：任务 SUCCESS 自动落作品 ✓ / regenerate 后 version v1→v2 ✓ / **越权读作品 404** ✓ / **契约测试 23→44/44**（新增 [7]风格 [8]草稿越权 [9]任务幂等/越权/SSE [10]作品 [11]删除）✓
- **本次实战修复的坑（前端侧，前端文档同记录）**：antd `getFieldsValue()` 无参只返回已挂载字段（跨 Step 取值丢 title）；`navigate` 闭包旧 state；Playwright 双字按钮插空格 + strict mode
- **CI 实战三坑（2026-09-20 云端首跑排查）**：
  ① **Redis 缓存序列化 500**：生产 profile 走 Redis 缓存，`GenericJackson2JsonRedisSerializer` 默认 ObjectMapper 无 JSR310 → DTO 的 `Instant` 字段写缓存即 500（test 用内存缓存不暴露）→ CacheConfig 注入 `JavaTimeModule` + `activateDefaultTyping`（泛型 List 反序列化）
  ② **旧库缺列**：`CREATE TABLE IF NOT EXISTS` 对已存在表不生效 → 01_create_schema.sql 的 users 补幂等 `ALTER TABLE ADD COLUMN IF NOT EXISTS` + DO 块加约束（PG 不支持 `ADD CONSTRAINT IF NOT EXISTS`）
  ③ **契约测试 --all 双端**：.NET 仅骨架（Auth/Articles/Health 3 接口）→ 暂改 Java 单端跑，.NET 同步后恢复

---

## 🔨 待办（下一步）

### M2 展望（等用户指示开课）
- [ ] 社区广场（作品公开 + 点赞/收藏）
- [ ] 模板/挑战赛/会员/企业 API（PRD v2.0 范围）
- [ ] **.NET 后端同步第 1-6 课全部改动**（契约测试 --all 双后端跑通；用户此前明确"后端使用 java 和.net"）
- [ ] .NET 端同步第 1-5 课改动
- [ ] 更新 docs/后端接口文档.md（含 styles/drafts/tasks/SSE）

---

## 🚀 常用命令

```bash
# Java（H2 测试环境）
cd D:\AI-project\backend-java
mvn spring-boot:run "-Dspring-boot.run.profiles=test"   # 启动 8080
mvn compile                                              # 编译检查

# Java（真实 PG + Redis，需先 docker compose up -d）
cd D:\AI-project\docker && docker compose up -d          # PG+Redis 容器
cd D:\AI-project\backend-java && mvn spring-boot:run     # 默认 profile 连 PG

# .NET
cd D:\AI-project\backend-dotnet\AiProject.Api
$env:ASPNETCORE_ENVIRONMENT="Test"
dotnet run --no-build --no-launch-profile --urls http://localhost:5080

# 契约测试
cd D:\AI-project
python tests/contract_test.py --base-url http://localhost:8080/api/v1   # 单测 Java
python tests/contract_test.py --all                                     # 双后端
```

## ⚠️ 踩坑记录
- **H2 vs PG 差异**：test profile 用 H2 由 JPA `ddl-auto=create-drop` 建表——**没有 DEFAULT**，所以种子 INSERT 必须显式列出所有 NOT NULL 列（第 1 课 CONTINUOUS_DAYS、第 2 课 ACTIVE 都踩过）。PG 的 01_create_schema.sql 有 DEFAULT，02_seed_data.sql 无需列。
- **旧进程占用 8080**：启动前先 `Get-NetTCPConnection -LocalPort 8080` 检查，有残留 java 进程会跑旧代码（表现为新接口 401）。
- **端口**：Java 8080 / .NET 5080 / 前端 5173 / PG 5432 / Redis 6379。

## 第 7 课：真实生成引擎接入 + 密钥安全（2026-09-20）

### 做了什么
- **generator 包（可插拔引擎）**：`VideoGenerator` 接口 + `GenerateRequest`/`GeneratorStatus` + 两个实现：
  - `MockVideoGenerator`（默认 provider=mock）：无 Key 也能跑通全链路，6 秒返回占位视频
  - `SeedanceVideoGenerator`（provider=seedance）：火山方舟 API 适配器（submit/query 异步任务模型），Key 只从环境变量注入
- **RealTaskWorker（生产 worker）**：消费 Redis 队列 → 组装草稿描述+风格模板 → 提交引擎 → 轮询 → 7 智能体推进+SSE → SUCCESS 落作品；基于 DB 状态分支推进，worker 重启可续；10 分钟超时保护
- **密钥安全三原则**：密钥永不进 Git（.gitignore 排除 backend-java/.env）；仓库只放 `.env.example` 占位模板；前端零密钥（Key 只在后端环境变量）
- **配套**：GenerationTask +provider_task_id（幂等 ALTER 迁移）；application.yml +ai.video 配置（可插拔 provider）；start-prod.ps1 启动脚本（读 .env → 注入环境变量）

### 实战踩坑（3 个，全本地复现验证）
1. **游离实体 set 不落库**：worker 直接 task.setProviderTaskId() 只在内存 → 需 TaskService 事务内重新加载保存（saveProviderTaskId）
2. **Mock 查询必须幂等**：第一次查 SUCCEEDED 就 remove 记录 → 后续 RENDERING/COMPILING 轮询"任务不存在" → 改为不删+超时清理
3. **缓存版本不兼容**：Docker 旧容器（旧序列化器）写入 Redis 的 styles 缓存，新代码（带类型信息）读炸 500 → 清 Redis 缓存（生产发布时清缓存或缓存 key 带版本号）

### 验证
- 契约测试 44/44（真实 PG+Redis，mock 引擎）
- 端到端：登录→建草稿→提交→7 智能体推进→SUCCESS→作品库落库 ✅

### 用户接真实模型步骤（别人用本项目）
1. 复制 backend-java/.env.example 为 backend-java/.env
2. 填 ARK_API_KEY（火山方舟控制台购买）
3. AI_VIDEO_PROVIDER=seedance
4. 运行 ./start-prod.ps1 启动
### 第 7 课补记（2026-09-21 真实接入排障）
1. **API 路径实战修正**：方舟视频生成正确路径是 `/contents/generations/tasks`（复数 contents+generations）；写成单数 `/content/generation/tasks` 返回 404
2. **PowerShell 传 JSON 三连坑**：内联引号被吞 → 用 `-d @file`；但 `Set-Content -Encoding UTF8` 写 BOM → 方舟"could not parse JSON" → 必须无 BOM 写入（`[System.IO.File]::WriteAllText` + UTF8Encoding($false)）
3. **ModelNotOpen 未开通模型**：账号需在方舟控制台"开通管理"申请 doubao-seedance-1-0-pro 后才能调用；已把方舟 error.message 透传成任务失败原因（前端可见）
4. **用户实操注意**：Key 必须放 backend-java/.env（gitignore），曾误填进 .env.example（公开模板）→ 已还原+提示轮换 Key
