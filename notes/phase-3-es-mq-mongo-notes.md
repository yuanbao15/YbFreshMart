# 阶段三：ES 搜索 + MongoDB 日志 + RabbitMQ 消息驱动

> 日期：2026-06-16  
> 范围：yb-common-mq + yb-common-es + yb-common-mongo + yb-search + yb-log  
> 状态：✅ 编译通过

---

## 一、整体架构

```
                            ┌──────────────┐
                            │    Nacos     │  注册中心 + 配置中心
                            │   :8848      │
                            └──────┬───────┘
                               ▲   │  查到 "yb-search" → :8084
                               │   │  查到 "yb-log"    → :8090
                               │   ▼
┌──────────┐           ┌──────────────────────────────────────────────────┐
│  浏览器   │ ───────▶ │              yb-gateway :8080                    │
│  curl    │  :8080   │  AuthGlobalFilter ─ 全局 JWT 鉴权 + 黑名单检查     │
│  IDEA    │          │  白名单: /api/auth/**, /api/search, /api/product/* │
└──────────┘          └──┬──────────┬──────────┬──────────┬──────────────┘
                         │          │          │          │
            ┌────────────┘          │          │          └────────────┐
            ▼                       ▼          ▼                       ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  yb-search :8084 │  │   yb-log :8090   │  │  yb-product :8083│  │  (其他服务...)    │
│   搜索服务        │  │   日志服务        │  │   商品服务        │  │                  │
│                  │  │                  │  │                  │  │                  │
│ • 全文检索       │  │ • 行为日志收集    │  │ • 商品 CRUD      │  │                  │
│ • 类目筛选       │  │ • 审计日志收集    │  │ • 发送 MQ 消息    │  │                  │
│ • 搜索高亮       │  │ • 日志查询 API   │  │                  │  │                  │
│   ┌───────────┐  │  │   ┌───────────┐  │  │   ┌───────────┐  │  │                  │
│   │ ES :9200  │  │  │   │ MongoDB   │  │  │   │ MySQL     │  │  │                  │
│   │ product   │  │  │   │ :27017    │  │  │   │ yb_product│  │  │                  │
│   │   索引    │  │  │   │ behavior  │  │  │   └───────────┘  │  │                  │
│   └───────────┘  │  │   │ audit_log │  │  │                  │  │                  │
│                  │  │   └───────────┘  │  │                  │  │                  │
│   ▲ MQ 消费      │  │   ▲ MQ 消费      │  │   │ MQ 发送       │  │                  │
│   │              │  │   │              │  │   ▼              │  │                  │
└──┼──────────────┘  └──┼──────────────┘  └──┼──────────────┘  └──────────────────┘
   │                     │                    │
   │          ┌──────────┴──────────┐         │
   │          │      RabbitMQ       │ ◀───────┘
   │          │      :5672          │
   │          │  :15672 (管理控制台)  │
   │          │                     │
   │          │  order.event.exchange│  ← Phase 4/5 预留
   │          │  product.sync.exchange│ ← 商品同步 Fanout
   │          │  log.exchange        │  ← 日志 Topic
   │          └──────────────────────┘
   │
   └────────────── 商品变更消息 → ES 索引同步
                  行为/审计日志 → MongoDB 存储
```

### 模块分层（阶段一~三完整视图）

```
yb-cloud-parent/
├── yb-common/            ← [阶段一] 公共 POJO、枚举、异常、MQ常量
├── yb-common-web/        ← [阶段一] 全局异常处理、TraceId
├── yb-common-mybatis/    ← [阶段一] MyBatis-Plus 分页、BaseEntity
├── yb-api/               ← [阶段一] Feign 接口 & DTO + SearchClient/LogClient
├── yb-gateway/           ← [阶段一] API 网关 :8080
├── yb-user/              ← [阶段一] 用户服务 :8082
│
├── yb-common-security/   ← [阶段二] JWT 工具 + 用户上下文 + Feign 鉴权
├── yb-common-redis/      ← [阶段二] Redis 序列化 + Spring Cache + 分布式锁
├── yb-auth/              ← [阶段二] 认证服务 :8081
├── yb-product/           ← [阶段二] 商品服务 :8083
├── yb-cart/              ← [阶段二] 购物车服务 :8085
│
├── yb-common-mq/         ← [阶段三] RabbitMQ 配置 + 交换机/队列声明 + 消息发送工具
├── yb-common-es/         ← [阶段三] Elasticsearch 配置 + 索引管理工具
├── yb-common-mongo/      ← [阶段三] MongoDB 配置 + MongoTemplate
├── yb-search/            ← [阶段三] 搜索服务 :8084
└── yb-log/               ← [阶段三] 日志服务 :8090
```

---

## 二、yb-common-mq：RabbitMQ 公共模块

### 2.1 模块定位

封装 RabbitMQ 的连接配置、JSON 序列化、交换机/队列/绑定自动声明，以及消息发送工具。其他服务引入后即可使用 `RabbitTemplate` 收发消息。

### 2.2 文件清单

| 文件 | 职责 |
|------|------|
| [`pom.xml`](../yb-common-mq/pom.xml) | 依赖 spring-boot-starter-amqp、Jackson |
| [`RabbitMQConfig.java`](../yb-common-mq/src/main/java/com/yb/common/mq/config/RabbitMQConfig.java) | `@AutoConfiguration`，注册 Jackson2JsonMessageConverter + RabbitTemplate（JSON 序列化） |
| [`ExchangeQueueDeclarer.java`](../yb-common-mq/src/main/java/com/yb/common/mq/config/ExchangeQueueDeclarer.java) | `@AutoConfiguration`，声明所有 Exchange、Queue、Binding Bean（应用启动时自动创建） |
| [`MessageSender.java`](../yb-common-mq/src/main/java/com/yb/common/mq/util/MessageSender.java) | `@Component`，封装发送方法：`send()`、`sendWithDelay()`、`sendToQueue()` |
| `AutoConfiguration.imports` | 注册上述两个 AutoConfiguration 类 |

### 2.3 交换机 / 队列 / 绑定关系

**交换机**（已在 `MqExchange` 常量中定义）：

| 交换机常量 | 名称 | 类型 | 用途 |
|-----------|------|------|------|
| `ORDER_EVENT` | `order.event.exchange` | Topic | 订单事件（Phase 4/5 预留） |
| `PRODUCT_SYNC` | `product.sync.exchange` | Fanout | 商品同步广播 |
| `LOG` | `log.exchange` | Topic | 日志收集 |

**队列 & 绑定**：

| 队列 | 交换机 | Routing Key | 说明 |
|------|--------|-------------|------|
| `queue.order.create` | `order.event.exchange` | `order.create` | 订单创建（预留） |
| `queue.order.pay.timeout` | — | — | 支付超时死信队列，TTL=30min（预留） |
| `queue.order.paid` | `order.event.exchange` | `order.paid` | 已支付（预留） |
| `queue.order.cancelled` | `order.event.exchange` | `order.cancelled` | 已取消（预留） |
| `queue.product.sync.search` | `product.sync.exchange` | —（Fanout） | **阶段三使用**：商品同步到 ES |
| `queue.log.behavior` | `log.exchange` | `log.behavior.#` | **阶段三使用**：行为日志 |
| `queue.log.audit` | `log.exchange` | `log.audit.#` | **阶段三使用**：审计日志 |

**⚠️ 注意**：`queue.order.pay.timeout` 没有绑定到交换机——它在 Phase 4/5 将通过死信交换机（DLX）机制使用，当前仅预创建队列。

### 2.4 消息序列化方案

```
Jackson2JsonMessageConverter
  ├─ ObjectMapper 注册 JavaTimeModule（支持 LocalDateTime）
  ├─ 消息体自动序列化为 JSON
  └─ 替代默认的 SimpleMessageConverter（Java 序列化）
```

消费端通过 `@RabbitListener` 直接接收 POJO，Spring 自动反序列化。

### 2.5 MessageSender 使用示例

```java
// 发送到指定交换机 + Routing Key
messageSender.send(MqExchange.PRODUCT_SYNC, "", syncMessage);

// 发送到指定队列
messageSender.sendToQueue(MqQueue.LOG_BEHAVIOR, behaviorLog);

// 发送延迟消息（需 rabbitmq_delayed_message_exchange 插件）
messageSender.sendWithDelay(MqExchange.ORDER_EVENT, MqRoutingKey.ORDER_CREATE, msg, 5000);
```

---

## 三、yb-common-es：Elasticsearch 公共模块

### 3.1 模块定位

统一管理 Elasticsearch 依赖版本。Spring Boot 3.2 + Spring Data Elasticsearch 5.2 已自动配置 `ElasticsearchClient`（新的 Java 客户端，替代废弃的 RestHighLevelClient）、`ElasticsearchOperations` 和 `ElasticsearchRepository`，本模块主要提供索引管理工具。

### 3.2 文件清单

| 文件 | 职责 |
|------|------|
| [`pom.xml`](../yb-common-es/pom.xml) | 依赖 spring-boot-starter-data-elasticsearch |
| [`ElasticsearchConfig.java`](../yb-common-es/src/main/java/com/yb/common/es/config/ElasticsearchConfig.java) | `@AutoConfiguration`，确保模块被 Spring 扫描 |
| [`EsIndexUtil.java`](../yb-common-es/src/main/java/com/yb/common/es/util/EsIndexUtil.java) | `@Component`，索引管理工具：`createIndex()`、`deleteIndex()`、`existsIndex()` |
| `AutoConfiguration.imports` | 注册 ElasticsearchConfig |

### 3.3 ES 连接配置

各服务在 `application.yml` 中配置：

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 3s
    socket-timeout: 10s
```

Docker Compose 中 xpack.security 已关闭，无需认证。

### 3.4 EsIndexUtil 使用示例

```java
// 创建索引（自动应用 @Document、@Setting、@Field 注解）
esIndexUtil.createIndex(ProductDocument.class);

// 检查索引是否存在
boolean exists = esIndexUtil.existsIndex(ProductDocument.class);

// 删除索引
esIndexUtil.deleteIndex(ProductDocument.class);
```

---

## 四、yb-common-mongo：MongoDB 公共模块

### 4.1 模块定位

统一管理 MongoDB 依赖版本，自定义 MongoTemplate 以保持文档干净（移除 `_class` 字段）。

### 4.2 文件清单

| 文件 | 职责 |
|------|------|
| [`pom.xml`](../yb-common-mongo/pom.xml) | 依赖 spring-boot-starter-data-mongodb |
| [`MongoConfig.java`](../yb-common-mongo/src/main/java/com/yb/common/mongo/config/MongoConfig.java) | `@AutoConfiguration`，自定义 MongoTemplate（`setTypeMapper(null)` 移除 `_class` 字段） |
| `AutoConfiguration.imports` | 注册 MongoConfig |

### 4.3 为什么移除 _class 字段？

Spring Data MongoDB 默认在文档中写入 `_class: "com.yb.log.document.BehaviorLog"` 用于反序列化时确定 Java 类型。本项目使用明确的 Java 类操作，不需要类型提示，移除后：
- 文档更干净，非 Java 工具也能直接读写
- 减少存储空间
- 避免类型变更时的兼容问题

### 4.4 MongoDB 连接配置

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      username: admin
      password: admin123
      authentication-database: admin
      database: yb_log
```

---

## 五、yb-search：搜索服务 (:8084)

### 5.1 模块定位

提供商品全文检索，通过 MQ 消费商品变更消息同步到 Elasticsearch。支持中文分词（ik）、高亮、类目筛选、分页。

### 5.2 文件清单

```
yb-search/
├── pom.xml
├── src/main/resources/application.yml
└── src/main/java/com/yb/search/
    ├── SearchApplication.java              ← 启动类
    ├── document/
    │   └── ProductDocument.java            ← ES 索引文档（product 索引）
    ├── repository/
    │   └── ProductSearchRepository.java    ← ElasticsearchRepository（基础 CRUD）
    ├── dto/
    │   └── ProductSyncMessage.java         ← 商品同步消息体（productId + SyncType 枚举）
    ├── service/
    │   ├── SearchService.java              ← 接口
    │   └── impl/SearchServiceImpl.java     ← 核心实现（高亮搜索 + ES 同步）
    ├── controller/
    │   └── SearchController.java           ← REST API
    └── mq/
        └── ProductSyncConsumer.java        ← MQ 消费者：监听 PRODUCT_SYNC_SEARCH 队列
```

### 5.3 ES 索引结构（ProductDocument）

```json
{
  "id": 10001,
  "spuId": 1001,
  "name": "有机菠菜 500g",
  "image": "http://...",
  "price": 9.90,
  "stock": 100,
  "categoryId": 11,
  "categoryName": "叶菜类"
}
```

**分词配置**：`name` 字段使用 `ik_max_word`（索引时细粒度分词）+ `ik_smart`（搜索时粗粒度分词），同时保存 `name.keyword` 子字段用于精确匹配。

**⚠️ 前提条件**：ES 需要安装 `ik` 分词插件：
```bash
docker exec -it yb-es elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
docker restart yb-es
```

### 5.4 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/search/product?keyword=苹果&categoryId=1&page=1&size=20` | 搜索商品（关键词 + 类目筛选 + 分页） |

**请求参数**：

| 参数 | 必填 | 说明 |
|------|------|------|
| `keyword` | 否 | 搜索关键词（ik 分词匹配 name 字段） |
| `categoryId` | 否 | 类目 ID 筛选（ES term 精确过滤） |
| `page` | 否 | 页码（默认 1） |
| `size` | 否 | 每页条数（默认 20） |

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "total": 42,
    "page": 1,
    "size": 20,
    "pages": 3,
    "records": [
      {
        "id": 10001,
        "spuId": 1001,
        "name": "有机<em>菠菜</em> 500g",
        "image": "http://...",
        "price": 9.90,
        "stock": 100,
        "categoryId": 11,
        "categoryName": "叶菜类"
      }
    ]
  }
}
```

### 5.5 搜索流程

```
GET /api/search/product?keyword=菠菜&categoryId=11&page=1&size=20
      │
      ▼
SearchController.search(keyword, categoryId, page, size)
      │
      ▼
SearchServiceImpl.search()
      │
      ├─ ① 构建 BoolQuery
      │     ├─ must:  match("name", keyword)     ← ik 分词全文检索
      │     └─ filter: term("categoryId", 11)    ← 类目精确筛选
      │
      ├─ ② 配置高亮：name 字段 → <em>标签包裹匹配词
      │
      ├─ ③ ElasticsearchOperations.search() 执行查询
      │
      ├─ ④ 后处理：将高亮片段替换到 name 字段
      │
      └─ ⑤ 返回 PageDTO<ProductDocument>
```

### 5.6 MQ 商品同步流程

```
yb-product 商品变更（创建/更新/删除）
      │
      ├─ 发送消息到 product.sync.exchange（Fanout）
      │     Body: {"productId": 10001, "spuId": 1001, "type": "UPDATE"}
      │
      ▼
queue.product.sync.search
      │
      ▼
ProductSyncConsumer.handleProductSync(msg)
      │
      ├─ type = DELETE → searchService.deleteProduct(productId) → ES delete
      │
      ├─ type = CREATE / UPDATE
      │     ├─ ① ProductClient.getSkuById(productId) → Feign 查商品最新详情
      │     ├─ ② toDocument(resp) → 转为 ProductDocument
      │     └─ ③ searchService.syncProduct(doc) → ES save
      │
      └─ 异常 → log.error（Phase 4/5 接入死信队列重试）
```

**ProductSyncMessage 结构**：
```json
{
  "productId": 10001,
  "spuId": 1001,
  "type": "CREATE"   // CREATE | UPDATE | DELETE
}
```

---

## 六、yb-log：日志服务 (:8090)

### 6.1 模块定位

通过 MQ 接收行为和审计日志消息，存入 MongoDB，提供日志查询 API。服务自身不产生日志消息——其他业务服务通过 `MessageSender` 将日志发送到 MQ，本服务作为消费者异步落库。

### 6.2 文件清单

```
yb-log/
├── pom.xml
├── src/main/resources/application.yml
└── src/main/java/com/yb/log/
    ├── LogApplication.java                 ← 启动类
    ├── document/
    │   ├── BehaviorLog.java                ← 行为日志文档（behavior_log 集合）
    │   └── AuditLog.java                   ← 审计日志文档（audit_log 集合）
    ├── service/
    │   ├── LogService.java                 ← 接口
    │   └── impl/LogServiceImpl.java        ← MongoTemplate 实现
    ├── controller/
    │   └── LogController.java              ← REST API
    └── mq/
        └── LogConsumer.java                ← MQ 消费者：监听 behavior + audit 队列
```

### 6.3 MongoDB 文档结构

**行为日志（behavior_log）**：

```json
{
  "_id": ObjectId("..."),
  "userId": 1001,
  "action": "VIEW",
  "target": "10001",
  "targetDesc": "有机菠菜 500g",
  "ip": "192.168.1.1",
  "userAgent": "Mozilla/5.0 ...",
  "timestamp": "2026-06-16T10:30:00"
}
```

支持的 action 类型：`VIEW`（浏览）、`CLICK`（点击）、`ADD_CART`（加购）、`PURCHASE`（购买）、`SEARCH`（搜索）。

**审计日志（audit_log）**：

```json
{
  "_id": ObjectId("..."),
  "userId": 1001,
  "operation": "LOGIN",
  "detail": "手机号 138****1111 登录成功",
  "result": "SUCCESS",
  "ip": "192.168.1.1",
  "timestamp": "2026-06-16T10:30:00"
}
```

支持的 operation 类型：`LOGIN`、`REGISTER`、`UPDATE_PROFILE`、`DELETE`、`GRANT` 等。result 为 `SUCCESS` 或 `FAILURE`。

**索引**：`userId` 和 `timestamp` 加了 `@Indexed` 注解，MongoDB 自动创建 B-Tree 索引以提升查询性能。

### 6.4 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/log/behavior?userId=1001&page=1&size=20` | 查询行为日志 |
| `GET` | `/api/log/audit?userId=1001&page=1&size=20` | 查询审计日志 |

**请求参数**：

| 参数 | 必填 | 说明 |
|------|------|------|
| `userId` | 否 | 按用户 ID 筛选（不传则查全部） |
| `page` | 否 | 页码（默认 1） |
| `size` | 否 | 每页条数（默认 20） |

查询结果按 `timestamp` 降序排列（最新日志在前）。

### 6.5 日志消费流程

```
其他业务服务                       yb-log
      │                               │
      ├─ messageSender.send(            │
      │   MqExchange.LOG,               │
      │   MqRoutingKey.LOG_BEHAVIOR,    │
      │   behaviorLog)                  │
      │                                ▼
      │               LogConsumer.handleBehaviorLog(log)
      │                    │
      │                    └─ logService.saveBehaviorLog(log)
      │                         └─ mongoTemplate.save(log)  → behavior_log 集合
      │
      ├─ messageSender.send(
      │   MqExchange.LOG,
      │   MqRoutingKey.LOG_AUDIT,
      │   auditLog)
      │                                ▼
      │               LogConsumer.handleAuditLog(log)
      │                    │
      │                    └─ logService.saveAuditLog(log)
      │                         └─ mongoTemplate.save(log)  → audit_log 集合
```

---

## 七、yb-api 补充：SearchClient + LogClient

### 7.1 新增 Feign 接口

| Feign 接口 | 目标服务 | 方法 |
|-----------|---------|------|
| [`SearchClient.java`](../yb-api/src/main/java/com/yb/api/client/SearchClient.java) | yb-search | `search(keyword, categoryId, page, size)` |
| [`LogClient.java`](../yb-api/src/main/java/com/yb/api/client/LogClient.java) | yb-log | `queryBehaviorLogs(userId, page, size)` + `queryAuditLogs(userId, page, size)` |

### 7.2 Fallback 降级

每个 Feign Client 都有对应的 `FallbackFactory`，在服务不可用时返回 `R.fail(500, "xxx服务暂时不可用")`，防止雪崩效应。

| Fallback 类 | 说明 |
|------------|------|
| [`SearchClientFallbackFactory.java`](../yb-api/src/main/java/com/yb/api/fallback/SearchClientFallbackFactory.java) | yb-search 不可用时降级 |
| [`LogClientFallbackFactory.java`](../yb-api/src/main/java/com/yb/api/fallback/LogClientFallbackFactory.java) | yb-log 不可用时降级 |

---

## 八、Gateway 更新：Token 黑名单

### 8.1 背景

阶段二中 `AuthGlobalFilter` 已留下 TODO：

> Token 在黑名单（Redis key=token:blacklist:\<token\>）？TODO: 阶段三实现

### 8.2 实现方案

**依赖选择**：Gateway 是 WebFlux 环境，不引入 `yb-common-redis`（该模块依赖 `spring-boot-starter-data-redis` 会引入 Tomcat/Servlet）。改为直接依赖 Redisson（底层基于 Netty，与 WebFlux 兼容）。

**新增文件**：

| 文件 | 职责 |
|------|------|
| [`RedissonConfig.java`](../yb-gateway/src/main/java/com/yb/gateway/config/RedissonConfig.java) | `@Configuration`，创建 `RedissonClient` Bean（从 application.yml 读取 Redis 连接信息） |

**修改文件**：

| 文件 | 变更 |
|------|------|
| `yb-gateway/pom.xml` | 新增 `redisson` 依赖 |
| `AuthGlobalFilter.java` | 注入 `RedissonClient`；JWT 解析成功后检查 `token:blacklist:<token>` 是否存在 |
| `application.yml` | 新增 `spring.data.redis` 配置块（host/password/port） |

### 8.3 鉴权流程（更新后）

```
请求进入 Gateway
  │
  ├─ ① 路径在白名单？ → 直接放行
  │
  ├─ ② 请求头有 Authorization: Bearer <token>？ → 没有 → 401
  │
  ├─ ③ 解析 JWT → 失败 → 401
  │
  ├─ ④ Token 在黑名单（redissonClient.getBucket("token:blacklist:<token>").isExists()）？
  │     是 → 401
  │
  └─ ⑤ 放行，请求头注入 X-User-Id、X-User-Role
```

### 8.4 黑名单写入方

黑名单的写入方是 **yb-auth** 的 `AuthServiceImpl`，在以下场景写入：
- 用户主动登出（`POST /api/auth/logout`）
- Token 刷新时旧 Token 作废（`POST /api/auth/refresh`）

写入时 TTL 设置为 Token 的剩余过期时间，过期后 Redis 自动清理，无需额外维护。

---

## 九、前端页面

### 9.1 新增页面

| 页面 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 商品搜索 | `/search` | [`Search.vue`](../yb-front/src/views/Search.vue) | 搜索框 + 类目筛选 + 商品网格 + 搜索结果高亮 + 分页 |
| 日志查询 | `/admin/logs` | [`admin/LogView.vue`](../yb-front/src/views/admin/LogView.vue) | 行为日志/审计日志 Tab 切换 + 用户ID筛选 + 表格展示 + 分页 |

### 9.2 新增 API 调用

| 文件 | 封装的接口 |
|------|-----------|
| [`api/search.js`](../yb-front/src/api/search.js) | `GET /api/search/product` |
| [`api/log.js`](../yb-front/src/api/log.js) | `GET /api/log/behavior` + `GET /api/log/audit` |

### 9.3 导航栏变更

在 [`Navbar.vue`](../yb-front/src/components/Navbar.vue) 中添加了公开的 **🔍 搜索** 链接（始终可见，无需登录）。

---

## 十、launch.json 更新

新增两个后端调试配置：

| 配置名 | 主类 | 端口 |
|--------|------|------|
| `🔍 yb-search · 搜索服务 :8084` | `com.yb.search.SearchApplication` | 8084 |
| `📋 yb-log · 日志服务 :8090` | `com.yb.log.LogApplication` | 8090 |

复合启动配置：

| 配置名 | 包含服务 |
|--------|---------|
| `🚀 全部后端（7 个服务）` | gateway + auth + user + product + cart + search + log |
| `📡 阶段三核心` | gateway + auth + user + product + search + log |

---

## 十一、开发调试

### 11.1 启动顺序

```bash
# 1. 启动阶段三中间件（ES + RabbitMQ + MongoDB）
cd docker-compose
docker compose up -d elasticsearch rabbitmq mongodb

# 如果阶段一/二的 MySQL、Redis、Nacos 还没起，直接全部启动：
docker compose up -d

# 2. 安装 ES 中文分词插件（仅首次）
docker exec -it yb-es elasticsearch-plugin install \
  https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
docker restart yb-es

# 3. VS Code → Debug 面板 → 选择 "📡 阶段三核心" → F5
#    或依次:
#    cd yb-gateway  && mvn spring-boot:run     # :8080
#    cd yb-auth     && mvn spring-boot:run     # :8081
#    cd yb-user     && mvn spring-boot:run     # :8082
#    cd yb-product  && mvn spring-boot:run     # :8083
#    cd yb-search   && mvn spring-boot:run     # :8084
#    cd yb-log      && mvn spring-boot:run     # :8090

# 4. 前端
cd yb-front && npm run dev                     # :5173
```

### 11.2 测试命令

```bash
# ========== 搜索 ==========
# 全文检索（通过 Gateway）
curl "http://localhost:8080/api/search/product?keyword=菠菜&page=1&size=10"

# 类目筛选
curl "http://localhost:8080/api/search/product?categoryId=11&page=1&size=10"

# 组合搜索
curl "http://localhost:8080/api/search/product?keyword=有机&categoryId=11"

# 直接连搜索服务
curl "http://localhost:8084/api/search/product?keyword=菠菜"

# ========== 日志查询 ==========
# 查询行为日志（通过 Gateway）
curl "http://localhost:8080/api/log/behavior?userId=1001&page=1&size=20"

# 查询审计日志
curl "http://localhost:8080/api/log/audit?page=1&size=20"

# 直接连日志服务
curl "http://localhost:8090/api/log/behavior"

# ========== 商品变更 → 触发 MQ 同步到 ES ==========
# 先登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}' | jq -r '.data.token')

# 创建商品（会触发 MQ → ES 同步）
curl -X POST http://localhost:8080/api/product/spu \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"categoryId":11,"name":"测试有机菠菜","brand":"测试农场"}'

# 稍等片刻，搜索验证
curl "http://localhost:8080/api/search/product?keyword=测试"
```

### 11.3 中间件调试命令

```bash
# ========== RabbitMQ ==========
# 管理控制台: http://localhost:15672 （admin/admin123）
# 查看队列
docker exec yb-rabbitmq rabbitmqctl list_queues

# ========== Elasticsearch ==========
# 查看索引
curl http://localhost:9200/_cat/indices?v

# 查看 product 索引的 mapping
curl http://localhost:9200/product/_mapping?pretty

# 搜索测试
curl "http://localhost:9200/product/_search?q=菠菜&pretty"

# Kibana 可视化: http://localhost:5601
#   → Dev Tools → GET /product/_search {"query":{"match":{"name":"菠菜"}}}

# ========== MongoDB ==========
docker exec -it yb-mongo mongosh -u admin -p admin123 --authenticationDatabase admin

# 在 mongosh 中：
use yb_log
db.behavior_log.find().sort({timestamp: -1}).limit(10)
db.audit_log.find({result: "FAILURE"})
db.behavior_log.countDocuments({userId: 1001})
db.behavior_log.getIndexes()
```

---

## 十二、关键设计决策

| # | 决策 | 理由 |
|---|------|------|
| 1 | 商品同步用 Fanout 交换机 | 未来可能有多个消费者（搜索、推荐、缓存预热等），Fanout 自动广播 |
| 2 | 日志收集用 Topic 交换机 | 行为日志和审计日志用不同 Routing Key 区分，一条消息只进一个队列 |
| 3 | Gateway 不引入 yb-common-redis，直接用 Redisson | Gateway 是 WebFlux/Netty 环境；引入 spring-boot-starter-data-redis 会拖入 Tomcat/Servlet |
| 4 | ES 索引名 `product`，shards=1 replicas=0 | 学习阶段单节点 ES，1 个分片 0 副本即可，生产环境需调整 |
| 5 | MongoDB 文档不存 `_class` 字段 | 保持文档干净，非 Java 工具也可读写；`setTypeMapper(null)` |
| 6 | MQ 消费者 `acknowledge-mode: auto` + 重试 3 次 | 简单可靠，消费失败自动 NACK 回队列重试 |
| 7 | 搜索服务通过 Feign 获取最新商品详情再写入 ES | 确保 ES 中的数据是最新的；MQ 消息体只传 productId + type，不传完整数据 |
| 8 | 日志服务不查询 MySQL，纯 MongoDB | MongoDB 天然适合时序写入 + 按时间排序查询，无需联表 |
| 9 | 前端搜索高亮由后端 ES 返回 `<em>` 标签 | 减少前端复杂度，后端直接返回带高亮标签的 name 字段 |
| 10 | 搜索页公开、日志页需登录 | 搜索是核心用户功能无需登录；日志包含用户隐私需要鉴权 |

## 十三、关键文件速查表

| 你想了解的事 | 看哪个文件 |
|-------------|----------|
| RabbitMQ 交换机/队列声明 | `yb-common-mq/.../config/ExchangeQueueDeclarer.java` |
| RabbitMQ JSON 序列化 | `yb-common-mq/.../config/RabbitMQConfig.java` |
| 消息发送工具 | `yb-common-mq/.../util/MessageSender.java` |
| ES 索引管理工具 | `yb-common-es/.../util/EsIndexUtil.java` |
| MongoDB 配置 | `yb-common-mongo/.../config/MongoConfig.java` |
| ES 商品文档结构 | `yb-search/.../document/ProductDocument.java` |
| 搜索逻辑（高亮+过滤） | `yb-search/.../service/impl/SearchServiceImpl.java` |
| 商品同步 MQ 消费者 | `yb-search/.../mq/ProductSyncConsumer.java` |
| 商品同步消息 DTO | `yb-search/.../dto/ProductSyncMessage.java` |
| 搜索 Controller | `yb-search/.../controller/SearchController.java` |
| MongoDB 日志文档 | `yb-log/.../document/BehaviorLog.java` + `AuditLog.java` |
| 日志 MQ 消费者 | `yb-log/.../mq/LogConsumer.java` |
| 日志查询逻辑 | `yb-log/.../service/impl/LogServiceImpl.java` |
| 日志 Controller | `yb-log/.../controller/LogController.java` |
| SearchClient (Feign) | `yb-api/.../client/SearchClient.java` |
| LogClient (Feign) | `yb-api/.../client/LogClient.java` |
| Gateway Token 黑名单 | `yb-gateway/.../filter/AuthGlobalFilter.java` |
| Gateway Redisson 配置 | `yb-gateway/.../config/RedissonConfig.java` |
| MQ 常量（交换机/队列/RoutingKey） | `yb-common/.../constant/MqExchange.java` + `MqQueue.java` + `MqRoutingKey.java` |
| Redis Key 常量（含黑名单前缀） | `yb-common/.../constant/RedisKey.java` |
| 前端搜索页 | `yb-front/src/views/Search.vue` |
| 前端日志页 | `yb-front/src/views/admin/LogView.vue` |
| 前端搜索 API | `yb-front/src/api/search.js` |
| 前端日志 API | `yb-front/src/api/log.js` |
| 前端路由（含 /search、/admin/logs） | `yb-front/src/router/index.js` |
| Docker 编排（ES/RabbitMQ/MongoDB） | `docker-compose/docker-compose.yml` |
| launch.json（7 个后端服务） | `.vscode/launch.json` |
