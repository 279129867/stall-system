# 智能摆摊集市管理系统

一个面向摆摊集市的全栈管理系统，支持管理员、摊主、消费者三种角色，包含摊位管理、商品管理、订单管理、投诉处理、AI智能客服等功能。

## 技术栈

**后端**
- Spring Boot 3.2.5
- MyBatis-Plus
- MySQL 8.0
- Spring Security + JWT
- Spring AI（DeepSeek + ChromaDB）
- 并发：自定义线程池（池隔离）+ CompletableFuture 异步编排 + 超时降级 + traceId 链路追踪

**前端**
- Vue 3 + Vite
- Element Plus
- Axios

## 功能模块

### 管理员端
- 摊位管理（增删改查、状态管理）
- 摊主管理（审核、信息查看）
- 消费者管理
- 投诉处理
- 账单统计

### 摊主端
- 商品管理（上架/下架、分类）
- 订单处理
- 店铺信息设置
- 收入账单查看

### 消费者端
- 浏览摊位与商品
- 下单购买
- 评价打分
- 投诉建议
- AI智能客服

### AI模块
- AI智能聊天（对话问答）
- 投诉自动分类
- 知识库检索（ChromaDB向量库）
- 商品智能推荐

## 异步与并发设计

项目在关键路径上引入了**并行扇出 + 超时降级 + 线程池隔离**，并用 traceId 把跨线程的日志串成一条链路。

**两个隔离的线程池**（`config/AsyncConfig`）

| 池 | 参数 | 用途 |
|---|---|---|
| `ai-*` | core 4 / max 8 / queue 200 | AI 调用（一次 3~8s 的慢任务，不能和业务共用） |
| `biz-*` | core 8 / max 16 / queue 1000 | 看板聚合等短任务 |

有界队列 + `CallerRunsPolicy`（池满反压，不丢任务）+ 优雅停机等待在途任务；分池的目的是防止慢调用把业务线程吃光（故障扩散）。

**场景一：AI 问答并行扇出**（`ai/AiChatService#ask`）

意图识别（LLM）和知识检索（向量库）互不依赖，改成并行发起，各自带 1.2s / 1.5s 超时；生成阶段整条链路 8s 预算。任何一路超时都降级（意图默认 chat、知识为空、回答给兜底话术），接口不返回 500。LLM 调用额外用 `Semaphore` 限制并发 8，避免超时后请求在池里堆积。

**场景二：看板并行聚合**（`controller/AdminController#stats`）

4 个独立查询并行执行，`allOf` + 500ms 统一超时，失败降级为上一次成功的快照；顺带把"把全部订单捞进内存 reduce"改成 SQL `SUM`。

**可观测**

- `common/TraceIdFilter` 每个请求生成 traceId，响应头 `X-Trace-Id` 回写
- `common/MdcTaskDecorator` 把 MDC 传递到线程池，日志里 `http-nio-*`、`ai-*`、`biz-*` 共享同一个 traceId
- `GET /api/admin/threadpool` 返回两个池的 active / queue / completed

实测：看板 4 路并行 46ms 完成；AI 扇出 146ms 拿到 3 条知识（串行约 800ms+）；LLM 不可用时 300ms 内降级返回，接口仍 200。

## 项目结构

```
stall-system/
├── src/main/java/com/example/stallsystem/
│   ├── config/              # 配置类
│   │   ├── SecurityConfig          # 安全配置
│   │   ├── WebConfig               # 前端路由转发
│   │   └── AsyncConfig             # 异步线程池（ai / biz 双池隔离）
│   ├── controller/          # 16个控制器
│   │   ├── AdminController          # 管理员接口
│   │   ├── AuthController           # 登录认证
│   │   ├── StallController          # 摊位管理
│   │   ├── StallOwnerController     # 摊主管理
│   │   ├── ConsumerController       # 消费者管理
│   │   ├── ProductController        # 商品管理
│   │   ├── ProductCategoryController # 商品分类
│   │   ├── OrdersController         # 订单管理
│   │   ├── OrderItemController      # 订单项
│   │   ├── ComplaintController      # 投诉管理
│   │   ├── ReviewController         # 评价管理
│   │   ├── BillController           # 账单管理
│   │   ├── DeliveryController       # 配送管理
│   │   ├── AiChatController         # AI聊天接口
│   │   ├── ProductRecommendController # 商品推荐
│   │   └── HealthController         # 健康检查
│   ├── service/            # 业务逻辑（11个服务）
│   ├── mapper/             # 数据访问层
│   ├── entity/             # 11个实体类
│   │   ├── Admin              # 管理员
│   │   ├── Stall              # 摊位
│   │   ├── StallOwner          # 摊主
│   │   ├── Consumer           # 消费者
│   │   ├── Product            # 商品
│   │   ├── ProductCategory    # 商品分类
│   │   ├── Orders             # 订单
│   │   ├── OrderItem          # 订单项
│   │   ├── Complaint          # 投诉
│   │   ├── Review             # 评价
│   │   └── Bill               # 账单
│   ├── ai/                 # AI相关服务
│   │   ├── AiChatService          # AI聊天服务
│   │   ├── ComplaintClassifyService # 投诉自动分类
│   │   ├── KnowledgeLoader        # 知识库加载
│   │   └── GreenVectorStoreConfig  # 向量存储配置
│   ├── security/           # JWT认证
│   └── common/             # 通用工具类
│       ├── Result                  # 统一响应
│       ├── GlobalExceptionHandler  # 全局异常
│       ├── TraceIdFilter           # traceId 生成/透传
│       └── MdcTaskDecorator        # traceId 跨线程传递
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   ├── mapper/             # MyBatis XML
│   └── static/             # 已构建的前端（由后端直接提供）
├── docs/
│   └── 异步改造方案.md      # 异步/多线程改造设计与验收
└── pom.xml                 # Maven依赖
```

## 快速启动

### 环境要求
- JDK 21
- MySQL 8.0
- Node.js 18+

### 1. 启动数据库
```sql
CREATE DATABASE stall_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端
```bash
cd stall-system
# 修改 src/main/resources/application.yml 里的数据库配置和 AI Key
mvn spring-boot:run
```

> AI 模块依赖 Chroma（`localhost:8000`），启动时会去连它，没起会导致启动失败。
> 不想装 Chroma 时，用内置本地知识库启动，加两个启动参数：
> `--spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration --app.ai.local-vector-store=true`

后端运行在 http://localhost:8080

### 3. 前端
前端已经构建好放在 `src/main/resources/static`，由后端直接提供，启动后端后访问 http://localhost:8080 即可，不需要单独起前端服务。

如需修改前端源码，请另用 `stall-frontend` 工程 `npm run dev`（默认 5173，接口指向 8080）。

## 测试账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 摊主 | stallowner1 | admin123 |
| 消费者 | consumer1 | admin123 |

## 数据库表

共11张表：
- admin（管理员）
- stall（摊位）
- stall_owner（摊主）
- consumer（消费者）
- product（商品）
- product_category（商品分类）
- orders（订单）
- order_item（订单项）
- complaint（投诉）
- review（评价）
- bill（账单）

## License

MIT
