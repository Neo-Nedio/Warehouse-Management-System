import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Login from '@/pages/Login.vue'
import Dashboard from '@/pages/Dashboard.vue'
import UserManagement from '@/pages/UserManagement.vue'
import GoodsManagement from '@/pages/GoodsManagement.vue'
import WarehouseManagement from '@/pages/WarehouseManagement.vue'
import WarehouseUserRelationManagement from '@/pages/WarehouseUserRelationManagement.vue'
import LogManagement from '@/pages/LogManagement.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/',
      name: 'Dashboard',
      component: Dashboard,
      meta: { requiresAuth: true },
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: UserManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/goods',
      name: 'GoodsManagement',
      component: GoodsManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/warehouse',
      name: 'WarehouseManagement',
      component: WarehouseManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/warehouse-user-relation',
      name: 'WarehouseUserRelationManagement',
      component: WarehouseUserRelationManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/logs',
      name: 'LogManagement',
      component: LogManagement,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
