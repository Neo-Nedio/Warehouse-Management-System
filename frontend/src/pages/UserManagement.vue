<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <button class="btn btn-primary" @click="showModal = true">新增用户</button>
    </div>

    <div class="card">
      <div class="search-bar">
        <select v-model="searchType" class="search-type-select">
          <option value="name">按名称</option>
          <option value="id">按ID</option>
        </select>
        <input 
          type="text" 
          :placeholder="searchType === 'id' ? '输入用户ID' : '搜索用户名称'" 
          v-model="searchName" 
          @keypress.enter="handleSearch" 
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn" @click="handleReset">刷新</button>
        <button class="btn" @click="() => { usePagination = !usePagination; currentPage = 1; loadUsers() }">
          {{ usePagination ? '切换为全量' : '切换为分页' }}
        </button>
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
          <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>电话</th>
            <th>性别</th>
            <th>年龄</th>
            <th>角色</th>
            <th>管理仓库</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <transition-group name="list">
            <tr v-for="user in users" :key="user.id" class="table-row">
              <td>{{ user.id }}</td>
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.phone }}</td>
              <td>{{ user.sex === 0 ? '女' : '男' }}</td>
              <td>{{ user.age }}</td>
              <td>{{ getRoleName(user.roleId) }}</td>
              <td>
                <span v-if="user.managedWarehouseIds && user.managedWarehouseIds.length > 0">
                  {{ user.managedWarehouseIds.join(', ') }}
                </span>
                <span v-else style="color: #999;">无</span>
              </td>
              <td>
                <button class="btn btn-primary" @click="handleEdit(user)">编辑</button>
                <button class="btn btn-danger" @click="handleDelete(user.id)" style="margin-left: 10px">删除</button>
              </td>
            </tr>
          </transition-group>
        </tbody>
      </table>
      
      <!-- 分页组件 -->
      <transition name="fade">
        <div class="pagination" v-if="usePagination && pageInfo.total > 0">
        <div class="pagination-info">
          共 {{ pageInfo.total }} 条，第 {{ pageInfo.current }} / {{ pageInfo.pages }} 页
        </div>
        <div class="pagination-controls">
          <button 
            class="btn" 
            @click="handlePageChange(pageInfo.current - 1)" 
            :disabled="pageInfo.current <= 1"
          >
            上一页
          </button>
          <span class="page-numbers">
            <button
              v-for="page in visiblePages"
              :key="page"
              class="btn"
              :class="{ 'btn-primary': page === pageInfo.current }"
              @click="handlePageChange(page)"
              :disabled="page === pageInfo.current"
            >
              {{ page }}
            </button>
          </span>
          <button 
            class="btn" 
            @click="handlePageChange(pageInfo.current + 1)" 
            :disabled="pageInfo.current >= pageInfo.pages"
          >
            下一页
          </button>
          <select v-model.number="pageSize" @change="handlePageSizeChange" class="page-size-select">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
            <option :value="100">100条/页</option>
          </select>
        </div>
      </div>
      </transition>
        </div>
      </transition>
    </div>

    <transition name="modal">
      <div v-if="showModal" class="modal-overlay" @click="showModal = false">
      <div class="modal-content" @click.stop>
        <h3>{{ editingUser ? '编辑用户' : '新增用户' }}</h3>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" v-model="formData.name" required />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="formData.email" required />
        </div>
        <div class="form-group">
          <label>电话</label>
          <input type="text" v-model="formData.phone" required />
        </div>
        <div class="form-group" v-if="!editingUser">
          <label>密码</label>
          <input type="password" v-model="formData.password" required />
        </div>
        <div class="form-group">
          <label>性别</label>
          <select v-model.number="formData.sex">
            <option :value="0">女</option>
            <option :value="1">男</option>
          </select>
        </div>
        <div class="form-group">
          <label>年龄</label>
          <input type="number" v-model.number="formData.age" required />
        </div>
        <div class="form-group">
          <label>角色</label>
          <select v-model.number="formData.roleId">
            <option :value="1">财务</option>
            <option :value="2">仓库管理员</option>
            <option :value="3">系统管理员</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleSave" :disabled="loading">保存</button>
          <button class="btn" @click="showModal = false; editingUser = null; resetForm()">取消</button>
        </div>
      </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { User, UserDTO, PageDTO, PageResult } from '@/types'

const users = ref<User[]>([])
const loading = ref(false)
const error = ref('')
const showModal = ref(false)
const editingUser = ref<UserDTO | null>(null)
const searchName = ref('')
const searchType = ref<'name' | 'id'>('name')
const authStore = useAuthStore()

// 分页相关
const usePagination = ref(false) // 是否使用分页模式
const pageInfo = ref({
  current: 1,
  pages: 1,
  total: 0,
  size: 20,
})
const pageSize = ref(20)
const currentPage = ref(1)

const formData = ref<UserDTO>({
  name: '',
  email: '',
  phone: '',
  password: '',
  sex: 0,
  age: 18,
  roleId: 1,
})

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  try {
    loading.value = true
    error.value = ''
    
    if (usePagination.value) {
      // 使用分页查询
      const response = await userApi.listPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        param: {},
      })
      if (response.data) {
        users.value = response.data
        // 后端只返回列表，没有分页信息，这里估算分页信息
        // 如果返回的数据少于pageSize，说明是最后一页
        if (response.data.length < pageSize.value) {
          pageInfo.value = {
            current: currentPage.value,
            pages: currentPage.value,
            total: (currentPage.value - 1) * pageSize.value + response.data.length,
            size: pageSize.value,
          }
        } else {
          // 无法确定总页数，假设还有更多页
          pageInfo.value = {
            current: currentPage.value,
            pages: currentPage.value + 1, // 假设至少还有一页
            total: currentPage.value * pageSize.value,
            size: pageSize.value,
          }
        }
      }
    } else {
      // 使用全量查询
      const response = await userApi.listPage({ pageSize: 100, pageNum: 1 })
      if (response.data) {
        users.value = response.data
        pageInfo.value = {
          current: 1,
          pages: 1,
          total: response.data.length,
          size: response.data.length,
        }
      }
    }
  } catch (err: any) {
    error.value = err.message || '加载用户列表失败'
    console.error('加载用户错误:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchName.value.trim()) {
    currentPage.value = 1
    loadUsers()
    return
  }
  try {
    loading.value = true
    error.value = ''
    
    if (searchType.value === 'id') {
      // 按ID搜索，不使用分页
      usePagination.value = false
      const id = parseInt(searchName.value)
      if (isNaN(id)) {
        error.value = '请输入有效的用户ID'
        return
      }
      const response = await userApi.findById(id)
      if (response.data) {
        users.value = [response.data]
        pageInfo.value = { current: 1, pages: 1, total: 1, size: 1 }
      } else {
        users.value = []
        error.value = '未找到该用户'
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    } else {
      // 按名称搜索，使用分页
      usePagination.value = true
      currentPage.value = 1
      const response = await userApi.listPage({
        pageNum: 1,
        pageSize: pageSize.value,
        param: { name: searchName.value },
      })
      if (response.data) {
        users.value = response.data
        // 估算分页信息
        if (response.data.length < pageSize.value) {
          pageInfo.value = {
            current: 1,
            pages: 1,
            total: response.data.length,
            size: pageSize.value,
          }
        } else {
          pageInfo.value = {
            current: 1,
            pages: 2, // 假设至少还有一页
            total: pageSize.value,
            size: pageSize.value,
          }
        }
      } else {
        users.value = []
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    }
  } catch (err: any) {
    error.value = err.message || '搜索失败'
    console.error('搜索错误:', err)
    users.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchName.value = ''
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  if (page < 1) return
  currentPage.value = page
  if (searchName.value && searchType.value === 'name') {
    // 如果有搜索条件，使用搜索
    handleSearch()
  } else {
    loadUsers()
  }
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  pageInfo.value.size = pageSize.value
  if (searchName.value && searchType.value === 'name') {
    handleSearch()
  } else {
    loadUsers()
  }
}

// 计算可见的页码
const visiblePages = computed(() => {
  const pages: number[] = []
  const total = pageInfo.value.pages
  const current = pageInfo.value.current
  const maxVisible = 5 // 最多显示5个页码
  
  if (total <= maxVisible) {
    // 如果总页数少于等于最大可见数，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 否则显示当前页附近的页码
    let start = Math.max(1, current - Math.floor(maxVisible / 2))
    let end = Math.min(total, start + maxVisible - 1)
    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1)
    }
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }
  return pages
})

const handleSave = async () => {
  try {
    loading.value = true
    error.value = ''
    const isCurrentUser = authStore.user && editingUser.value?.id === authStore.user.id
    if (editingUser.value?.id) {
      // 编辑用户时，如果密码为空则不传password字段，避免验证失败
      // 其他字段（包括roleId）都正常传递，因为后端验证注解已支持roleId 1-3
      const { password, ...editData } = formData.value
      const modData = password ? { ...editData, password, id: editingUser.value.id } : { ...editData, id: editingUser.value.id }
      await userApi.mod(modData)
    } else {
      await userApi.save(formData.value)
    }
    showModal.value = false
    editingUser.value = null
    resetForm()
    loadUsers()
    // 如果修改的是当前登录用户，尝试刷新token（失败不影响，用户可手动刷新页面）
    if (isCurrentUser) {
      authStore.refreshToken().catch((err: any) => {
        console.error('刷新token失败:', err)
        // 静默失败，不影响操作，用户可手动刷新页面获取最新数据
      })
    }
  } catch (err: any) {
    error.value = err.message || '保存失败'
    console.error('保存错误:', err)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这个用户吗？')) return
  try {
    loading.value = true
    error.value = ''
    await userApi.delete(id)
    loadUsers()
  } catch (err: any) {
    error.value = err.message || '删除失败'
    console.error('删除错误:', err)
  } finally {
    loading.value = false
  }
}

const handleEdit = (user: User) => {
  editingUser.value = {
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone,
    sex: user.sex,
    age: user.age,
    roleId: user.roleId,
  }
  formData.value = {
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone,
    password: '',
    sex: user.sex,
    age: user.age,
    roleId: user.roleId,
  }
  showModal.value = true
}

const getRoleName = (roleId: number): string => {
  switch (roleId) {
    case 1:
      return '财务'
    case 2:
      return '仓库管理员'
    case 3:
      return '系统管理员'
    default:
      return '未知'
  }
}

const resetForm = () => {
  formData.value = {
    name: '',
    email: '',
    phone: '',
    password: '',
    sex: 0,
    age: 18,
    roleId: 1,
  }
}

</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
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
  background: white;
  cursor: pointer;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-numbers {
  display: flex;
  gap: 5px;
}

.page-numbers .btn {
  min-width: 36px;
  padding: 6px 12px;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
}

.page-size-select:focus {
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

.table-row:nth-child(even) {
  animation-delay: 0.05s;
}

.table-row:nth-child(odd) {
  animation-delay: 0.1s;
}

</style>
