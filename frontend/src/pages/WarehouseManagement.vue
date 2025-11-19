<template>
  <div class="warehouse-management">
    <div class="page-header">
      <h2>仓库管理</h2>
      <button class="btn btn-primary" @click="showModal = true">新增仓库</button>
    </div>

    <div class="card">
      <div class="search-bar">
        <select v-model="searchType" class="search-type-select">
          <option value="name">按名称</option>
          <option value="id">按ID</option>
        </select>
        <input 
          type="text" 
          :placeholder="searchType === 'id' ? '输入仓库ID' : '搜索仓库名称'" 
          v-model="searchName" 
          @keypress.enter="handleSearch" 
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn" @click="handleReset">重置</button>
        <button class="btn" @click="loadWarehouses">刷新</button>
        <button class="btn" @click="() => { usePagination = !usePagination; currentPage = 1; loadWarehouses() }">
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
        <table v-if="!loading" class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>仓库名称</th>
            <th>描述</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <transition-group name="list">
            <tr v-for="warehouse in warehouses" :key="warehouse.id" class="table-row">
              <td>{{ warehouse.id }}</td>
              <td>{{ warehouse.name }}</td>
              <td>{{ warehouse.description }}</td>
              <td>
                <button class="btn btn-primary" @click="handleEdit(warehouse)">编辑</button>
                <button class="btn btn-danger" @click="handleDelete(warehouse.id)" style="margin-left: 10px">删除</button>
              </td>
            </tr>
          </transition-group>
        </tbody>
      </table>
      </transition>
      
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
            <span class="page-size-select">
              <label>每页显示：</label>
              <select v-model.number="pageSize" @change="handlePageSizeChange">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
              </select>
            </span>
          </div>
        </div>
      </transition>
    </div>

    <transition name="modal">
      <div v-if="showModal" class="modal-overlay" @click="showModal = false">
      <div class="modal-content" @click.stop>
        <h3>{{ editingWarehouse ? '编辑仓库' : '新增仓库' }}</h3>
        <div class="form-group">
          <label>仓库名称</label>
          <input type="text" v-model="formData.name" required />
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="formData.description" required rows="4"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleSave" :disabled="loading">保存</button>
          <button class="btn" @click="showModal = false; editingWarehouse = null; resetForm()">取消</button>
        </div>
      </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { warehouseApi } from '@/api/warehouse'
import { Warehouse, WarehouseDTO } from '@/types'

const warehouses = ref<Warehouse[]>([])
const loading = ref(false)
const error = ref('')
const showModal = ref(false)
const editingWarehouse = ref<Warehouse | null>(null)
const searchName = ref('')
const searchType = ref<'name' | 'id'>('name')
const usePagination = ref(true) // 默认使用分页
const pageInfo = ref({
  current: 1,
  pages: 1,
  total: 0,
  size: 20,
})
const pageSize = ref(20)
const currentPage = ref(1)

const formData = ref<WarehouseDTO>({
  name: '',
  description: '',
})

onMounted(() => {
  loadWarehouses()
})

const loadWarehouses = async () => {
  try {
    loading.value = true
    error.value = ''
    
    if (usePagination.value) {
      // 使用分页查询
      const response = await warehouseApi.listPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        param: searchName.value && searchType.value === 'name' ? { name: searchName.value } : {},
      })
      if (response.data) {
        warehouses.value = response.data as Warehouse[]
        // 后端返回的是数组，无法获取真正的总数
        // 如果返回的数据量等于pageSize，说明可能还有下一页
        // 如果返回的数据量小于pageSize，说明这是最后一页
        const hasMore = warehouses.value.length === pageSize.value
        const estimatedTotal = hasMore 
          ? (currentPage.value * pageSize.value) + 1 // 至少还有一页
          : (currentPage.value - 1) * pageSize.value + warehouses.value.length
        
        pageInfo.value = {
          current: currentPage.value,
          pages: hasMore ? currentPage.value + 1 : currentPage.value, // 至少显示当前页+1，或者就是当前页
          total: estimatedTotal,
          size: pageSize.value,
        }
      } else {
        warehouses.value = []
        pageInfo.value = { current: 1, pages: 1, total: 0, size: pageSize.value }
      }
    } else {
      // 使用全量查询（不使用分页）
      const response = await warehouseApi.listPage({ 
        pageSize: 1000, 
        pageNum: 1,
        param: searchName.value && searchType.value === 'name' ? { name: searchName.value } : {},
      })
      if (response.data) {
        warehouses.value = response.data as Warehouse[]
        pageInfo.value = {
          current: 1,
          pages: 1,
          total: warehouses.value.length,
          size: warehouses.value.length,
        }
      }
    }
  } catch (err: any) {
    error.value = err.message || '加载仓库列表失败'
    console.error('加载仓库错误:', err)
    warehouses.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchName.value.trim()) {
    currentPage.value = 1
    loadWarehouses()
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
        error.value = '请输入有效的仓库ID'
        return
      }
      const response = await warehouseApi.findById(id)
      if (response.data) {
        warehouses.value = [response.data]
        pageInfo.value = { current: 1, pages: 1, total: 1, size: 1 }
      } else {
        warehouses.value = []
        error.value = '未找到该仓库'
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    } else {
      // 按名称搜索，使用分页
      usePagination.value = true
      currentPage.value = 1
      const response = await warehouseApi.listPage({
        pageNum: 1,
        pageSize: pageSize.value,
        param: { name: searchName.value },
      })
      if (response.data) {
        warehouses.value = response.data as Warehouse[]
        // 后端返回的是数组，无法获取真正的总数
        // 如果返回的数据量等于pageSize，说明可能还有下一页
        const hasMore = warehouses.value.length === pageSize.value
        const estimatedTotal = hasMore 
          ? pageSize.value + 1 // 至少还有一页
          : warehouses.value.length
        
        pageInfo.value = {
          current: 1,
          pages: hasMore ? 2 : 1, // 至少显示2页，或者就是1页
          total: estimatedTotal,
          size: pageSize.value,
        }
      } else {
        warehouses.value = []
        pageInfo.value = { current: 1, pages: 1, total: 0, size: pageSize.value }
      }
    }
  } catch (err: any) {
    error.value = err.message || '搜索失败'
    console.error('搜索错误:', err)
    warehouses.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchName.value = ''
  searchType.value = 'name'
  currentPage.value = 1
  usePagination.value = true
  loadWarehouses()
}

const handlePageChange = (page: number) => {
  if (page < 1 || page > pageInfo.value.pages) return
  currentPage.value = page
  loadWarehouses()
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  pageInfo.value.size = pageSize.value
  loadWarehouses()
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
    if (editingWarehouse.value?.id) {
      await warehouseApi.mod({ ...formData.value, id: editingWarehouse.value.id })
    } else {
      await warehouseApi.save(formData.value)
    }
    showModal.value = false
    editingWarehouse.value = null
    resetForm()
    loadWarehouses()
  } catch (err: any) {
    error.value = err.message || '保存失败'
    console.error('保存错误:', err)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这个仓库吗？')) return
  try {
    loading.value = true
    error.value = ''
    await warehouseApi.delete(id)
    loadWarehouses()
  } catch (err: any) {
    error.value = err.message || '删除失败'
    console.error('删除错误:', err)
  } finally {
    loading.value = false
  }
}

const handleEdit = (warehouse: Warehouse) => {
  editingWarehouse.value = warehouse
  formData.value = {
    id: warehouse.id,
    name: warehouse.name,
    description: warehouse.description,
  }
  showModal.value = true
}

const resetForm = () => {
  formData.value = {
    name: '',
    description: '',
  }
}

</script>

<style scoped>
.warehouse-management {
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

/* 分页样式 */
.pagination {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.page-numbers {
  display: flex;
  gap: 5px;
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 10px;
}

.page-size-select label {
  font-size: 14px;
  color: #666;
}

.page-size-select select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.page-size-select select:focus {
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
