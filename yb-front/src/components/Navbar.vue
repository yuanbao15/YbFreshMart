<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="logo">🥬 FreshMart</router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/search" class="nav-link">🔍 搜索</router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/cart" class="nav-link">🛒 购物车</router-link>
          <span class="nav-dropdown">
            <span class="nav-link" style="cursor:default">⚙️ 管理</span>
            <span class="dropdown-menu">
              <router-link to="/admin/categories">📁 类目管理</router-link>
              <router-link to="/admin/products">📦 商品管理</router-link>
            </span>
          </span>
          <router-link to="/profile" class="nav-link">👤 {{ auth.nickname || auth.phone || '我的' }}</router-link>
          <a href="#" class="nav-link" @click.prevent="handleLogout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '../stores/auth.js'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await auth.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  z-index: 1000;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  text-decoration: none;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 16px;
}
.nav-link {
  color: var(--text);
  text-decoration: none;
  font-size: 15px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.2s;
}
.nav-link:hover {
  color: var(--primary);
}

/* 管理下拉菜单 */
.nav-dropdown {
  position: relative;
}
.nav-dropdown .dropdown-menu {
  display: none;
  position: absolute;
  top: 100%;
  right: 0;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  min-width: 140px;
  padding: 4px 0;
  z-index: 1001;
}
.nav-dropdown:hover .dropdown-menu {
  display: block;
}
.nav-dropdown .dropdown-menu a {
  display: block;
  padding: 8px 16px;
  font-size: 14px;
  color: var(--text);
  text-decoration: none;
  white-space: nowrap;
}
.nav-dropdown .dropdown-menu a:hover {
  background: #f0fff4;
  color: var(--primary);
}
</style>
