import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: 'FreshMart - 首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/product/:spuId',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue'),
    meta: { title: '购物车', requireAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requireAuth: true }
  },
  {
    path: '/admin/categories',
    name: 'CategoryManage',
    component: () => import('../views/admin/CategoryManage.vue'),
    meta: { title: '类目管理', requireAuth: true }
  },
  {
    path: '/admin/products',
    name: 'ProductManage',
    component: () => import('../views/admin/ProductManage.vue'),
    meta: { title: '商品管理', requireAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'FreshMart'

  const token = localStorage.getItem('token')

  // 需要登录的页面
  if (to.meta.requireAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 已登录用户不能访问登录/注册页
  if (to.meta.guest && token) {
    return next({ name: 'Home' })
  }

  next()
})

export default router
