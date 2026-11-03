# 智能摆摊集市管理系统

一个面向摆摊集市的全栈管理系统，支持管理员、摊主、消费者三种角色，包含摊位管理、商品管理、订单管理、投诉处理、AI智能客服等功能。

## 技术栈

**后端**
- Spring Boot 3.2.5
- MyBatis-Plus
- MySQL 8.0
- Spring Security + JWT
- Spring AI（DeepSeek + ChromaDB）

**前端**
- Vue 3
- Vite
- Element Plus
- Axios

## 功能模块

### 管理员端
- 摊位管理（增删改查、摊位状态）
- 摊主审核与管理
- 消费者管理
- 投诉处理与处罚
- 数据统计看板

### 摊主端
- 商品管理（上架/下架）
- 订单处理
- 店铺信息设置
- 收入账单查看

### 消费者端
- 浏览摊位与商品
- 下单购买
- 评价打分
- 投诉建议
- AI智能客服咨询

### AI智能客服
- 智能问答（政策咨询、办证流程等）
- 商品推荐
- 投诉自动分类

## 快速启动

### 环境要求
- JDK 21
- MySQL 8.0
- Node.js 18+
- ChromaDB（可选，AI功能需要）

### 1. 启动数据库
```sql
CREATE DATABASE stall_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入 `stall_system.sql` 数据库脚本。

### 2. 启动后端
```bash
cd stall-system
# 修改 src/main/resources/application.yml 里的数据库密码
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 3. 启动前端
```bash
cd stall-frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

## 测试账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 摊主 | stallowner1 | admin123 |
| 消费者 | consumer1 | admin123 |

## 项目结构

```
stall-system/
├── src/main/java/com/example/stallsystem/
│   ├── config/          # 配置类（Security、CORS等）
│   ├── controller/      # 接口控制器
│   ├── service/         # 业务逻辑
│   ├── mapper/          # 数据访问层
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── ai/              # AI相关服务
│   ├── security/        # JWT认证
│   └── common/          # 通用工具类
├── src/main/resources/
│   ├── application.yml   # 配置文件
│   ├── mapper/          # MyBatis XML
│   └── static/          # 前端打包后的静态文件
└── pom.xml              # Maven依赖
```

## 接口文档

后端启动后访问：http://localhost:8080/swagger-ui.html（如果开启了Swagger）

## License

MIT
