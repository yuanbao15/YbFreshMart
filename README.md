# FreshMart 生鲜电商 - Spring Cloud 学习项目

基于 Spring Cloud 微服务架构的生鲜电商平台，用于学习分布式系统、服务治理、配置中心、搜索引擎、消息队列、容器化部署等技术。

> 📖 **详细架构设计文档**：见 `.claude/plans/luminous-purring-pinwheel.md`

## 技术栈

| 分类 | 技术 | 说明 |
|---|---|---|
| 框架 | Spring Boot 3.2 + Spring Cloud 2023 | 微服务基础 |
| 注册/配置 | Nacos 2.3 | 服务发现 + 配置中心 |
| 网关 | Spring Cloud Gateway | API 统一入口 |
| RPC | OpenFeign + LoadBalancer | 声明式服务调用 |
| 数据库 | MySQL 8.0 | 关系型数据 |
| ORM | MyBatis-Plus 3.5 | 增强 MyBatis |
| 缓存/锁 | Redis 7 + Redisson | 缓存/分布式锁 |
| 搜索引擎 | Elasticsearch 8 | 商品全文检索 |
| 消息队列 | RabbitMQ 3.12 | 异步解耦 |
| 文档数据库 | MongoDB 7 | 日志存储 |
| 限流降级 | Sentinel | 流量控制 |
| 部署 | Docker Compose + K8s | 容器化编排 |

## 项目结构

```
yb-cloud-parent/
├── yb-common/              # 公共：R<T>、异常、枚举、常量
├── yb-common-web/          # Web层：全局异常处理、Trace拦截器
├── yb-common-security/     # 安全：JWT、Feign拦截器   [阶段二]
├── yb-common-mybatis/      # MyBatis-Plus：分页、自动填充
├── yb-common-redis/        # Redis：分布式锁、缓存     [阶段二]
├── yb-common-mq/           # RabbitMQ公共配置          [阶段三]
├── yb-common-es/           # Elasticsearch公共配置     [阶段三]
├── yb-common-mongo/        # MongoDB公共配置           [阶段三]
├── yb-common-sentinel/     # Sentinel降级处理          [阶段四]
├── yb-api/                 # Feign接口集中管理
├── yb-gateway/             # API网关 (8080)
├── yb-auth/                # 认证授权 (8081)           [阶段二]
├── yb-user/                # 用户服务 (8082)          ✅
├── yb-product/             # 商品服务 (8083)           [阶段二]
├── yb-search/              # 搜索服务 (8084)           [阶段三]
├── yb-cart/                # 购物车服务 (8085)         [阶段二]
├── yb-order/               # 订单服务 (8086)           [阶段四]
├── yb-inventory/           # 库存服务 (8087)           [阶段四]
├── yb-payment/             # 支付服务 (8088)           [阶段五]
├── yb-notification/        # 通知服务 (8089)           [阶段五]
├── yb-log/                 # 日志服务 (8090)           [阶段三]
├── yb-job/                 # 定时任务 (8091)           [阶段六]
├── sql/                    # 数据库初始化脚本
├── docker-compose/         # 本地中间件编排
└── k8s/                    # K8s部署文件               [阶段六]
```



## 核心模块：

![image-20260611174513786](https://yuanbao-oss.oss-cn-shenzhen.aliyuncs.com/img/public_imgs/PicGo/202606111745224.png)



## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- Docker Desktop（用于启动中间件）

### 2. 启动中间件服务

```bash
cd docker-compose
docker-compose up -d
```

启动后访问：
- Nacos 控制台: http://localhost:8848/nacos （用户名/密码：nacos/nacos）
- RabbitMQ 管理: http://localhost:15672 （admin/admin123）
- Sentinel 控制台: http://localhost:8858 （sentinel/sentinel）
- Kibana: http://localhost:5601

### 3. 初始化数据库

SQL 脚本在 `sql/` 目录下，Docker Compose 启动 MySQL 时会自动执行。

### 4. 启动应用服务（按顺序）

```bash
# 1. 先启动 User 服务
cd yb-user
mvn spring-boot:run

# 2. 启动 Gateway 网关
cd yb-gateway
mvn spring-boot:run
```

### 5. 测试接口

```bash
# 通过 Gateway 访问用户服务
curl http://localhost:8080/api/user/page?page=1&size=10

# 新增用户
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456","nickname":"测试用户"}'
```

## 学习路径

| 阶段 | 内容 | 关键技术 |
|---|---|---|
| 阶段一 | 基础骨架 + Gateway + User CRUD | SpringBoot, MyBatis-Plus, Nacos, Gateway, OpenFeign |
| 阶段二 | 认证授权 + 商品 + 购物车 + Redis | JWT, Redis缓存, @Cacheable |
| 阶段三 | ES搜索 + MongoDB日志 + RabbitMQ | 全文检索, 文档存储, 消息发布/订阅 |
| 阶段四 | 下单核心链路 + Sentinel | 分布式锁, 死信队列, 限流熔断 |
| 阶段五 | 支付 + 通知闭环 | 多服务消息驱动 |
| 阶段六 | Dockerfile + K8s部署 | 容器化, 服务编排 |
| 阶段七 | 秒杀场景(可选) | 高并发, Lua脚本, 消息削峰 |

## 设计原则

- ✅ **同步查、异步改**：查询用 Feign，写操作通知用 MQ
- ✅ **最终一致性**：MQ + 重试 + 幂等，不引入分布式事务
- ✅ **禁止跨服务直连数据库**：所有数据交互通过 API/MQ
- ✅ **统一响应体**：所有 API 返回 `R<T>` 格式
- ✅ **全链路追踪**：TraceId 从 Gateway 传入，贯穿所有服务
