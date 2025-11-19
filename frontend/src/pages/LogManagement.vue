<template>
  <div class="log-management">
    <div class="page-header">
      <h2>操作日志</h2>
      <button class="btn btn-primary" @click="loadLogs">刷新</button>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item">
          <label>操作类型：</label>
          <select v-model.number="filterType">
            <option :value="''">全部</option>
            <option :value="1">新增</option>
            <option :value="2">修改信息</option>
            <option :value="3">修改仓库</option>
            <option :value="4">删除</option>
          </select>
          <button class="btn btn-primary" @click="handleFilterByType">按类型筛选</button>
        </div>
        <div class="filter-item">
          <label>仓库ID：</label>
          <input type="number" v-model="warehouseId" placeholder="输入仓库ID" />
          <button class="btn btn-primary" @click="handleFilterByWarehouse">按仓库筛选</button>
        </div>
        <div class="filter-item">
          <label>用户名：</label>
          <input type="text" v-model="userName" placeholder="输入用户名" />
          <button class="btn btn-primary" @click="handleFilterByUserName">按用户筛选</button>
        </div>
        <div class="filter-item">
          <label>商品ID：</label>
          <input type="number" v-model.number="goodsId" placeholder="输入商品ID" />
          <button type="button" class="btn btn-primary" @click="handleFilterByGoodsId">按商品ID筛选</button>
        </div>
        <div class="filter-item">
          <label>商品名称：</label>
          <input type="text" v-model="goodsName" placeholder="输入商品名称" />
          <button class="btn btn-primary" @click="handleFilterByGoodsName">按商品名筛选</button>
        </div>
        <div class="filter-item">
          <label>组合查询：</label>
          <button class="btn btn-primary" @click="handleFilterByTypeAndWarehouse">类型+仓库</button>
        </div>
        <div class="filter-item">
          <label>时间范围：</label>
          <input type="datetime-local" v-model="startTime" placeholder="开始时间" />
          <input type="datetime-local" v-model="endTime" placeholder="结束时间" />
          <button type="button" class="btn btn-primary" @click="handleFilterByTime">按时间筛选</button>
        </div>
        <div class="filter-item">
          <button type="button" class="btn btn-primary" @click="showAnyConditionModal = true">任意条件查询</button>
        </div>
        <div class="filter-item">
          <button class="btn" @click="handleReset">重置</button>
          <button class="btn" @click="() => { usePagination = !usePagination; currentPage = 1; loadLogs() }">
            {{ usePagination ? '切换为全量' : '切换为分页' }}
          </button>
        </div>
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
          <div v-if="logs.length === 0" class="empty-message">
            暂无日志数据
          </div>
          <table v-else class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>操作类型</th>
                <th>商品ID</th>
                <th>商品名称</th>
                <th>原仓库ID</th>
                <th>新仓库ID</th>
                <th>操作人</th>
                <th>操作时间</th>
              </tr>
            </thead>
            <tbody>
              <transition-group name="list">
                <tr v-for="log in logs" :key="log.id" class="table-row">
                  <td>{{ log.id }}</td>
                  <td>{{ getOperateTypeName(log.operateType) }}</td>
                  <td>{{ log.goodsId }}</td>
                  <td>{{ log.goodsName }}</td>
                  <td>{{ log.formerWarehouseId || '-' }}</td>
                  <td>{{ log.newWarehouseId || '-' }}</td>
                  <td>{{ log.updateUser }}</td>
                  <td>{{ new Date(log.updateTime).toLocaleString() }}</td>
                </tr>
              </transition-group>
            </tbody>
          </table>
        </div>
      </transition>
      
      <!-- 任意条件查询模态框 -->
      <transition name="modal">
        <div v-if="showAnyConditionModal" class="modal-overlay" @click="showAnyConditionModal = false">
          <div class="modal-content modal-content-large" @click.stop>
            <h3>任意条件查询</h3>
            <div class="form-group">
              <label>操作类型：</label>
              <select v-model.number="anyCondition.operateType">
                <option :value="''">不限制</option>
                <option :value="1">新增</option>
                <option :value="2">修改信息</option>
                <option :value="3">修改仓库</option>
                <option :value="4">删除</option>
              </select>
            </div>
            <div class="form-group">
              <label>商品ID：</label>
              <input type="number" v-model.number="anyCondition.goodsId" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>商品名称：</label>
              <input type="text" v-model="anyCondition.goodsName" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>原仓库ID：</label>
              <input type="number" v-model.number="anyCondition.formerWarehouseId" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>新仓库ID：</label>
              <input type="number" v-model.number="anyCondition.newWarehouseId" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>操作人：</label>
              <input type="text" v-model="anyCondition.updateUser" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>开始时间：</label>
              <input type="datetime-local" v-model="anyCondition.startTime" placeholder="不限制" />
            </div>
            <div class="form-group">
              <label>结束时间：</label>
              <input type="datetime-local" v-model="anyCondition.endTime" placeholder="不限制" />
            </div>
            <div class="modal-actions">
              <button type="button" class="btn btn-primary" @click="handleFilterByAnyCondition" :disabled="loading">查询</button>
              <button type="button" class="btn" @click="resetAnyCondition">重置</button>
              <button type="button" class="btn" @click="showAnyConditionModal = false">取消</button>
            </div>
          </div>
        </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { logApi } from '@/api/log'
import { OperationLog, PageDTO, UserHavaOperationVO } from '@/types'

const logs = ref<OperationLog[]>([])
const loading = ref(false)
const error = ref('')
const filterType = ref<number | ''>('')
const warehouseId = ref('')
const userName = ref('')
const goodsId = ref<number | string>('')
const goodsName = ref('')
const startTime = ref('')
const endTime = ref('')
const searchByUser = ref(false) // 标记是否按用户搜索
const searchByGoods = ref(false) // 标记是否按商品搜索
const showAnyConditionModal = ref(false)
const anyCondition = ref<OperationLogDTO>({
  operateType: '',
  goodsId: undefined,
  goodsName: '',
  formerWarehouseId: undefined,
  newWarehouseId: undefined,
  updateUser: '',
  startTime: '',
  endTime: '',
})

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

onMounted(() => {
  loadLogs()
})

const loadLogs = async () => {
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    console.log('开始加载所有日志...')
    const response = await logApi.findAll()
    console.log('加载日志响应:', response)
    if (response.data) {
      logs.value = response.data
      console.log('加载到的日志数量:', logs.value.length)
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    } else {
      console.log('响应中没有数据')
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '加载日志列表失败'
    console.error('加载日志错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleFilterByType = async () => {
  if (!filterType.value || filterType.value < 1 || filterType.value > 4) {
    loadLogs()
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    currentPage.value = 1
    
    if (usePagination.value) {
      // 使用分页查询
      const response = await logApi.findAllByTypeAndPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        param: { type: filterType.value },
      })
      if (response.data) {
        logs.value = response.data as OperationLog[]
        // 后端返回的是数组，估算分页信息
        const hasMore = logs.value.length === pageSize.value
        const estimatedTotal = hasMore 
          ? (currentPage.value * pageSize.value) + 1
          : (currentPage.value - 1) * pageSize.value + logs.value.length
        
        pageInfo.value = {
          current: currentPage.value,
          pages: hasMore ? currentPage.value + 1 : currentPage.value,
          total: estimatedTotal,
          size: pageSize.value,
        }
      }
    } else {
      // 使用全量查询
      const response = await logApi.findAllByType(filterType.value)
      if (response.data) {
        logs.value = response.data
        pageInfo.value = {
          current: 1,
          pages: 1,
          total: logs.value.length,
          size: logs.value.length,
        }
      }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
  } finally {
    loading.value = false
  }
}

const handleFilterByWarehouse = async () => {
  if (!warehouseId.value) {
    loadLogs()
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    const response = await logApi.findByWarehouseId(parseInt(warehouseId.value))
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
  } finally {
    loading.value = false
  }
}

const handleFilterByTypeAndWarehouse = async () => {
  if (!filterType.value || filterType.value < 1 || filterType.value > 4 || !warehouseId.value) {
    error.value = '请选择操作类型并输入仓库ID'
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    const response = await logApi.findAllByTypeAndWarehouseId(
      filterType.value,
      parseInt(warehouseId.value)
    )
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
  } finally {
    loading.value = false
  }
}

const handleFilterByUserName = async () => {
  if (!userName.value || !userName.value.trim()) {
    error.value = '请输入用户名'
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = true
    searchByGoods.value = false
    const response = await logApi.findByUpdateName(userName.value.trim())
    if (response.data) {
      // 将 UserHavaOperationVO[] 转换为 OperationLog[]
      const userOperations = response.data as UserHavaOperationVO[]
      const allLogs: OperationLog[] = []
      userOperations.forEach((userOp) => {
        if (userOp.operationLogs && userOp.operationLogs.length > 0) {
          allLogs.push(...userOp.operationLogs)
        }
      })
      logs.value = allLogs
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    } else {
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleFilterByGoodsId = async () => {
  // 处理数字或字符串类型的输入
  const goodsIdStr = String(goodsId.value || '').trim()
  if (!goodsIdStr) {
    error.value = '请输入商品ID'
    return
  }
  const id = parseInt(goodsIdStr)
  if (isNaN(id) || id <= 0) {
    error.value = '请输入有效的商品ID'
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = true
    console.log('查询商品ID:', id)
    const response = await logApi.findByGoodsId(id)
    console.log('查询结果:', response)
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    } else {
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleFilterByGoodsName = async () => {
  if (!goodsName.value || !goodsName.value.trim()) {
    error.value = '请输入商品名称'
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = true
    const response = await logApi.findByGoodsName(goodsName.value.trim())
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    } else {
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleFilterByTime = async () => {
  if (!startTime.value || !endTime.value) {
    error.value = '请选择开始时间和结束时间'
    return
  }
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    // 将本地时间格式转换为后端需要的格式 (YYYY-MM-DDTHH:mm:ss)
    const startTimeStr = new Date(startTime.value).toISOString().slice(0, 19)
    const endTimeStr = new Date(endTime.value).toISOString().slice(0, 19)
    console.log('查询时间范围:', startTimeStr, '到', endTimeStr)
    const response = await logApi.findByTime(startTimeStr, endTimeStr)
    console.log('查询结果:', response)
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
    } else {
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleFilterByAnyCondition = async () => {
  try {
    loading.value = true
    error.value = ''
    searchByUser.value = false
    searchByGoods.value = false
    
    // 构建查询条件，只包含有值的字段
    const condition: OperationLogDTO = {} as OperationLogDTO
    
    // 只有当 operateType 有值时才添加（不能是空字符串）
    if (anyCondition.value.operateType && anyCondition.value.operateType !== '') {
      condition.operateType = String(anyCondition.value.operateType)
    }
    
    if (anyCondition.value.goodsId) {
      condition.goodsId = anyCondition.value.goodsId
    }
    if (anyCondition.value.goodsName && anyCondition.value.goodsName.trim()) {
      condition.goodsName = anyCondition.value.goodsName.trim()
    }
    if (anyCondition.value.formerWarehouseId) {
      condition.formerWarehouseId = anyCondition.value.formerWarehouseId
    }
    if (anyCondition.value.newWarehouseId) {
      condition.newWarehouseId = anyCondition.value.newWarehouseId
    }
    if (anyCondition.value.updateUser && anyCondition.value.updateUser.trim()) {
      condition.updateUser = anyCondition.value.updateUser.trim()
    }
    // 时间字段：只有当有值时才添加，避免后端查询条件错误
    // 注意：后端代码中时间条件没有判空，如果发送 null 可能会导致查询失败
    // 所以只有当用户明确输入了时间时才添加
    if (anyCondition.value.startTime && anyCondition.value.startTime.trim()) {
      condition.startTime = new Date(anyCondition.value.startTime).toISOString().slice(0, 19)
    }
    if (anyCondition.value.endTime && anyCondition.value.endTime.trim()) {
      condition.endTime = new Date(anyCondition.value.endTime).toISOString().slice(0, 19)
    }
    
    // 检查是否有至少一个条件
    if (!condition.operateType && !condition.goodsId && !condition.goodsName && 
        !condition.formerWarehouseId && !condition.newWarehouseId && 
        !condition.updateUser && !condition.startTime && !condition.endTime) {
      error.value = '请至少输入一个查询条件'
      loading.value = false
      return
    }
    
    // 检查是否有至少一个条件（包括操作类型）
    const hasAnyCondition = condition.operateType || condition.goodsId || condition.goodsName || 
        condition.formerWarehouseId || condition.newWarehouseId || 
        condition.updateUser || condition.startTime || condition.endTime
    
    if (!hasAnyCondition) {
      error.value = '请至少输入一个查询条件'
      loading.value = false
      return
    }
    
    // 如果用户没有选择操作类型，不发送 operateType 字段（后端已支持）
    // 前端不需要强制要求选择操作类型
    
    console.log('任意条件查询:', condition)
    const response = await logApi.findByAnyCondition(condition)
    console.log('查询结果:', response)
    if (response.data) {
      logs.value = response.data
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: logs.value.length,
        size: logs.value.length,
      }
      showAnyConditionModal.value = false
    } else {
      logs.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '筛选失败'
    console.error('筛选错误:', err)
    logs.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const resetAnyCondition = () => {
  anyCondition.value = {
    operateType: '',
    goodsId: undefined,
    goodsName: '',
    formerWarehouseId: undefined,
    newWarehouseId: undefined,
    updateUser: '',
    startTime: '',
    endTime: '',
  }
}

const handleReset = () => {
  filterType.value = ''
  warehouseId.value = ''
  userName.value = ''
  goodsId.value = '' as any
  goodsName.value = ''
  startTime.value = ''
  endTime.value = ''
  searchByUser.value = false
  searchByGoods.value = false
  resetAnyCondition()
  currentPage.value = 1
  loadLogs()
}

const handlePageChange = (page: number) => {
  if (page < 1) return
  currentPage.value = page
  if (searchByGoods.value && goodsId.value) {
    handleFilterByGoodsId()
  } else if (searchByGoods.value && goodsName.value) {
    handleFilterByGoodsName()
  } else if (searchByUser.value && userName.value) {
    handleFilterByUserName()
  } else if (filterType.value && filterType.value >= 1 && filterType.value <= 4) {
    handleFilterByType()
  } else {
    loadLogs()
  }
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  if (searchByGoods.value && goodsId.value) {
    handleFilterByGoodsId()
  } else if (searchByGoods.value && goodsName.value) {
    handleFilterByGoodsName()
  } else if (searchByUser.value && userName.value) {
    handleFilterByUserName()
  } else if (filterType.value && filterType.value >= 1 && filterType.value <= 4) {
    handleFilterByType()
  } else {
    loadLogs()
  }
}

// 计算可见的页码
const visiblePages = computed(() => {
  const pages: number[] = []
  const total = pageInfo.value.pages
  const current = pageInfo.value.current
  
  if (total <= 7) {
    // 如果总页数少于等于7，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 如果总页数大于7，显示当前页附近的页码
    if (current <= 4) {
      // 当前页在前4页
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push(total)
    } else if (current >= total - 3) {
      // 当前页在后4页
      pages.push(1)
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // 当前页在中间
      pages.push(1)
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push(total)
    }
  }
  
  return pages
})

const getOperateTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    ADD: '新增',
    MOD_MESSAGE: '修改信息',
    MOD_WAREHOUSE: '修改仓库',
    DELETE: '删除',
  }
  return typeMap[type] || type
}
</script>

<style scoped>
.log-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  margin: 0;
  white-space: nowrap;
}

.filter-item input,
.filter-item select {
  width: auto;
  min-width: 150px;
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

/* 分页样式 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.page-numbers {
  display: flex;
  gap: 5px;
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-size-select label {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.page-size-select select {
  padding: 4px 8px;
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

.empty-message {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-content-large {
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-group input[type='text'],
.form-group input[type='number'],
.form-group input[type='email'],
.form-group input[type='password'],
.form-group input[type='datetime-local'],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.modal-actions {
  margin-top: 30px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
  opacity: 0;
}
</style>
