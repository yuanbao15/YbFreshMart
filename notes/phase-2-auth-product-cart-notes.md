# 阶段二：认证授权 + 商品 + 购物车 + Redis

> 日期：2026-06-13  
> 范围：yb-common-security + yb-common-redis + yb-auth + yb-product + yb-cart  
> 状态：✅ 编译通过

---

## 一、整体架构

```
                            ┌──────────────┐
                            │    Nacos     │  注册中心 + 配置中心
                            │   :8848      │
                            └──────┬───────┘
                               ▲   │  查到 "yb-auth" → :8081
                               │   │  查到 "yb-product" → :8083
                               │   │  查到 "yb-cart" → :8085
                               │   ▼
┌──────────┐           ┌──────────────────────────────────────────┐
│  浏览器   │ ───────▶ │           yb-gateway :8080               │
│  curl    │  :8080   │  AuthGlobalFilter ─ 全局 JWT 鉴权          │
│  IDEA    │          │  白名单: /api/auth/**, /api/product/page   │
└──────────┘          └────┬──────┬──────┬──────────────────────┘
                           │      │      │
              ┌────────────┘      │      └────────────┐
              ▼                   ▼                   ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   yb-auth :8081  │  │  yb-product :8083│  │   yb-cart :8085  │
│   认证授权服务     │  │   商品服务        │  │   购物车服务      │
│                  │  │                  │  │                  │
│ • 登录/注册/刷新  │  │ • 类目树查询      │  │ • Redis Hash 存储 │
│ • Token 黑名单    │  │ • SPU/SKU CRUD  │  │ • 增删改查        │
│ • BCrypt 加密    │  │ • @Cacheable    │  │ • 数量叠加        │
│   ┌───────────┐  │  │   ┌───────────┐  │  │   ┌───────────┐  │
│   │ MySQL     │  │  │   │ MySQL     │  │  │   │  Redis    │  │
│   │ yb_auth   │  │  │   │ yb_product│  │  │   │  :6379    │  │
│   │ t_user    │  │  │   │ t_category│  │  │   │  Hash     │  │
│   └───────────┘  │  │   │ t_spu     │  │  │   └───────────┘  │
│                  │  │   │ t_sku     │  │  │                  │
│  (注册时调用      │  │   └───────────┘  │  │                  │
│   UserClient ────┼──▶   ┌───────────┐  │  │                  │
│   同步创建档案)   │  │   │  Redis    │  │  │                  │
│                  │  │   │  缓存      │  │  │                  │
└──────────────────┘  │   └───────────┘  │  └──────────────────┘
                      └──────────────────┘

公共模块：
┌─────────────────────────┐  ┌─────────────────────────┐
│  yb-common-security     │  │  yb-common-redis        │
│                         │  │                         │
│ • JwtUtil (生成/解析)    │  │ • RedisConfig (序列化)   │
│ • UserContext (ThreadLocal)│ • CacheConfig (@Cacheable)│
│ • UserInfoInterceptor   │  │ • RedisLock (Redisson)   │
│ • FeignAuthInterceptor  │  │                         │
└─────────────────────────┘  └─────────────────────────┘
```

### 模块分层

```
yb-cloud-parent/
├── yb-common-security/   ← JWT 工具 + 用户上下文 + Feign 鉴权拦截器  [阶段二]
├── yb-common-redis/      ← Redis 序列化 + Spring Cache + 分布式锁   [阶段二]
├── yb-auth/              ← 认证服务 :8081                          [阶段二]
├── yb-product/           ← 商品服务 :8083                          [阶段二]
├── yb-cart/              ← 购物车服务 :8085                        [阶段二]
│
├── yb-common/            ← [阶段一] 公共 POJO、枚举、异常
├── yb-common-web/        ← [阶段一] 全局异常处理、TraceId
├── yb-common-mybatis/    ← [阶段一] MyBatis-Plus 分页、BaseEntity
├── yb-api/               ← [阶段一] Feign 接口 & DTO
├── yb-gateway/           ← [阶段一] API 网关 :8080
└── yb-user/              ← [阶段一] 用户服务 :8082
```

---

## 二、yb-common-security：JWT 工具 + Feign 拦截器

### 2.1 模块定位

为所有业务微服务提供统一的身份认证基础设施。Gateway 完成 JWT 解析后，通过请求头向下游传递用户信息，业务代码通过 `UserContext` 直接获取当前用户。

**⚠️ 注意**：Gateway 是 WebFlux 环境，不引入此模块。Gateway 的 `AuthGlobalFilter` 直接用 jjwt 解析 Token，保持独立。

### 2.2 文件清单

| 文件 | 职责 |
|------|------|
| [`JwtUtil.java`](../yb-common-security/src/main/java/com/yb/common/security/util/JwtUtil.java) | JWT 生成、解析、校验、刷新 |
| [`UserContext.java`](../yb-common-security/src/main/java/com/yb/common/security/context/UserContext.java) | ThreadLocal 封装，提供 `getCurrentUserId()`、`getCurrentUserRole()` |
| [`UserInfoInterceptor.java`](../yb-common-security/src/main/java/com/yb/common/security/interceptor/UserInfoInterceptor.java) | 从请求头提取 `X-User-Id`/`X-User-Role` → 存入 UserContext |
| [`FeignAuthInterceptor.java`](../yb-common-security/src/main/java/com/yb/common/security/feign/FeignAuthInterceptor.java) | Feign 调用时自动透传 `Authorization` 到下游服务 |
| [`SecurityAutoConfiguration.java`](../yb-common-security/src/main/java/com/yb/common/security/config/SecurityAutoConfiguration.java) | `@AutoConfiguration`，自动注册拦截器 + JwtUtil Bean |

### 2.3 JWT Token 结构

**Payload**：
```json
{
  "userId": 123456789,
  "role": "user",
  "iat": "2026-06-13T10:00:00",
  "exp": "2026-06-14T10:00:00"
}
```

**签名**：HMAC-SHA256（密钥通过 `jwt.secret` 配置注入）

### 2.4 调用链：Token 是如何从 Gateway 一路传递到下游的？

```
① 客户端 → Authorization: Bearer <token>

② Gateway AuthGlobalFilter
    ├─ 用 jjwt 解析 Token，提取 userId、role
    ├─ 放入请求头: X-User-Id=<userId>, X-User-Role=<role>
    └─ 转发到下游服务

③ 下游服务（如 yb-product）
    ├─ UserInfoInterceptor.preHandle()
    │    从请求头提取 X-User-Id/X-User-Role
    │    → UserContext.setCurrentUserId() / setCurrentUserRole()
    │
    ├─ Controller 中通过 UserContext.getCurrentUserId() 获取用户
    │
    ├─ 如果 Feign 调用其他服务：
    │    FeignAuthInterceptor.apply()
    │    从当前请求上下文取出原始 Authorization 头
    │    → 自动放入 Feign 请求头，传递到下一个服务
    │
    └─ UserInfoInterceptor.afterCompletion()
         UserContext.clear()  ← 防止内存泄漏

④ 被调用服务（如 yb-cart）
    └─ 同样通过 UserInfoInterceptor 获取用户信息
```

### 2.5 自动配置原理

Spring Boot 3.x 不再使用 `spring.factories`，改用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件：

```
com.yb.common.security.config.SecurityAutoConfiguration
```

任何引入 `yb-common-security` 的微服务，启动时会自动加载 `SecurityAutoConfiguration`：
- 注册 `JwtUtil` Bean（从 `application.yml` 读取 `jwt.secret` 和 `jwt.expiration`）
- 注册 `UserInfoInterceptor` 到 WebMvc 拦截器链
- 注册 `FeignAuthInterceptor` 到 Feign 拦截器链（按需，依赖 `@ConditionalOnClass`）

---

## 三、yb-common-redis：Redis 配置 + 缓存 + 分布式锁

### 3.1 模块定位

封装 Redis 的通用配置和工具，其他服务引入后即可直接使用 `RedisTemplate`、`@Cacheable`、`RedisLock`。

### 3.2 文件清单

| 文件 | 职责 |
|------|------|
| [`RedisConfig.java`](../yb-common-redis/src/main/java/com/yb/common/redis/config/RedisConfig.java) | 配置 `RedisTemplate<String, Object>`，Key 用 String 序列化，Value 用 Jackson JSON 序列化 |
| [`CacheConfig.java`](../yb-common-redis/src/main/java/com/yb/common/redis/config/CacheConfig.java) | 启用 `@EnableCaching`，配置 `RedisCacheManager`，不同缓存名不同 TTL |
| [`RedisLock.java`](../yb-common-redis/src/main/java/com/yb/common/redis/lock/RedisLock.java) | 分布式锁封装，基于 Redisson |

### 3.3 序列化方案

```
RedisTemplate<String, Object>
  ├─ Key 序列化: StringRedisSerializer.UTF_8
  ├─ Value 序列化: Jackson2JsonRedisSerializer (支持 LocalDateTime)
  ├─ HashKey 序列化: StringRedisSerializer.UTF_8
  └─ HashValue 序列化: Jackson2JsonRedisSerializer
```

**⚠️ Redis 连接配置**：
- 各服务的 `application.yml` 需要配置 `spring.data.redis.password`，Docker Compose 中 Redis 默认密码为 `redis123`
- 配置示例：
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
        password: redis123
  ```

**⚠️ 注意事项**：
- Jackson 需要注册 `JavaTimeModule` 才能正确序列化 `LocalDateTime`
- 序列化时附带类型信息 `@class`，反序列化才能还原为正确的 Java 对象

### 3.4 缓存 TTL 配置

| 缓存名 | TTL | 用途 |
|--------|-----|------|
| `product:detail` | 30 分钟 | 商品 SKU 详情缓存 |
| `product:categories` | 1 小时 | 商品类目树缓存 |
| 默认 | 10 分钟 | 其他缓存 |

### 3.5 RedisLock 使用示例

```java
// 有返回值
String result = redisLock.tryRun("lock:stock:sku:1001", 3, 10, TimeUnit.SECONDS, () -> {
    return deductStock(skuId, quantity);
});

// 无返回值
redisLock.tryRun("lock:stock:sku:1001", 3, 10, TimeUnit.SECONDS, () -> {
    deductStock(skuId, quantity);
});
```

参数说明：
- `waitTime=3`：最多等 3 秒
- `leaseTime=10`：锁持有 10 秒后自动释放（防止死锁）
- 获取锁失败时返回 `null`（业务代码自行处理）

---

## 四、yb-auth：认证授权服务 (:8081)

### 4.1 模块定位

提供手机号+密码的登录/注册，JWT Token 签发/刷新/登出管理。仅存储认证相关字段（手机号、密码、状态），用户档案（昵称、头像等）由 yb-user 管理。

### 4.2 文件清单

```
yb-auth/
├── pom.xml
├── src/main/resources/application.yml
└── src/main/java/com/yb/auth/
    ├── AuthApplication.java           ← 启动类
    ├── entity/
    │   └── UserEntity.java            ← t_user 实体（phone, password, status, lastLogin）
    ├── mapper/
    │   └── UserMapper.java            ← MyBatis-Plus BaseMapper
    ├── dto/
    │   ├── LoginReq.java              ← {phone, password}
    │   ├── RegisterReq.java           ← {phone, password, nickname}
    │   └── LoginResp.java             ← {token, userId, phone, ...}
    ├── service/
    │   ├── AuthService.java           ← 接口
    │   └── impl/AuthServiceImpl.java  ← 核心实现
    └── controller/
        └── AuthController.java        ← REST API
```

### 4.3 数据库表

**数据库**：`yb_auth`（SQL：[`sql/yb_auth.sql`](../sql/yb_auth.sql)）

```sql
CREATE TABLE t_user (
    id          BIGINT PRIMARY KEY COMMENT '用户ID',
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    status      TINYINT DEFAULT 1 COMMENT '0-禁用, 1-正常',
    last_login  DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除'
);
```

### 4.4 API 接口清单

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/api/auth/login` | 手机号+密码登录 | 白名单 |
| `POST` | `/api/auth/register` | 手机号注册 | 白名单 |
| `POST` | `/api/auth/refresh` | 刷新 Token | 白名单（旧 Token 通过 Header 传递） |
| `POST` | `/api/auth/logout` | 登出（Token 加入 Redis 黑名单） | 需登录 |
| `GET` | `/api/auth/me` | 获取当前用户 ID/角色 | 需登录 |

### 4.5 登录流程

```
POST /api/auth/login {"phone":"138...","password":"123456"}
      │
      ▼
AuthServiceImpl.login()
      │
      ├─ ① getByPhone(phone)  → 查 yb_auth.t_user
      │     ├─ 用户不存在 → USER_NOT_FOUND
      │     └─ status=0 → USER_ACCOUNT_DISABLED
      │
      ├─ ② BCrypt.checkpw(password, user.password)
      │     └─ 不匹配 → USER_PASSWORD_ERROR
      │
      ├─ ③ jwtUtil.generateToken(userId, "user")
      │     生成 JWT，含 {userId, role, iat, exp}
      │
      ├─ ④ 更新 last_login 时间
      │
      └─ ⑤ 返回 LoginResp { token, userId, phone, nickname, role, loginTime }
                              │
                              └─ nickname 通过 Feign 从 yb-user 获取
```

### 4.6 注册流程

```
POST /api/auth/register {"phone":"138...","password":"123456","nickname":"张三"}
      │
      ▼
AuthServiceImpl.register()
      │
      ├─ ① 检查手机号是否已存在 → USER_PHONE_EXIST
      │
      ├─ ② 插入 yb_auth.t_user
      │     ├─ phone = req.phone
      │     ├─ password = BCrypt.hashpw(req.password)  ← 不可逆加密
      │     ├─ status = 1
      │     └─ id = 雪花算法生成
      │
      ├─ ③ 通过 UserClient.createUser() 调用 yb-user
      │     同步创建用户档案（昵称、头像等存在 yb_user.t_user）
      │     失败不阻塞注册 ← catch 异常只打日志
      │
      ├─ ④ jwtUtil.generateToken(userId, "user") 生成 Token
      │
      └─ ⑤ 返回 LoginResp
```

### 4.7 Token 刷新与登出

**刷新（refresh）**：
```
① 旧 Token 通过 Authorization Header 传入
② jwtUtil.getUserId(oldToken) + jwtUtil.getUserRole(oldToken)
③ 旧 Token 剩余有效期 > 0 →
    redisTemplate.opsForValue().set("token:blacklist:<oldToken>", "1", TTL)
④ jwtUtil.refreshToken(oldToken) → 生成新 Token
⑤ 返回新 LoginResp
```

**登出（logout）**：
```
① Token → redisTemplate.opsForValue().set("token:blacklist:<token>", "1", TTL)
② TTL = Token 剩余的过期时间
③ 后续请求 → Gateway AuthGlobalFilter 检查黑名单
```

### 4.8 yb-auth.t_user vs yb-user.t_user

| 维度 | yb-auth.t_user | yb-user.t_user |
|------|---------------|----------------|
| 数据库 | `yb_auth` | `yb_user` |
| 字段 | phone, password, status, last_login | phone, nickname, avatar, email, gender, ... |
| 职责 | 认证（能不能登录） | 档案（用户叫什么、长什么样） |
| 关联 | — | 同一个 id（注册时 yb-auth 生成 id，yb-user 复用此 id） |

---

## 五、yb-product：商品服务 (:8083)

### 5.1 模块定位

管理商品的三级数据结构：**类目 → SPU → SKU**。类目树和 SKU 详情使用 `@Cacheable` 缓存到 Redis，写操作通过 `@CacheEvict` 自动清除缓存。

### 5.2 文件清单

```
yb-product/
├── pom.xml
├── src/main/resources/application.yml
└── src/main/java/com/yb/product/
    ├── ProductApplication.java
    ├── entity/
    │   ├── CategoryEntity.java       ← t_category（类目）
    │   ├── SpuEntity.java            ← t_spu（标准化产品单元）
    │   └── SkuEntity.java            ← t_sku（库存量单位）
    ├── mapper/
    │   ├── CategoryMapper.java
    │   ├── SpuMapper.java
    │   └── SkuMapper.java
    ├── dto/
    │   └── CategoryTreeDTO.java      ← 类目树节点（含 children）
    ├── service/
    │   ├── CategoryService.java + impl
    │   ├── SpuService.java + impl
    │   └── SkuService.java + impl
    └── controller/
        ├── CategoryController.java   ← /api/product/category/**
        ├── SpuController.java        ← /api/product/spu/**
        └── SkuController.java        ← /api/product/{skuId}, /api/product/page, /api/product/sku/**
```

### 5.3 数据库表

**数据库**：`yb_product`（SQL：[`sql/yb_product.sql`](../sql/yb_product.sql)）

**三级结构**：

```
t_category（类目）
  ├─ id=1, parent_id=0, name="蔬菜水果", level=1
  │   ├─ id=11, parent_id=1, name="叶菜类", level=2
  │   │   ├─ id=111, parent_id=11, name="菠菜", level=3
  │   │   └─ id=112, parent_id=11, name="生菜", level=3
  │   └─ id=12, parent_id=1, name="根茎类", level=2
  ├─ id=2, parent_id=0, name="肉禽蛋品", level=1
  └─ ...

t_spu（标准化产品单元，挂在类目下）
  └─ id=1001, category_id=11, name="有机菠菜", brand="XXX农场"

t_sku（库存量单位，挂在 SPU 下，区分规格）
  ├─ id=10001, spu_id=1001, spec="500g",  price=9.90
  ├─ id=10002, spu_id=1001, spec="1kg",   price=18.00
  └─ id=10003, spu_id=1001, spec="2.5kg", price=39.90
```

### 5.4 API 接口清单

**类目（CategoryController）**：

| 方法 | 路径 | 说明 | 缓存 |
|------|------|------|------|
| `GET` | `/api/product/category/tree` | 获取完整类目树 | `@Cacheable("product:categories")` |
| `GET` | `/api/product/category/{id}` | 获取类目详情 | — |
| `POST` | `/api/product/category` | 新增类目 | `@CacheEvict("product:categories")` |
| `PUT` | `/api/product/category/{id}` | 更新类目 | `@CacheEvict("product:categories")` |
| `DELETE` | `/api/product/category/{id}` | 删除类目 | `@CacheEvict("product:categories")` |

**SPU（SpuController）**：

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/product/spu/page?page=1&size=10&categoryId=11` | 分页查询 SPU |
| `GET` | `/api/product/spu/{id}` | SPU 详情 |
| `POST` | `/api/product/spu` | 新增 SPU |
| `PUT` | `/api/product/spu/{id}` | 更新 SPU |

**SKU（SkuController）**：同时承担 Feign 接口契约

| 方法 | 路径 | 说明 | 缓存 |
|------|------|------|------|
| `GET` | `/api/product/{skuId}` | SKU 详情（Feign 契约：`ProductClient.getSkuById`） | `@Cacheable("product:detail")` |
| `GET` | `/api/product/page?page=1&size=10` | 分页查询 SKU（Feign 契约：`ProductClient.page`） | — |
| `GET` | `/api/product/sku/list?spuId=1001` | 查询 SPU 下的所有 SKU | — |
| `PUT` | `/api/product/sku/{id}/stock?stock=100` | 更新库存 | `@CacheEvict("product:detail")` |

### 5.5 类目树构建算法

```
getTree()
  │
  ├─ SELECT * FROM t_category ORDER BY sort_order ASC
  │   查出所有类目（一次查询，不分页）
  │
  ├─ 按 parentId 分组：Map<parentId, List<CategoryEntity>>
  │   childrenMap = {0: [蔬菜, 肉禽], 1: [叶菜, 根茎], 11: [菠菜, 生菜], ...}
  │
  ├─ 遍历 parentId=0 的一级类目
  │   └─ 递归调用 buildChildren(id, childrenMap)
  │       对每个子类目继续递归，构建子树
  │
  └─ 返回 List<CategoryTreeDTO>（最多 3 级深）
```

⚠️ **注意**：`getTree()` 加了 `@Cacheable`，首次查询后缓存到 Redis；增删改类目时 `@CacheEvict` 自动失效缓存。

### 5.6 缓存策略总结

| 操作 | 缓存行为 |
|------|---------|
| 查询类目树 | `@Cacheable("product:categories", key="'tree'")` → 缓存 1 小时 |
| 新增/更新/删除类目 | `@CacheEvict("product:categories", key="'tree'")` → 清空 |
| 查询 SKU 详情 | `@Cacheable("product:detail", key="#id")` → 缓存 30 分钟 |
| 更新库存 | `@CacheEvict("product:detail", key="#id")` → 清空单个 SKU |

---

## 六、yb-cart：购物车服务 (:8085)

### 6.1 模块定位

纯 Redis 实现的购物车，不依赖 MySQL。购物车数据从 Redis 实时读取，服务重启不丢失。

### 6.2 文件清单

```
yb-cart/
├── pom.xml
├── src/main/resources/application.yml
└── src/main/java/com/yb/cart/
    ├── CartApplication.java
    ├── dto/
    │   └── CartItemDTO.java          ← 购物车单项
    ├── service/
    │   ├── CartService.java          ← 接口
    │   └── impl/CartServiceImpl.java ← Redis Hash 操作
    └── controller/
        └── CartController.java       ← REST API
```

### 6.3 Redis 数据结构

```
Key:   cart:{userId}          （例如 cart:1001）
Type:  Hash

Field            | Value (CartItemDTO JSON)
─────────────────┼─────────────────────────────────────────────
10001 (skuId)    | {"skuId":10001,"spuId":1001,"name":"有机菠菜",
                 |  "image":"http://...","price":9.90,
                 |  "quantity":3,"addTime":"2026-06-13T10:30:00"}
                 |
10002 (skuId)    | {"skuId":10002,"spuId":1001,"name":"有机菠菜",
                 |  "image":"http://...","price":18.00,
                 |  "quantity":1,"addTime":"2026-06-13T10:31:00"}
```

**为什么用 Hash 而不是 String？**
- Hash 可以按 SKU 独立操作（增删改一个商品不影响其他）
- `HGET cart:1001 10001` → O(1) 查单个商品
- `HDEL cart:1001 10001` → O(1) 删除
- String 需要整体序列化/反序列化，大购物车性能差

### 6.4 API 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/cart/{userId}` | 查询购物车 → `Map<skuId, quantity>`（Feign 契约：`CartClient.getCart`） |
| `GET` | `/api/cart/{userId}/detail` | 查询购物车详情（含商品名称、价格等） |
| `POST` | `/api/cart/{userId}/items` | 添加商品（Body: CartItemDTO），已存在则叠加数量 |
| `PUT` | `/api/cart/{userId}/items/{skuId}?quantity=5` | 修改商品数量 |
| `DELETE` | `/api/cart/{userId}/items/{skuId}` | 删除单个商品 |
| `DELETE` | `/api/cart/{userId}/clear` | 清空购物车（Feign 契约：`CartClient.clearCart`） |

### 6.5 添加商品流程

```
POST /api/cart/1001/items  Body: {skuId:10001, name:"有机菠菜", price:9.90, quantity:2}
      │
      ▼
CartServiceImpl.addItem(1001, item)
      │
      ├─ key = "cart:1001"
      ├─ field = "10001"
      │
      ├─ HGET cart:1001 10001
      │    ├─ 已存在 → existingItem.quantity += 2  → HSET cart:1001 10001 newItem
      │    └─ 不存在 → item.addTime = now  → HSET cart:1001 10001 item
      │
      └─ 日志: "添加商品到购物车, userId=1001, skuId=10001"
```

---

## 七、Gateway 鉴权调整

### 7.1 白名单变更

在 [`AuthGlobalFilter.java`](../yb-gateway/src/main/java/com/yb/gateway/filter/AuthGlobalFilter.java) 中：

```diff
- "/api/user",        // TODO: 阶段一调试放行，阶段二接入 auth 后移除
+ // "/api/user",     // 阶段二：正式接入 auth，不再放行
```

### 7.2 当前白名单

| 路径 | 原因 |
|------|------|
| `/api/auth/login` | 登录 |
| `/api/auth/register` | 注册 |
| `/api/auth/refresh` | Token 刷新 |
| `/api/search` | 搜索（公开） |
| `/api/product/page` | 商品列表（公开浏览） |
| `/api/product/category` | 类目树（公开浏览） |
| `/doc.html`, `/swagger-ui`, `/webjars`, `/v3/api-docs` | API 文档 |

### 7.3 鉴权流程

```
请求进入 Gateway
  │
  ├─ ① 路径在白名单？ → 直接放行
  │
  ├─ ② 请求头有 Authorization: Bearer <token>？
  │     没有 → 401 UNAUTHORIZED
  │
  ├─ ③ 解析 JWT → 失败 → 401
  │
  ├─ ④ Token 在黑名单（Redis key=token:blacklist:<token>）？
  │     TODO: 阶段三实现（需要 yb-common-redis 依赖 Gateway）
  │
  └─ ⑤ 放行，请求头注入 X-User-Id、X-User-Role
```

---

## 八、开发调试

### 8.1 启动顺序

```bash
# 1. 启动中间件
cd docker-compose
docker-compose up -d mysql redis nacos    # 阶段二只需这 3 个
# ⚠️ Redis 密码: redis123（见 docker-compose.yml command 配置）
# 各服务 application.yml 需配置 spring.data.redis.password=redis123

# 2. 基础服务（阶段一已有）
cd yb-user && mvn spring-boot:run         # :8082
cd yb-gateway && mvn spring-boot:run      # :8080

# 3. 阶段二新服务
cd yb-auth && mvn spring-boot:run         # :8081
cd yb-product && mvn spring-boot:run      # :8083
cd yb-cart && mvn spring-boot:run         # :8085
```

### 8.2 测试命令

```bash
# ========== 认证 ==========
# 注册
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456","nickname":"测试用户"}'

# 登录
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}'
# 返回: {"code":200,"data":{"token":"eyJ...","userId":...,...}}

# 刷新 Token
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Authorization: Bearer <token>"

# 登出
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer <token>"

# ========== 商品 ==========
# 类目树（公开）
curl http://localhost:8080/api/product/category/tree

# 新增类目（需登录）
curl -X POST http://localhost:8080/api/product/category \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"parentId":0,"name":"蔬菜水果","level":1,"sortOrder":1}'

# 查询 SKU（公开）
curl http://localhost:8080/api/product/page?page=1&size=10

# ========== 购物车（需登录） ==========
# 添加商品
curl -X POST http://localhost:8080/api/cart/1/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"skuId":10001,"spuId":1001,"name":"有机菠菜","price":9.90,"quantity":2}'

# 查询购物车
curl http://localhost:8080/api/cart/1 \
  -H "Authorization: Bearer <token>"

# 查询购物车详情
curl http://localhost:8080/api/cart/1/detail \
  -H "Authorization: Bearer <token>"
```

### 8.3 Redis 调试命令

```bash
# Redis 已设置密码，需要 -a 参数认证
# 进入 Redis 容器
docker exec -it yb-redis redis-cli -a redis123

# 查看购物车
redis-cli -a redis123 HGETALL cart:1

# 查看 Token 黑名单
redis-cli -a redis123 KEYS "token:blacklist:*"

# 查看缓存
redis-cli -a redis123 KEYS "product:*"

# 查看缓存 TTL
redis-cli -a redis123 TTL "product:categories::tree"
```

---

## 九、关键设计决策

| # | 决策 | 理由 |
|---|------|------|
| 1 | Gateway 不引入 yb-common-security | WebFlux 和 Servlet 环境不兼容；Gateway 直接使用 jjwt 即可 |
| 2 | yb-auth 和 yb-user 分离 t_user 表 | 关注点分离：认证 ≠ 档案。未来可独立扩缩 |
| 3 | 购物车纯 Redis 存储 | 购物车是高频读写场景，Redis 性能远超 MySQL；数据丢失可接受（非核心数据） |
| 4 | 购物车用 Hash 不用 String | Hash 支持按 SKU 独立读写，不需要整取整存 |
| 5 | Token 黑名单存 Redis 而非数据库 | 登出后需要快速校验黑名单；黑名单 TTL 与 Token 过期时间一致，自动清理 |
| 6 | @Cacheable 只缓存读多写少的数据 | 类目树 → 1 小时，SKU 详情 → 30 分钟。写操作立即失效缓存 |
| 7 | 注册时 Feign 调用失败不阻塞注册 | yb-user 可能暂时不可用，不影响核心注册流程（幂等补偿后续处理） |
| 8 | 自动配置使用 `@AutoConfiguration` + 新 imports 文件 | Spring Boot 3.x 标准方式，替代已废弃的 `spring.factories` |

## 十、关键文件速查表

| 你想了解的事 | 看哪个文件 |
|-------------|----------|
| JWT 生成/解析 | `yb-common-security/.../util/JwtUtil.java` |
| UserContext 怎么用 | `yb-common-security/.../context/UserContext.java` |
| Feign 如何透传 Token | `yb-common-security/.../feign/FeignAuthInterceptor.java` |
| Redis 序列化怎么配 | `yb-common-redis/.../config/RedisConfig.java` |
| 缓存 TTL 怎么设 | `yb-common-redis/.../config/CacheConfig.java` |
| 分布式锁怎么用 | `yb-common-redis/.../lock/RedisLock.java` |
| 登录/注册逻辑 | `yb-auth/.../service/impl/AuthServiceImpl.java` |
| yb-auth 的 t_user 表结构 | `sql/yb_auth.sql` |
| 类目树构建算法 | `yb-product/.../service/impl/CategoryServiceImpl.java` |
| SPU/SKU CRUD | `yb-product/.../service/impl/SpuServiceImpl.java` + `SkuServiceImpl.java` |
| 缓存注解在哪加的 | `CategoryServiceImpl.java` + `SkuServiceImpl.java` |
| 购物车 Redis 操作 | `yb-cart/.../service/impl/CartServiceImpl.java` |
| 购物车 API | `yb-cart/.../controller/CartController.java` |
| Gateway 鉴权 | `yb-gateway/.../filter/AuthGlobalFilter.java` |
| 路由配置 | `yb-gateway/.../application.yml` → `routes` |
| Redis Key 常量 | `yb-common/.../constant/RedisKey.java` |
| 错误码 | `yb-common/.../enums/ErrorCode.java` |
| Feign 接口契约 | `yb-api/.../client/UserClient.java` + `ProductClient.java` + `CartClient.java` |
