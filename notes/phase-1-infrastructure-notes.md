# 阶段一：微服务基础设施 & 架构梳理

> 日期：2026-06-12  
> 范围：Docker 中间件 + yb-common + yb-gateway + yb-user  
> 状态：✅ 全部调通

---

## 一、整体架构

```
                        ┌──────────────┐
                        │    Nacos     │  注册中心 + 配置中心
                        │   :8848      │
                        └──────┬───────┘
                           ▲   │
              注册自己      │   │  查询 "yb-user" 的地址
              启动时上报     │   │  路由时查询
                           │   ▼
┌──────────┐           ┌──────────────┐           ┌──────────┐
│  浏览器   │ ───────▶ │  yb-gateway  │ ───────▶ │  yb-user │
│  curl    │  :8080   │  路由+鉴权    │  lb://    │  :8082   │
│  IDEA    │          │  AuthFilter   │  转发     │  CRUD    │
└──────────┘          └──────────────┘           └────┬─────┘
                                                      │
                                                      ▼
                                                ┌──────────┐
                                                │  MySQL   │
                                                │  :3306   │
                                                └──────────┘
```

### 模块分层

```
yb-cloud-parent/
├── yb-common/          ← 公共 POJO、枚举、异常、常量（被所有服务依赖）
├── yb-common-web/      ← Web 层通用配置（全局异常处理、TraceId 等）
├── yb-common-mybatis/  ← MyBatis-Plus 通用配置（分页插件、自动填充等）
├── yb-api/             ← Feign 接口 & DTO（服务间调用的契约）
│
├── yb-gateway/ :8080   ← 网关（路由转发 + JWT 鉴权）
├── yb-user/    :8082   ← 用户服务（CRUD + 收货地址）
│
├── yb-auth/            ← 【阶段二】认证服务
├── yb-product/         ← 【阶段二】商品服务
├── ...                 ← 后续阶段逐步启用
│
├── docker-compose/     ← 本地中间件编排
├── sql/                ← 数据库初始化脚本
└── notes/              ← 笔记合集（当前目录）
```

---

## 二、Nacos：服务注册 & 发现

### 2.1 服务是怎么注册上去的？

**需要的条件**（缺一不可）：

| 条件 | 代码位置 | 说明 |
|------|---------|------|
| 依赖包 | `pom.xml` → `spring-cloud-starter-alibaba-nacos-discovery` | 引入 Nacos 客户端 |
| 注解 | `XxxApplication.java` → `@EnableDiscoveryClient` | 激活服务发现 |
| 配置 | `application.yml` → `spring.cloud.nacos.discovery.server-addr` | 告诉它 Nacos 在哪 |
| 服务名 | `application.yml` → `spring.application.name` | 注册到 Nacos 的名字 |

**示例**（[yb-user/UserApplication.java](../yb-user/src/main/java/com/yb/user/UserApplication.java)）：

```java
@SpringBootApplication
@EnableDiscoveryClient    // ← 激活
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
```

**示例**（[yb-user/application.yml](../yb-user/src/main/resources/application.yml)）：

```yaml
spring:
  application:
    name: yb-user          # ← 注册名
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848   # ← Nacos 地址
```

### 2.2 注册流程（启动时自动完成）

```
服务启动
  │
  ├─ @EnableDiscoveryClient 激活 Nacos 客户端
  ├─ 读取 server-addr = localhost:8848
  ├─ 读取 application.name = "yb-user"
  ├─ 获取本机 IP + 端口 (192.168.1.221:8082)
  │
  ├─ ── 发送注册请求 ──▶ Nacos Server
  │                        │
  │                    注册表存入：
  │                    {
  │                      "yb-user": [{
  │                        "ip": "192.168.1.221",
  │                        "port": 8082,
  │                        "healthy": true
  │                      }]
  │                    }
  │
  ├─ 每 5 秒发心跳，告诉 Nacos "我还活着"
  └─ Nacos 15 秒收不到心跳 → 标记 unhealthy → 30 秒后剔除

yb-gateway 启动后做完全一样的事 ────▶ 注册表里多一条 "yb-gateway": [...]
```

**验证**：浏览器打开 `http://localhost:8848/nacos` → 服务管理 → 服务列表

---

## 三、Gateway：路由转发 & 鉴权

### 3.1 路由是怎么找到下游服务的？

**核心配置**（[yb-gateway/application.yml](../yb-gateway/src/main/resources/application.yml) 第 31-48 行）：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: yb-user
          uri: lb://yb-user              # ← "lb://" = 负载均衡查找
          predicates:
            - Path=/api/user/**          # ← URL 匹配规则
          filters:
            - StripPrefix=0              # ← 不删前缀
```

### 3.2 术语解释

| 术语 | 含义 | 类比 |
|------|------|------|
| **Route** | 一条路由规则（ID + 目标 + 断言 + 过滤器） | 外卖配送路线 |
| **Predicate** | 断言，判断请求是否匹配此路由 | "地址是不是朝阳区？" |
| **Filter** | 过滤器，对请求/响应做处理 | 包装、贴标签、检查 |
| **`lb://`** | Load Balanced URI，告诉 Gateway 去注册中心查 | "去地址簿查这个店在哪" |

### 3.3 一次请求的完整旅程

```
curl http://localhost:8080/api/user/page?page=1
      │
      ▼
┌─ yb-gateway :8080 ──────────────────────────────────────────┐
│                                                              │
│  ① AuthGlobalFilter（全局过滤器，最先执行）                    │
│     path = "/api/user/page"                                  │
│     ├─ 白名单匹配 "/api/user" → 放行                          │
│     └─ chain.filter(exchange)                                │
│                                                              │
│  ② RoutePredicateHandlerMapping（路由匹配）                    │
│     ├─ 遍历所有 Route                                         │
│     ├─ Path=/api/user/** 命中路由 ID 为 "yb-user"             │
│     └─ 获取 uri = lb://yb-user                                │
│                                                              │
│  ③ ReactiveLoadBalancerClientFilter（负载均衡）                │
│     ├─ 问 Nacos："谁叫 yb-user？"                              │
│     ├─ Nacos 返回：[192.168.1.221:8082]                       │
│     └─ 选一个实例（多实例时轮询/加权）                           │
│                                                              │
│  ④ NettyRoutingFilter（实际转发）                              │
│     └─ HTTP 请求 → http://192.168.1.221:8082/api/user/page   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
      │
      ▼
┌─ yb-user :8082 ─────────────────────────────────────────────┐
│  UserController.pageUser()                                   │
│    → UserService.page()                                      │
│      → UserMapper.selectPage()                               │
│        → MySQL: SELECT * FROM t_user LIMIT 10                │
│    ← 返回 JSON                                               │
└──────────────────────────────────────────────────────────────┘
      │
      ▼
   响应原路返回给客户端
```

### 3.4 JWT 鉴权过滤器

**代码**：[AuthGlobalFilter.java](../yb-gateway/src/main/java/com/yb/gateway/filter/AuthGlobalFilter.java)

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)  // 高优先级，在路由之前执行
public class AuthGlobalFilter implements GlobalFilter {

    // 白名单：这些路径不需要 Token
    private static final String[] WHITE_LIST = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/search",
        "/api/product/page",
        "/api/user",         // 阶段一临时放行
    };
}
```

**执行流程**：

```
请求进入 AuthGlobalFilter
  │
  ├─ ① URL 在白名单？ → 直接放行
  │
  ├─ ② 请求头有 Authorization: Bearer <token>？ → 没有 → 401
  │
  ├─ ③ 用 jwt.secret 解密 Token → 失败 → 401
  │
  └─ ④ 解密成功，提取 userId、role
       ├─ 塞入请求头 X-User-Id
       ├─ 塞入请求头 X-User-Role
       └─ 放行，下游服务可以直接从请求头读用户信息
```

> **GlobalFilter vs GatewayFilter**：  
> - `GlobalFilter` = 对所有路由生效（如鉴权）  
> - `GatewayFilter` = 只对配置了它的路由生效（如 `StripPrefix`，如未来阶段二的 `AuthFilter`）

### 3.5 路由注册全流程总结

```
Gateway 启动
    │
    ├── ① @EnableDiscoveryClient → 把自己注册到 Nacos
    │
    ├── ② 加载 application.yml 中的 routes 配置
    │     每个 route：{ id, uri(lb://), predicates, filters }
    │
    ├── ③ 启动 Netty Server，监听 8080
    │
    └── ④ 请求进来时：
         ├─ GlobalFilter 链（AuthGlobalFilter 在这）
         ├─ 路由匹配（Predicate）
         ├─ GatewayFilter 链（StripPrefix 在这）
         ├─ 负载均衡（lb:// → Nacos 查询 → 真实地址）
         └─ 转发请求
```

---

## 四、yb-user：业务服务

### 4.1 项目结构

```
yb-user/
├── pom.xml
├── src/main/java/com/yb/user/
│   ├── UserApplication.java      ← 启动类
│   ├── controller/
│   │   ├── UserController.java   ← /api/user/*
│   │   └── AddressController.java ← /api/user/address/*
│   ├── service/
│   │   ├── UserService.java
│   │   └── impl/UserServiceImpl.java
│   ├── mapper/
│   │   └── UserMapper.java
│   ├── entity/
│   │   └── UserEntity.java
│   └── dto/
│       ├── UserSaveReq.java
│       └── UserPageQuery.java
└── src/main/resources/
    ├── application.yml
    └── mapper/UserMapper.xml
```

### 4.2 API 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/user/{userId}` | 简易查询（给 Feign 调用） |
| `GET` | `/api/user/{userId}/detail` | 详情查询 |
| `GET` | `/api/user/page?page=1&size=10` | 分页查询 |
| `POST` | `/api/user` | 新增用户 |
| `PUT` | `/api/user/{userId}` | 更新用户 |
| `DELETE` | `/api/user/{userId}` | 逻辑删除 |
| `GET` | `/api/user/address/{userId}/list` | 地址列表 |
| `POST` | `/api/user/address` | 新增地址 |
| `PUT` | `/api/user/address/{id}` | 更新地址 |

---

## 五、Docker 中间件

### 5.1 容器清单

| 服务 | 容器名 | 端口 | 镜像 |
|------|--------|------|------|
| MySQL 8.0 | `yb-mysql` | 3306 | `mysql:8.0` |
| Redis 7 | `yb-redis` | 6379 | `redis:7-alpine` |
| Elasticsearch 8 | `yb-es` | 9200/9300 | `docker.elastic.co/elasticsearch/elasticsearch:8.12.0` |
| Kibana | `yb-kibana` | 5601 | `docker.elastic.co/kibana/kibana:8.12.0` |
| RabbitMQ 3.12 | `yb-rabbitmq` | 5672/15672 | `rabbitmq:3.12-management-alpine` |
| MongoDB 7 | `yb-mongo` | 27017 | `mongo:7` |
| Nacos 2.3 | `yb-nacos` | 8848/9848 | `nacos/nacos-server:v2.3.2` |
| Sentinel | `yb-sentinel` | 8858 | `docker.io/bladex/sentinel-dashboard:1.8.6` |

### 5.2 MySQL 自动初始化原理

```
容器首次启动
  │
  ├─ MySQL 官方 entrypoint 检查 /var/lib/mysql 是否为空
  │
  ├─ 为空（首次启动）
  │   ├─ ① 执行 mysql_install_db 创建系统表
  │   ├─ ② 临时启动 MySQL
  │   ├─ ③ 按字母顺序执行 /docker-entrypoint-initdb.d/ 下的 .sql / .sh 文件
  │   └─ ④ 停止临时服务，正式启动
  │
  └─ 不为空（已有数据）→ 跳过初始化

docker-compose.yml:
  volumes:
    - ../sql:/docker-entrypoint-initdb.d   ← 将本地 sql/ 目录映射进去
```

**⚠️ 注意事项**：
- 初始化只在 **首次启动** 时执行
- SQL 文件需要在开头加 `SET NAMES utf8mb4;`，否则中文注释会乱码
- 要重建数据库：`rm -rf mysql-data && docker-compose up -d`

---

## 六、阶段一踩坑记录

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | `docker-compose up -d` 报 `unknown escape character` | YAML 双引号字符串中 `\|` 非法 | 换 `grep -E`，用 `\|` 代替 `\|` |
| 2 | Nacos 报 `UnknownHostException: ${MYSQL_SERVICE_HOST}` | 配置了 `SPRING_DATASOURCE_PLATFORM=mysql` 但没配数据库连接 | 注释掉，改用内嵌 Derby |
| 3 | Sentinel 8858 访问不了 | 容器内监听 8858，但端口映射是 `8858:8080` | 改为 `8858:8858` |
| 4 | Gateway 报 `AuthFilter` 找不到 | 过滤器的 Java 类还没写 | 注释掉路由级 `AuthFilter` |
| 5 | `No spring.config.import property` | Spring Cloud 2023.x 必须显式声明 | 设置 `import-check.enabled=false` |
| 6 | 空 `config:` 节点导致启动失败 | Spring Boot 尝试绑定空配置 | 删除空的 `config:` 块 |
| 7 | yb-common 编译失败 | Lombok 注解未处理 + 文件名不匹配 | 手写构造器 + 拆分 MqConstants 为独立文件 |
| 8 | JDBC 报 `utf8mb4` 不支持 | JDBC 驱动认 Java 字符集名，不认 MySQL 内部名 | `characterEncoding=UTF-8` |
| 9 | Gateway 返回 401 | `AuthGlobalFilter` 全局拦截 JWT | 临时加 `/api/user` 到白名单 |

---

## 七、关键文件速查表

| 你想了解的事 | 看哪个文件 |
|-------------|----------|
| Nacos 注册怎么配 | `yb-user/.../application.yml` → `spring.cloud.nacos.discovery` |
| 服务发现依赖 | `yb-user/pom.xml` → `nacos-discovery` |
| 路由规则 | `yb-gateway/.../application.yml` → `spring.cloud.gateway.routes` |
| JWT 鉴权 | `yb-gateway/.../filter/AuthGlobalFilter.java` |
| 启动类 | `UserApplication.java` / `GatewayApplication.java` |
| 用户 API | `yb-user/.../controller/UserController.java` |
| 统一响应体 | `yb-common/.../dto/R.java` |
| 错误码 | `yb-common/.../enums/ErrorCode.java` |
| 中间件编排 | `docker-compose/docker-compose.yml` |
| SQL 初始化 | `sql/yb_user.sql` |

---

## 八、常用调试命令

```bash
# === Docker ===
docker-compose up -d                   # 启动所有中间件
docker-compose ps                      # 查看状态
docker-compose logs -f [服务名]          # 看日志
docker-compose down                    # 全停

# === 应用服务 ===
cd yb-user && mvn spring-boot:run     # 启动用户服务
cd yb-gateway && mvn spring-boot:run  # 启动网关

# === 编译 ===
mvn clean install -DskipTests          # 全量编译（在项目根目录）
mvn clean compile -DskipTests          # 只编译不打包

# === IDEA HTTP Client ===
POST http://localhost:8082/api/user    # 直连测试
Content-Type: application/json
{"phone":"13800001111","nickname":"测试","password":"123456"}

GET http://localhost:8080/api/user/page?page=1&size=10  # 网关测试
```
