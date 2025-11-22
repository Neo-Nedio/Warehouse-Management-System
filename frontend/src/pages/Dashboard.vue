<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <h1>仓库管理系统</h1>
      <div class="user-info">
        <span>欢迎, {{ authStore.user?.name }}</span>
        <button @click="handleLogout" class="btn btn-danger">退出登录</button>
      </div>
    </header>

    <div class="dashboard-content">
      <div class="menu-grid">
        <transition-group name="card" tag="div" class="menu-grid">
          <router-link to="/users" class="menu-card" key="users">
          <h3>用户管理</h3>
          <p>管理系统用户信息</p>
        </router-link>

          <router-link to="/goods" class="menu-card" key="goods">
          <h3>商品管理</h3>
          <p>管理商品信息和库存</p>
        </router-link>

          <router-link to="/warehouse" class="menu-card" key="warehouse">
          <h3>仓库管理</h3>
            <p>管理仓库信息</p>
          </router-link>

          <router-link to="/warehouse-user-relation" class="menu-card" key="warehouse-user-relation">
            <h3>用户与仓库关系</h3>
            <p>管理用户与仓库的关联关系</p>
        </router-link>

          <router-link to="/logs" class="menu-card" key="logs">
          <h3>操作日志</h3>
          <p>查看系统操作记录</p>
        </router-link>
        </transition-group>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
}

.dashboard-header {
  background: white;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-header h1 {
  margin: 0;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.dashboard-content {
  padding: 40px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.menu-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.menu-card::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(24, 144, 255, 0.1);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
  pointer-events: none;
}

.menu-card:active::before {
  width: 400px;
  height: 400px;
}

.menu-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.menu-card:active {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  opacity: 0.95;
}

.menu-card h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.menu-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 卡片进入动画 */
.card-enter-active {
  transition: all 0.4s ease;
}

.card-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.card-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.card-enter-active:nth-child(1) {
  transition-delay: 0.1s;
}

.card-enter-active:nth-child(2) {
  transition-delay: 0.2s;
}

.card-enter-active:nth-child(3) {
  transition-delay: 0.3s;
}

.card-enter-active:nth-child(4) {
  transition-delay: 0.4s;
}

.card-enter-active:nth-child(5) {
  transition-delay: 0.5s;
}
</style>
