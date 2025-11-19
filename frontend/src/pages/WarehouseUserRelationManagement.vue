<template>
  <div class="warehouse-user-relation-management">
    <div class="page-header">
      <h2>用户与仓库关系管理</h2>
    </div>

    <div class="card">
      <div class="search-bar">
        <select v-model="viewType" @change="loadRelations" class="search-type-select">
          <option value="user">按用户查看</option>
          <option value="warehouse">按仓库查看</option>
        </select>
        <input 
          type="text" 
          :placeholder="viewType === 'user' ? '搜索用户名称' : '搜索仓库名称'" 
          v-model="searchName" 
          @keypress.enter="handleSearch" 
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn" @click="handleReset">重置</button>
        <button class="btn" @click="loadRelations">刷新</button>
      </div>

      <transition name="fade">
        <div class="error-message" v-if="error">{{ error }}</div>
      </transition>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span style="margin-left: 10px;">加载中...</span>
      </div>
      <transition name="fade">
        <div v-if="!loading">
          <!-- 按用户查看 -->
          <div v-if="viewType === 'user'">
            <div v-if="userRelations.length === 0" class="empty-message">
              暂无用户数据
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名称</th>
                  <th>邮箱</th>
                  <th>管理的仓库</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <transition-group name="list">
                  <tr v-for="userRelation in userRelations" :key="userRelation.id" class="table-row">
                    <td>{{ userRelation.id }}</td>
                    <td>{{ userRelation.name }}</td>
                    <td>{{ userRelation.email }}</td>
                    <td>
                      <span v-if="userRelation.managedWarehouse && userRelation.managedWarehouse.length > 0">
                        {{ userRelation.managedWarehouse.map(w => w.name).join(', ') }}
                      </span>
                      <span v-else style="color: #999;">无</span>
                    </td>
                    <td>
                      <button class="btn btn-primary" @click="handleManageUserWarehouses(userRelation)">管理仓库</button>
                    </td>
                  </tr>
                </transition-group>
              </tbody>
            </table>
          </div>

          <!-- 按仓库查看 -->
          <div v-else>
            <div v-if="warehouseRelations.length === 0" class="empty-message">
              暂无仓库数据
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>仓库ID</th>
                  <th>仓库名称</th>
                  <th>描述</th>
                  <th>管理的用户</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <transition-group name="list">
                  <tr v-for="warehouseRelation in warehouseRelations" :key="warehouseRelation.id" class="table-row">
                    <td>{{ warehouseRelation.id }}</td>
                    <td>{{ warehouseRelation.name }}</td>
                    <td>{{ warehouseRelation.description }}</td>
                    <td>
                      <span v-if="warehouseRelation.users && warehouseRelation.users.length > 0">
                        {{ warehouseRelation.users.map(u => u.name).join(', ') }}
                      </span>
                      <span v-else style="color: #999;">无</span>
                    </td>
                    <td>
                      <button class="btn btn-primary" @click="handleManageWarehouseUsers(warehouseRelation)">管理用户</button>
                    </td>
                  </tr>
                </transition-group>
              </tbody>
            </table>
          </div>
        </div>
      </transition>
    </div>

    <!-- 管理用户仓库模态框 -->
    <transition name="modal">
      <div v-if="showUserWarehouseModal" class="modal-overlay" @click="showUserWarehouseModal = false">
      <div class="modal-content modal-content-large" @click.stop>
        <h3>管理用户仓库 - {{ managingUserRelation?.name }}</h3>
        <div class="relation-management-section">
          <div class="section-header">
            <h4>当前仓库列表</h4>
            <button class="btn btn-primary" @click="showAddWarehouseModal = true">添加仓库</button>
          </div>
          <div v-if="currentUserWarehouses.length === 0" class="empty-message">暂无仓库</div>
          <table v-else class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>仓库名称</th>
                <th>描述</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="warehouse in currentUserWarehouses" :key="warehouse.id">
                <td>{{ warehouse.id }}</td>
                <td>{{ warehouse.name }}</td>
                <td>{{ warehouse.description }}</td>
                <td>
                  <button class="btn btn-danger btn-sm" @click="handleRemoveUserWarehouse(warehouse.id)">移除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showUserWarehouseModal = false; managingUserRelation = null; currentUserWarehouses = []">关闭</button>
        </div>
      </div>
      </div>
    </transition>

    <!-- 管理仓库用户模态框 -->
    <transition name="modal">
      <div v-if="showWarehouseUserModal" class="modal-overlay" @click="showWarehouseUserModal = false">
      <div class="modal-content modal-content-large" @click.stop>
        <h3>管理仓库用户 - {{ managingWarehouseRelation?.name }}</h3>
        <div class="relation-management-section">
          <div class="section-header">
            <h4>当前用户列表</h4>
            <button class="btn btn-primary" @click="showAddUserModal = true">添加用户</button>
          </div>
          <div v-if="currentWarehouseUsers.length === 0" class="empty-message">暂无用户</div>
          <table v-else class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>邮箱</th>
                <th>电话</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in currentWarehouseUsers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.phone }}</td>
                <td>
                  <button class="btn btn-danger btn-sm" @click="handleRemoveWarehouseUser(user.id)">移除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showWarehouseUserModal = false; managingWarehouseRelation = null; currentWarehouseUsers = []">关闭</button>
        </div>
      </div>
      </div>
    </transition>

    <!-- 添加仓库模态框 -->
    <transition name="modal">
      <div v-if="showAddWarehouseModal" class="modal-overlay" @click="showAddWarehouseModal = false">
      <div class="modal-content" @click.stop>
        <h3>为用户添加仓库</h3>
        <div class="form-group">
          <label>选择仓库</label>
          <select v-model.number="selectedWarehouseId" required>
            <option :value="0">请选择仓库</option>
            <option v-for="warehouse in availableWarehouses" :key="warehouse.id" :value="warehouse.id">
              {{ warehouse.name }} (ID: {{ warehouse.id }})
            </option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleAddUserWarehouse" :disabled="loading || !selectedWarehouseId">添加</button>
          <button class="btn" @click="showAddWarehouseModal = false; selectedWarehouseId = 0">取消</button>
        </div>
      </div>
      </div>
    </transition>

    <!-- 添加用户模态框 -->
    <transition name="modal">
      <div v-if="showAddUserModal" class="modal-overlay" @click="showAddUserModal = false">
      <div class="modal-content" @click.stop>
        <h3>为仓库添加用户</h3>
        <div class="form-group">
          <label>选择用户</label>
          <select v-model.number="selectedUserId" required>
            <option :value="0">请选择用户</option>
            <option v-for="user in availableUsers" :key="user.id" :value="user.id">
              {{ user.name }} ({{ user.email }})
            </option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleAddWarehouseUser" :disabled="loading || !selectedUserId">添加</button>
          <button class="btn" @click="showAddUserModal = false; selectedUserId = 0">取消</button>
        </div>
      </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { warehouseApi } from '@/api/warehouse'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { Warehouse, User, WarehouseHaveUserVO, UserHaveWarehouseVO, PageDTO } from '@/types'

const loading = ref(false)
const error = ref('')
const viewType = ref<'user' | 'warehouse'>('user')
const searchName = ref('')
const userRelations = ref<UserHaveWarehouseVO[]>([])
const warehouseRelations = ref<WarehouseHaveUserVO[]>([])

// 管理用户仓库相关
const showUserWarehouseModal = ref(false)
const managingUserRelation = ref<UserHaveWarehouseVO | null>(null)
const currentUserWarehouses = ref<Warehouse[]>([])
const showAddWarehouseModal = ref(false)
const selectedWarehouseId = ref(0)
const availableWarehouses = ref<Warehouse[]>([])

// 管理仓库用户相关
const showWarehouseUserModal = ref(false)
const managingWarehouseRelation = ref<WarehouseHaveUserVO | null>(null)
const currentWarehouseUsers = ref<User[]>([])
const showAddUserModal = ref(false)
const selectedUserId = ref(0)
const availableUsers = ref<User[]>([])

const authStore = useAuthStore()

onMounted(() => {
  loadRelations()
})

const loadRelations = async () => {
  try {
    loading.value = true
    error.value = ''
    
    if (viewType.value === 'user') {
      // 加载用户与仓库关系
      const response = await warehouseApi.findRelationForUser({
        pageSize: 1000,
        pageNum: 1,
        param: searchName.value ? { name: searchName.value } : {},
      })
      if (response.data) {
        userRelations.value = response.data as UserHaveWarehouseVO[]
      } else {
        userRelations.value = []
      }
    } else {
      // 加载仓库与用户关系
      const response = await warehouseApi.findRelationForWarehouse({
        pageSize: 1000,
        pageNum: 1,
        param: searchName.value ? { name: searchName.value } : {},
      })
      console.log('按仓库查看 - API响应:', response)
      console.log('按仓库查看 - response.data:', response.data)
      console.log('按仓库查看 - response.data 类型:', typeof response.data)
      console.log('按仓库查看 - response.data 是否为数组:', Array.isArray(response.data))
      
      if (response.data) {
        // 确保 response.data 是数组
        const data = Array.isArray(response.data) ? response.data : []
        warehouseRelations.value = data as WarehouseHaveUserVO[]
        console.log('仓库关系数据:', warehouseRelations.value)
        console.log('仓库关系数据长度:', warehouseRelations.value.length)
      } else {
        warehouseRelations.value = []
        console.log('仓库关系数据为空')
      }
    }
  } catch (err: any) {
    error.value = err.message || '加载关系列表失败'
    console.error('加载关系错误:', err)
    // 如果出错，设置为空列表
    if (viewType.value === 'user') {
      userRelations.value = []
    } else {
      warehouseRelations.value = []
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadRelations()
}

const handleReset = () => {
  searchName.value = ''
  loadRelations()
}

// 管理用户仓库
const handleManageUserWarehouses = async (userRelation: UserHaveWarehouseVO) => {
  managingUserRelation.value = userRelation
  showUserWarehouseModal.value = true
  currentUserWarehouses.value = userRelation.managedWarehouse || []
  await loadAvailableWarehousesForUser()
}

const loadAvailableWarehousesForUser = async () => {
  try {
    const response = await warehouseApi.listPage({ pageSize: 1000, pageNum: 1 })
    if (response.data) {
      const existingWarehouseIds = currentUserWarehouses.value.map((w) => w.id)
      availableWarehouses.value = (response.data as Warehouse[]).filter(
        (w) => !existingWarehouseIds.includes(w.id)
      )
    }
  } catch (err: any) {
    console.error('加载仓库列表错误:', err)
  }
}

const handleAddUserWarehouse = async () => {
  if (!selectedWarehouseId.value || !managingUserRelation.value) {
    error.value = '请选择仓库'
    return
  }
  try {
    loading.value = true
    error.value = ''
    const userId = managingUserRelation.value.id
    await warehouseApi.saveRelation({
      warehouseId: selectedWarehouseId.value,
      userId: userId,
    } as any)
    showAddWarehouseModal.value = false
    showUserWarehouseModal.value = false
    selectedWarehouseId.value = 0
    managingUserRelation.value = null
    currentUserWarehouses.value = []
    await loadRelations()
    // 如果被修改的用户是当前登录用户，尝试刷新token
    if (authStore.user && authStore.user.id === userId) {
      refreshCurrentUser().catch(() => {})
    }
    window.dispatchEvent(new CustomEvent('user-warehouses-updated'))
  } catch (err: any) {
    error.value = err.message || '添加仓库失败'
    console.error('添加仓库错误:', err)
  } finally {
    loading.value = false
  }
}

const handleRemoveUserWarehouse = async (warehouseId: number) => {
  if (!managingUserRelation.value) return
  if (!confirm('确定要移除这个仓库吗？')) return
  try {
    loading.value = true
    error.value = ''
    const userId = managingUserRelation.value.id
    await warehouseApi.deleteRelation(userId, warehouseId)
    showUserWarehouseModal.value = false
    managingUserRelation.value = null
    currentUserWarehouses.value = []
    await loadRelations()
    // 如果被修改的用户是当前登录用户，尝试刷新token
    if (authStore.user && authStore.user.id === userId) {
      refreshCurrentUser().catch(() => {})
    }
    window.dispatchEvent(new CustomEvent('user-warehouses-updated'))
  } catch (err: any) {
    error.value = err.message || '移除仓库失败'
    console.error('移除仓库错误:', err)
  } finally {
    loading.value = false
  }
}

// 管理仓库用户
const handleManageWarehouseUsers = async (warehouseRelation: WarehouseHaveUserVO) => {
  managingWarehouseRelation.value = warehouseRelation
  showWarehouseUserModal.value = true
  currentWarehouseUsers.value = warehouseRelation.users || []
  await loadAvailableUsersForWarehouse()
}

const loadAvailableUsersForWarehouse = async () => {
  try {
    const response = await userApi.listPage({ pageSize: 1000, pageNum: 1 })
    if (response.data) {
      const existingUserIds = currentWarehouseUsers.value.map((u) => u.id)
      availableUsers.value = (response.data as User[]).filter(
        (u) => !existingUserIds.includes(u.id)
      )
    }
  } catch (err: any) {
    console.error('加载用户列表错误:', err)
  }
}

const handleAddWarehouseUser = async () => {
  if (!selectedUserId.value || !managingWarehouseRelation.value) {
    error.value = '请选择用户'
    return
  }
  const addedUserId = selectedUserId.value
  try {
    loading.value = true
    error.value = ''
    await warehouseApi.saveRelation({
      warehouseId: managingWarehouseRelation.value.id,
      userId: addedUserId,
    } as any)
    showAddUserModal.value = false
    showWarehouseUserModal.value = false
    selectedUserId.value = 0
    managingWarehouseRelation.value = null
    currentWarehouseUsers.value = []
    await loadRelations()
    // 如果被修改的用户是当前登录用户，尝试刷新token
    if (authStore.user && authStore.user.id === addedUserId) {
      refreshCurrentUser().catch(() => {})
    }
    window.dispatchEvent(new CustomEvent('user-warehouses-updated'))
  } catch (err: any) {
    error.value = err.message || '添加用户失败'
    console.error('添加用户错误:', err)
  } finally {
    loading.value = false
  }
}

const handleRemoveWarehouseUser = async (userId: number) => {
  if (!managingWarehouseRelation.value) return
  if (!confirm('确定要移除这个用户吗？')) return
  try {
    loading.value = true
    error.value = ''
    await warehouseApi.deleteRelation(userId, managingWarehouseRelation.value.id)
    showWarehouseUserModal.value = false
    managingWarehouseRelation.value = null
    currentWarehouseUsers.value = []
    await loadRelations()
    // 如果被修改的用户是当前登录用户，尝试刷新token
    if (authStore.user && authStore.user.id === userId) {
      refreshCurrentUser().catch(() => {})
    }
    window.dispatchEvent(new CustomEvent('user-warehouses-updated'))
  } catch (err: any) {
    error.value = err.message || '移除用户失败'
    console.error('移除用户错误:', err)
  } finally {
    loading.value = false
  }
}

const refreshCurrentUser = async () => {
  if (!authStore.user) return
  try {
    await authStore.refreshToken()
  } catch (err: any) {
    console.error('刷新token失败:', err)
  }
}
</script>

<style scoped>
.warehouse-user-relation-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  max-width: 300px;
}

.search-type-select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.search-type-select:focus {
  outline: none;
  border-color: #1890ff;
}

.modal-content-large {
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.relation-management-section {
  margin: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h4 {
  margin: 0;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #999;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.form-group select:focus {
  outline: none;
  border-color: #1890ff;
}

/* 加载动画容器 */
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 列表项动画 */
.list-enter-active {
  transition: all 0.3s ease;
}

.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.list-move {
  transition: transform 0.3s ease;
}

/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
}

/* 表格行动画 */
.table-row {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

