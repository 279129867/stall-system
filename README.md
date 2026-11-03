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

## 项目结构

```
stall-system/
├── src/main/java/com/example/stallsystem/
│   ├── config/              # 配置类
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
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   └── mapper/             # MyBatis XML
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
# 修改 src/main/resources/application.yml 里的数据库配置
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
