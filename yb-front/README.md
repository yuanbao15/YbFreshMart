# yb-front — FreshMart 生鲜电商前端

Vue 3 前端测试项目，配套 [yb-cloud-parent](../) 后端微服务。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | ^3.4 | Composition API 响应式框架 |
| Vite | ^5.2 | 开发/构建工具 |
| Vue Router | ^4.3 | 前端路由 + 导航守卫 |
| Pinia | ^2.1 | 状态管理（Auth Store） |
| Axios | ^1.7 | HTTP 请求 + 拦截器 |

## 项目结构

```
yb-front/
├── index.html                  # 入口 HTML
├── package.json                # 依赖配置
├── vite.config.js              # Vite 配置（代理 /api → :8080）
├── README.md                   # 本文档
└── src/
    ├── main.js                 # 应用入口，挂载 Pinia + Router
    ├── App.vue                 # 根组件（Navbar + router-view）
    ├── style.css               # 全局样式（CSS 变量 + 通用组件）
    ├── api/
    │   ├── index.js            # Axios 实例 + 请求/响应拦截器
    │   ├── auth.js             # 认证 API（login/register/refresh/logout/me）
    │   ├── product.js          # 商品 API（类目树/SPU分页/详情/SKU）
    │   ├── cart.js             # 购物车 API（增删改查/清空）
    │   └── admin.js            # 管理后台 API（类目/SPU/SKU CRUD）
    ├── router/
    │   └── index.js            # 路由表 + beforeEach 守卫
    ├── stores/
    │   └── auth.js             # Pinia Auth Store（Token 持久化）
    ├── components/
    │   ├── Navbar.vue          # 顶部导航栏（登录态切换 + 管理下拉菜单）
    │   └── ProductCard.vue     # 商品卡片（SPU 网格项）
    └── views/
        ├── Home.vue            # 首页：类目树 + 商品网格 + 搜索
        ├── Login.vue           # 登录：手机号 + 密码
        ├── Register.vue        # 注册：手机号 + 昵称 + 密码
        ├── ProductDetail.vue   # 商品详情：SPU + SKU 规格 + 加购物车
        ├── Cart.vue            # 购物车：列表/数量/总价/清空
        ├── Profile.vue         # 个人中心：信息 + 接口测试面板
        └── admin/
            ├── CategoryManage.vue  # 类目管理：CRUD + 树形展示
            └── ProductManage.vue   # 商品管理：SPU 列表 + SKU 规格管理
```

## 快速开始

### 前提条件

- Node.js >= 18
- 后端服务已启动（Gateway :8080 + 各微服务注册到 Nacos）

### 安装 & 启动

```bash
# 1. 进入前端目录
cd yb-front

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

浏览器打开 http://localhost:5173

### 构建生产版本

```bash
npm run build    # 输出到 dist/
npm run preview  # 预览构建结果
```

## 页面路由

| 路由 | 页面 | 需要登录 | 说明 |
|------|------|----------|------|
| `/` | 首页 | ❌ | 三级类目树 + 商品网格 + 关键词搜索 + 分页 |
| `/login` | 登录 | ❌ | 手机号+密码登录；已登录用户自动跳首页 |
| `/register` | 注册 | ❌ | 手机号注册，成功后自动登录 |
| `/product/:spuId` | 商品详情 | ❌ | SPU 信息 + SKU 规格 + 加购物车（加购需登录） |
| `/cart` | 购物车 | ✅ | 数量增减/删除/清空/总价 |
| `/profile` | 个人中心 | ✅ | 用户信息展示 + API 测试面板 |
| `/admin/categories` | 类目管理 | ✅ | 类目树 CRUD（增删改查） |
| `/admin/products` | 商品管理 | ✅ | SPU 列表 + SKU 规格管理 |

### 路由守卫规则

- `requireAuth: true` → 未登录跳 `/login?redirect=原路径`
- `guest: true` → 已登录跳 `/`
- 401 响应 → Axios 拦截器自动清登录态并跳 `/login`

## 后端 API 对接

Vite 开发服务器将 `/api` 请求代理到 `http://localhost:8080`（Gateway）。

### API 一览

#### 认证 (yb-auth, 端口 8081)

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 白名单 | 手机号+密码登录 |
| POST | `/api/auth/register` | 白名单 | 手机号注册 |
| POST | `/api/auth/refresh` | 白名单 | 刷新 Token |
| POST | `/api/auth/logout` | 需登录 | 登出（Token 加入黑名单） |
| GET | `/api/auth/me` | 需登录 | 获取当前用户信息 |

#### 商品 (yb-product, 端口 8083)

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/product/category/tree` | 白名单 | 完整类目树 |
| GET | `/api/product/spu/page` | 需登录 | SPU 分页查询 |
| GET | `/api/product/spu/{id}` | 需登录 | SPU 详情 |
| GET | `/api/product/sku/list` | 需登录 | SPU 下的 SKU 列表 |
| GET | `/api/product/{skuId}` | 需登录 | SKU 详情 |
| GET | `/api/product/page` | 白名单 | SKU 分页 |

#### 购物车 (yb-cart, 端口 8085)

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/cart/{userId}/detail` | 需登录 | 购物车列表（含商品详情） |
| POST | `/api/cart/{userId}/items` | 需登录 | 添加商品到购物车 |
| PUT | `/api/cart/{userId}/items/{skuId}` | 需登录 | 修改商品数量 |
| DELETE | `/api/cart/{userId}/items/{skuId}` | 需登录 | 删除购物车商品 |
| DELETE | `/api/cart/{userId}/clear` | 需登录 | 清空购物车 |

### 请求格式

所有请求通过 Axios 拦截器自动处理：

- **请求头**：`Authorization: Bearer {token}`（登录后自动附带）
- **响应格式**：`{ code: 200, message: "ok", data: {...}, timestamp: ... }`
- **价格字段**：后端存储为分（整型），前端展示除以 100 转为元
- **分页参数**：`page`(当前页)、`size`(每页条数)
- **分页响应**：`{ records: [...], total: N, current/page: N, size: N, pages: N }`

## 状态管理

### Auth Store (Pinia)

```js
// 状态
token, userId, phone, nickname, role   // 全部持久化到 localStorage

// 计算属性
isLoggedIn                             // 是否已登录

// 方法
login(phone, password)                 // 登录 → 保存 Token
register(phone, password, nickname)    // 注册 → 自动登录
logout()                               // 登出 → 清空 Token
refresh()                              // 刷新 Token
fetchCurrentUser()                     // 获取 /api/auth/me
```

## 样式主题

```css
--primary: #07c160       /* FreshMart 绿（主色调） */
--danger:  #ee0a24       /* 价格/删除/错误 */
--warning: #ff976a       /* 警告 */
--text:    #323233       /* 主文字 */
--text-light: #969799    /* 辅助文字 */
--bg:      #f7f8fa       /* 页面背景 */
```

## 后续计划

- [x] 阶段二：认证 + 商品 + 购物车 + 管理后台
- [x] 种子数据（34 类目 / 16 SPU / 25 SKU）
- [ ] 阶段三：搜索页 + 日志查看
- [ ] 阶段四：订单管理 + 库存显示
- [ ] 阶段五：支付流程 + 消息通知
- [ ] 响应式优化（平板/手机适配）
- [ ] 图片上传（商品图片管理）
- [ ] 地址管理页面

---

> 最后更新：2026-06-15 — 阶段二完成：种子数据 + 管理后台
