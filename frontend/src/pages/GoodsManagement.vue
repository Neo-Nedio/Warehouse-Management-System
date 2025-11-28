<template>
  <div class="goods-management">
    <div class="page-header">
      <h2>商品管理</h2>
      <div>
        <button class="btn btn-primary" @click="handleOpenAddModal">新增商品</button>
        <button class="btn btn-success" @click="handleOpenBatchModal" style="margin-left: 10px">批量新增</button>
      </div>
    </div>

    <div class="card">
      <div class="search-bar">
        <select v-model="searchType" class="search-type-select">
          <option value="name">按商品名称</option>
          <option value="id">按商品ID</option>
          <option value="warehouseId">按仓库ID</option>
          <option value="warehouseName">按仓库名称</option>
        </select>
        <input 
          type="text" 
          :placeholder="getSearchPlaceholder()" 
          v-model="searchName" 
          @keypress.enter="handleSearch" 
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn" @click="handleOpenAdvancedSearch">高级搜索</button>
        <button class="btn btn-success" @click="handleLoadAllManagedWarehouseGoods">查看所有管理仓库商品</button>
        <button class="btn" @click="() => { currentPage = 1; loadGoods(usePagination) }">刷新</button>
        <button class="btn" @click="() => { usePagination = !usePagination; currentPage = 1; loadGoods(usePagination) }">
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
            <th>商品名称</th>
            <th>价格</th>
            <th>数量</th>
            <th>仓库ID</th>
            <th>仓库名称</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <transition-group name="list">
            <tr v-for="item in goods" :key="item.id" class="table-row">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.price }}</td>
            <td>{{ item.number }}</td>
            <td>{{ item.warehouseId }}</td>
              <td>{{ getWarehouseName(item.warehouseId) }}</td>
            <td>{{ item.status === 0 ? '已删除' : '正常' }}</td>
            <td>
              <button class="btn btn-primary" @click="handleEdit(item)">编辑</button>
                <button class="btn btn-warning" @click="handleModifyWarehouse(item)" style="margin-left: 10px" :disabled="item.status === 0">
                  修改仓库
                </button>
              <button class="btn btn-danger" @click="handleDelete(item)" style="margin-left: 10px" :disabled="item.status === 0">
                删除
              </button>
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
        <h3>{{ editingGoods ? '编辑商品' : '新增商品' }}</h3>
        <div class="form-group">
          <label>商品名称</label>
          <input type="text" v-model="formData.name" required />
        </div>
        <div class="form-group">
          <label>价格</label>
          <input type="number" v-model.number="formData.price" required min="0" />
        </div>
        <div class="form-group">
          <label>数量</label>
          <input type="number" v-model.number="formData.number" required min="0" />
        </div>
        <div class="form-group" v-if="!editingGoods">
          <label>选择仓库</label>
          <select v-model.number="formData.warehouseId" required>
            <option :value="0">请选择仓库</option>
            <option v-for="warehouse in availableWarehouses" :key="warehouse.id" :value="warehouse.id">
              {{ warehouse.name }} (ID: {{ warehouse.id }})
            </option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleSave" :disabled="loading">保存</button>
          <button class="btn" @click="showModal = false; editingGoods = null; resetForm()">取消</button>
        </div>
      </div>
    </div>
    </transition>

    <!-- 高级搜索模态框 -->
    <transition name="modal">
      <div v-if="showAdvancedSearch" class="modal-overlay" @click="showAdvancedSearch = false">
      <div class="modal-content advanced-search-modal" @click.stop>
        <h3>高级搜索</h3>
        <div class="advanced-search-form">
          <div class="form-row">
            <div class="form-group">
              <label>商品ID</label>
              <input type="number" v-model.number="advancedSearch.id" placeholder="精确匹配" />
            </div>
            <div class="form-group">
              <label>商品名称</label>
              <input type="text" v-model="advancedSearch.name" placeholder="模糊匹配" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>价格</label>
              <input type="number" v-model.number="advancedSearch.price" placeholder="精确匹配" />
            </div>
            <div class="form-group">
              <label>数量</label>
              <input type="number" v-model.number="advancedSearch.number" placeholder="精确匹配" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>仓库</label>
              <select v-model.number="advancedSearch.warehouseId">
                <option :value="0">全部仓库</option>
                <option v-for="warehouse in availableWarehouses" :key="warehouse.id" :value="warehouse.id">
                  {{ warehouse.name }} (ID: {{ warehouse.id }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>创建人</label>
              <input type="text" v-model="advancedSearch.createUser" placeholder="模糊匹配" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>更新人</label>
              <input type="text" v-model="advancedSearch.updateUser" placeholder="模糊匹配" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>创建时间（开始）</label>
              <input type="datetime-local" v-model="advancedSearch.startCreateTime" />
            </div>
            <div class="form-group">
              <label>创建时间（结束）</label>
              <input type="datetime-local" v-model="advancedSearch.endCreateTime" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>更新时间（开始）</label>
              <input type="datetime-local" v-model="advancedSearch.startUpdateTime" />
            </div>
            <div class="form-group">
              <label>更新时间（结束）</label>
              <input type="datetime-local" v-model="advancedSearch.endUpdateTime" />
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleAdvancedSearch" :disabled="loading">搜索</button>
          <button class="btn" @click="resetAdvancedSearch">重置</button>
          <button class="btn" @click="showAdvancedSearch = false">取消</button>
        </div>
      </div>
      </div>
    </transition>

    <!-- 批量新增模态框 -->
    <transition name="modal">
      <div v-if="showBatchModal" class="modal-overlay" @click="showBatchModal = false">
        <div class="modal-content batch-add-modal" @click.stop>
          <h3>批量新增商品</h3>
          <transition name="fade">
            <div class="error-message" v-if="error">{{ error }}</div>
          </transition>
          <div class="form-group">
            <label>选择仓库 <span style="color: red;">*</span></label>
            <select v-model.number="batchWarehouseId" required @change="clearBatchError">
              <option :value="0">请选择仓库</option>
              <option v-for="warehouse in availableWarehouses" :key="warehouse.id" :value="warehouse.id">
                {{ warehouse.name }} (ID: {{ warehouse.id }})
              </option>
            </select>
            <span v-if="!batchWarehouseId || batchWarehouseId === 0" class="field-error">请选择仓库</span>
          </div>
          <div class="batch-goods-list">
            <div class="batch-header">
              <h4>商品列表</h4>
              <button type="button" class="btn btn-primary btn-sm" @click="addBatchGoodsItem">添加商品</button>
            </div>
            <div class="batch-goods-table">
              <table class="table">
                <thead>
                  <tr>
                    <th style="width: 40px;">序号</th>
                    <th>商品名称</th>
                    <th>价格</th>
                    <th>数量</th>
                    <th style="width: 80px;">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in batchGoodsList" :key="index" :class="{ 'invalid-row': isBatchItemInvalid(item) }">
                    <td>{{ index + 1 }}</td>
                    <td>
                      <input 
                        type="text" 
                        v-model="item.name" 
                        placeholder="商品名称" 
                        class="batch-input"
                        @input="clearBatchError"
                        required
                      />
                      <span v-if="!item.name || !item.name.trim()" class="field-error-small">必填</span>
                    </td>
                    <td>
                      <input 
                        type="number" 
                        v-model.number="item.price" 
                        placeholder="价格" 
                        class="batch-input"
                        min="0"
                        @input="clearBatchError"
                        required
                      />
                      <span v-if="!item.price || item.price <= 0" class="field-error-small">必须>0</span>
                    </td>
                    <td>
                      <input 
                        type="number" 
                        v-model.number="item.number" 
                        placeholder="数量" 
                        class="batch-input"
                        min="0"
                        @input="clearBatchError"
                        required
                      />
                      <span v-if="!item.number || item.number <= 0" class="field-error-small">必须>0</span>
                    </td>
                    <td>
                      <button 
                        type="button" 
                        class="btn btn-danger btn-sm" 
                        @click="removeBatchGoodsItem(index)"
                        :disabled="batchGoodsList.length <= 1"
                      >
                        删除
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-actions">
            <button 
              type="button"
              class="btn btn-primary" 
              @click="handleBatchSave" 
              :disabled="loading || !canBatchSave"
            >
              {{ loading ? '保存中...' : '保存' }}
            </button>
            <button type="button" class="btn" @click="resetBatchForm">重置</button>
            <button type="button" class="btn" @click="showBatchModal = false; resetBatchForm()">取消</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 修改仓库模态框 -->
    <transition name="modal">
      <div v-if="showWarehouseModal" class="modal-overlay" @click="showWarehouseModal = false">
      <div class="modal-content" @click.stop>
        <h3>修改商品仓库</h3>
        <div class="form-group">
          <label>商品ID</label>
          <input type="number" :value="modifyingGoods?.id" disabled />
        </div>
        <div class="form-group">
          <label>商品名称</label>
          <input type="text" :value="modifyingGoods?.name" disabled />
        </div>
        <div class="form-group">
          <label>当前仓库ID</label>
          <input type="number" :value="modifyingGoods?.warehouseId" disabled />
        </div>
        <div class="form-group">
          <label>选择新仓库</label>
          <select v-model.number="newWarehouseId" required>
            <option :value="0">请选择仓库</option>
            <option v-for="warehouse in availableWarehouses" :key="warehouse.id" :value="warehouse.id">
              {{ warehouse.name }} (ID: {{ warehouse.id }})
            </option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleSaveWarehouse" :disabled="loading || !newWarehouseId">保存</button>
          <button class="btn" @click="showWarehouseModal = false; modifyingGoods = null; newWarehouseId = 0">取消</button>
        </div>
      </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { goodsApi } from '@/api/goods'
import { warehouseApi } from '@/api/warehouse'
import { Goods, GoodsDTO, GoodsInWarehouseVO, Warehouse, PageResult } from '@/types'

const goods = ref<Goods[]>([])
const loading = ref(false)
const error = ref('')
const showModal = ref(false)
const editingGoods = ref<Goods | null>(null)
const searchName = ref('')
const searchType = ref<'name' | 'id' | 'warehouseId' | 'warehouseName'>('name')
const showAdvancedSearch = ref(false)
const showWarehouseModal = ref(false)
const showBatchModal = ref(false)
const modifyingGoods = ref<Goods | null>(null)
const newWarehouseId = ref(0)
const availableWarehouses = ref<Warehouse[]>([])
const warehousesData = ref<GoodsInWarehouseVO[]>([])

// 批量新增相关
const batchWarehouseId = ref(0)
const batchGoodsList = ref<Array<{ name: string; price: number; number: number }>>([
  { name: '', price: 0, number: 0 }
])

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

// 高级搜索表单数据
const advancedSearch = ref<GoodsDTO>({
  id: undefined,
  name: '',
  price: 0,
  number: 0,
  warehouseId: 0,
  createUser: '',
  updateUser: '',
  startCreateTime: '',
  endCreateTime: '',
  startUpdateTime: '',
  endUpdateTime: '',
})

const formData = ref<GoodsDTO>({
  name: '',
  price: 0,
  number: 0,
  warehouseId: 0,
})

onMounted(() => {
  loadGoods()
  // 监听用户仓库关系更新事件
  window.addEventListener('user-warehouses-updated', handleWarehousesUpdated)
})

const handleWarehousesUpdated = () => {
  // 当用户仓库关系更新时，重新加载商品和仓库列表
  loadGoods(usePagination.value)
}

const loadGoods = async (usePage = false) => {
  try {
    loading.value = true
    error.value = ''
    
    if (usePage) {
      // 使用分页查询
      usePagination.value = true
      const response = await goodsApi.listPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        param: searchName.value ? { name: searchName.value } : {},
      })
      if (response.data) {
        const pageResult = response.data as PageResult<Goods>
        // 按ID递减排序
        goods.value = pageResult.records.sort((a, b) => b.id - a.id)
        pageInfo.value = {
          current: pageResult.current,
          pages: pageResult.pages,
          total: pageResult.total,
          size: pageResult.size,
        }
        // 从商品中提取仓库信息（用于下拉选择）
        const warehouseIds = new Set(goods.value.map(g => g.warehouseId))
        // 需要加载所有仓库信息，这里先使用已有的availableWarehouses
        // 如果没有，则从warehousesData中获取
        if (availableWarehouses.value.length === 0) {
          await loadWarehousesForSelect()
        }
      }
    } else {
      // 使用原来的全量查询
      usePagination.value = false
    const response = await goodsApi.findGoodsAllByManagedWarehouseIds()
    if (response.data) {
        warehousesData.value = response.data
      // 将 GoodsInWarehouseVO[] 转换为 Goods[]
      // 每个仓库下的商品列表合并成一个数组
      const allGoods: Goods[] = []
      response.data.forEach((warehouse: GoodsInWarehouseVO) => {
        if (warehouse.goods && warehouse.goods.length > 0) {
          allGoods.push(...warehouse.goods)
        }
      })
        // 按ID递减排序
        goods.value = allGoods.sort((a, b) => b.id - a.id)
        // 提取可用的仓库列表
        availableWarehouses.value = response.data.map((w: GoodsInWarehouseVO) => ({
          id: w.id,
          name: w.name,
          description: w.description,
        }))
      }
    }
  } catch (err: any) {
    error.value = err.message || '加载商品列表失败'
    console.error('加载商品错误:', err)
  } finally {
    loading.value = false
  }
}

const loadWarehousesForSelect = async () => {
  // 加载仓库列表用于下拉选择
  try {
    const response = await goodsApi.findGoodsAllByManagedWarehouseIds()
    if (response.data) {
      availableWarehouses.value = response.data.map((w: GoodsInWarehouseVO) => ({
        id: w.id,
        name: w.name,
        description: w.description,
      }))
    }
  } catch (err: any) {
    console.error('加载仓库列表错误:', err)
  }
}

// 查看所有管理仓库的商品
const handleLoadAllManagedWarehouseGoods = async () => {
  try {
    loading.value = true
    error.value = ''
    usePagination.value = false
    searchName.value = '' // 清空搜索条件
    
    const response = await goodsApi.findGoodsAllByManagedWarehouseIds()
    if (response.data) {
      warehousesData.value = response.data
      // 将 GoodsInWarehouseVO[] 转换为 Goods[]
      // 每个仓库下的商品列表合并成一个数组
      const allGoods: Goods[] = []
      response.data.forEach((warehouse: GoodsInWarehouseVO) => {
        if (warehouse.goods && warehouse.goods.length > 0) {
          allGoods.push(...warehouse.goods)
        }
      })
      // 按ID递减排序
      goods.value = allGoods.sort((a, b) => b.id - a.id)
      // 提取可用的仓库列表
      availableWarehouses.value = response.data.map((w: GoodsInWarehouseVO) => ({
        id: w.id,
        name: w.name,
        description: w.description,
      }))
      // 更新分页信息
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: allGoods.length,
        size: allGoods.length,
      }
    } else {
      goods.value = []
      availableWarehouses.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '加载所有管理仓库商品失败'
    console.error('加载所有管理仓库商品错误:', err)
    goods.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

// 获取仓库名称
const getWarehouseName = (warehouseId: number): string => {
  const warehouse = availableWarehouses.value.find(w => w.id === warehouseId)
  if (warehouse) {
    return warehouse.name
  }
  // 如果availableWarehouses中没有，尝试从warehousesData中查找
  const warehouseVO = warehousesData.value.find(w => w.id === warehouseId)
  if (warehouseVO) {
    return warehouseVO.name
  }
  return `仓库ID: ${warehouseId}`
}

// 获取搜索框占位符
const getSearchPlaceholder = () => {
  switch (searchType.value) {
    case 'id':
      return '输入商品ID'
    case 'warehouseId':
      return '输入仓库ID'
    case 'warehouseName':
      return '输入仓库名称'
    case 'name':
    default:
      return '搜索商品名称'
  }
}

const handleSearch = async () => {
  if (!searchName.value.trim()) {
    currentPage.value = 1
    loadGoods(usePagination.value)
    return
  }
  try {
    loading.value = true
    error.value = ''
    usePagination.value = false // 仓库搜索不使用分页
    
    if (searchType.value === 'id') {
      // 按商品ID搜索
      const id = parseInt(searchName.value)
      if (isNaN(id)) {
        error.value = '请输入有效的商品ID'
        return
      }
      try {
        const response = await goodsApi.findGoodsByID(id)
        if (response.data) {
          // 按ID递减排序（单条数据也保持一致性）
          goods.value = [response.data].sort((a, b) => b.id - a.id)
          pageInfo.value = { current: 1, pages: 1, total: 1, size: 1 }
        } else {
          // 未找到商品，只显示无数据，不显示错误
          goods.value = []
          error.value = '' // 清空错误信息
          pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
        }
      } catch (err: any) {
        // 请求失败（网络错误等），只显示无数据，不显示错误信息
        goods.value = []
        error.value = '' // 清空错误信息
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
        console.error('查找商品失败:', err)
        return // 提前返回，避免被外层 catch 捕获
      }
    } else if (searchType.value === 'warehouseId') {
      // 按仓库ID搜索
      const warehouseId = parseInt(searchName.value)
      if (isNaN(warehouseId)) {
        error.value = '请输入有效的仓库ID'
        return
      }
      const response = await goodsApi.findGoodsByWarehouseID(warehouseId)
      if (response.data) {
        const warehouseVO = response.data as GoodsInWarehouseVO
        // 更新仓库信息到availableWarehouses
        const existingIndex = availableWarehouses.value.findIndex(w => w.id === warehouseVO.id)
        if (existingIndex >= 0) {
          availableWarehouses.value[existingIndex] = {
            id: warehouseVO.id,
            name: warehouseVO.name,
            description: warehouseVO.description,
          }
        } else {
          availableWarehouses.value.push({
            id: warehouseVO.id,
            name: warehouseVO.name,
            description: warehouseVO.description,
          })
        }
        
        if (warehouseVO.goods && warehouseVO.goods.length > 0) {
          // 按ID递减排序
          goods.value = warehouseVO.goods.sort((a, b) => b.id - a.id)
          pageInfo.value = { 
            current: 1, 
            pages: 1, 
            total: warehouseVO.goods.length, 
            size: warehouseVO.goods.length 
          }
        } else {
          goods.value = []
          error.value = `仓库 "${warehouseVO.name}" (ID: ${warehouseId}) 中没有商品`
          pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
        }
      } else {
        goods.value = []
        error.value = '未找到该仓库或您没有权限访问'
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    } else if (searchType.value === 'warehouseName') {
      // 按仓库名称搜索
      // 先根据仓库名称查找仓库
      const warehouseResponse = await warehouseApi.findByNameLike(searchName.value)
      if (!warehouseResponse.data || warehouseResponse.data.length === 0) {
        goods.value = []
        error.value = `未找到名称包含 "${searchName.value}" 的仓库`
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
        return
      }
      
      const warehouses = warehouseResponse.data as Warehouse[]
      // 更新仓库信息到availableWarehouses
      warehouses.forEach(warehouse => {
        const existingIndex = availableWarehouses.value.findIndex(w => w.id === warehouse.id)
        if (existingIndex >= 0) {
          availableWarehouses.value[existingIndex] = warehouse
        } else {
          availableWarehouses.value.push(warehouse)
        }
      })
      
      // 对每个仓库查找商品
      const allGoods: Goods[] = []
      for (const warehouse of warehouses) {
        try {
          const goodsResponse = await goodsApi.findGoodsByWarehouseID(warehouse.id)
          if (goodsResponse.data) {
            const warehouseVO = goodsResponse.data as GoodsInWarehouseVO
            if (warehouseVO.goods && warehouseVO.goods.length > 0) {
              allGoods.push(...warehouseVO.goods)
            }
          }
        } catch (err) {
          // 忽略单个仓库的错误，继续查找其他仓库
          console.warn(`查找仓库 ${warehouse.name} (ID: ${warehouse.id}) 的商品失败:`, err)
        }
      }
      
      if (allGoods.length > 0) {
        // 按ID递减排序
        goods.value = allGoods.sort((a, b) => b.id - a.id)
        pageInfo.value = { 
          current: 1, 
          pages: 1, 
          total: allGoods.length, 
          size: allGoods.length 
        }
      } else {
        goods.value = []
        error.value = `找到 ${warehouses.length} 个匹配的仓库，但这些仓库中都没有商品`
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    } else {
      // 按商品名称搜索，使用分页
      usePagination.value = true
      currentPage.value = 1
      const response = await goodsApi.listPage({
        pageNum: 1,
        pageSize: pageSize.value,
        param: { name: searchName.value },
      })
      if (response.data) {
        const pageResult = response.data as PageResult<Goods>
        // 按ID递减排序
        goods.value = pageResult.records.sort((a, b) => b.id - a.id)
        pageInfo.value = {
          current: pageResult.current,
          pages: pageResult.pages,
          total: pageResult.total,
          size: pageResult.size,
        }
      } else {
        goods.value = []
        pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
      }
    }
  } catch (err: any) {
    error.value = err.message || '搜索失败'
    console.error('搜索错误:', err)
    goods.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  if (page < 1 || page > pageInfo.value.pages) return
  currentPage.value = page
  loadGoods(true)
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  pageInfo.value.size = pageSize.value
  loadGoods(true)
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

const handleAdvancedSearch = async () => {
  try {
    loading.value = true
    error.value = ''
    usePagination.value = false // 高级搜索不使用分页
    
    // 构建查询条件，只包含有值的字段
    const searchCondition: GoodsDTO = {} as GoodsDTO
    
    if (advancedSearch.value.id) {
      searchCondition.id = advancedSearch.value.id
    }
    if (advancedSearch.value.name && advancedSearch.value.name.trim()) {
      searchCondition.name = advancedSearch.value.name.trim()
    }
    if (advancedSearch.value.price && advancedSearch.value.price > 0) {
      searchCondition.price = advancedSearch.value.price
    }
    if (advancedSearch.value.number && advancedSearch.value.number > 0) {
      searchCondition.number = advancedSearch.value.number
    }
    if (advancedSearch.value.warehouseId && advancedSearch.value.warehouseId > 0) {
      searchCondition.warehouseId = advancedSearch.value.warehouseId
    }
    if (advancedSearch.value.createUser && advancedSearch.value.createUser.trim()) {
      searchCondition.createUser = advancedSearch.value.createUser.trim()
    }
    if (advancedSearch.value.updateUser && advancedSearch.value.updateUser.trim()) {
      searchCondition.updateUser = advancedSearch.value.updateUser.trim()
    }
    if (advancedSearch.value.startCreateTime) {
      searchCondition.startCreateTime = new Date(advancedSearch.value.startCreateTime).toISOString()
    }
    if (advancedSearch.value.endCreateTime) {
      searchCondition.endCreateTime = new Date(advancedSearch.value.endCreateTime).toISOString()
    }
    if (advancedSearch.value.startUpdateTime) {
      searchCondition.startUpdateTime = new Date(advancedSearch.value.startUpdateTime).toISOString()
    }
    if (advancedSearch.value.endUpdateTime) {
      searchCondition.endUpdateTime = new Date(advancedSearch.value.endUpdateTime).toISOString()
    }
    
    const response = await goodsApi.findGoodsByAnyCondition(searchCondition)
    if (response.data) {
      // 按ID递减排序
      goods.value = response.data.sort((a, b) => b.id - a.id)
      pageInfo.value = {
        current: 1,
        pages: 1,
        total: response.data.length,
        size: response.data.length,
      }
      showAdvancedSearch.value = false
    } else {
      goods.value = []
      pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
    }
  } catch (err: any) {
    error.value = err.message || '高级搜索失败'
    console.error('高级搜索错误:', err)
    goods.value = []
    pageInfo.value = { current: 1, pages: 1, total: 0, size: 0 }
  } finally {
    loading.value = false
  }
}

const handleOpenAdvancedSearch = async () => {
  // 如果仓库列表为空，先加载仓库列表
  if (availableWarehouses.value.length === 0) {
    await loadWarehousesForSelect()
  }
  showAdvancedSearch.value = true
}

const resetAdvancedSearch = () => {
  advancedSearch.value = {
    id: undefined,
    name: '',
    price: 0,
    number: 0,
    warehouseId: 0,
    createUser: '',
    updateUser: '',
    startCreateTime: '',
    endCreateTime: '',
    startUpdateTime: '',
    endUpdateTime: '',
  }
}

const handleSave = async () => {
  try {
    loading.value = true
    error.value = ''
    if (editingGoods.value?.id) {
      // 编辑时不能传递warehouseId，后端不允许在mod/message中更改仓库
      const { warehouseId, ...editData } = formData.value
      await goodsApi.modMessage({ ...editData, id: editingGoods.value.id })
    } else {
      // 新增时验证是否选择了仓库
      if (!formData.value.warehouseId || formData.value.warehouseId === 0) {
        error.value = '请选择仓库'
        return
      }
      await goodsApi.save(formData.value)
    }
    showModal.value = false
    editingGoods.value = null
    resetForm()
    loadGoods(usePagination.value)
  } catch (err: any) {
    error.value = err.message || '保存失败'
    console.error('保存错误:', err)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (item: Goods) => {
  if (!confirm('确定要删除这个商品吗？')) return
  try {
    loading.value = true
    error.value = ''
    // 后端只需要id字段，其他字段不传避免验证失败
    await goodsApi.delete({ id: item.id } as GoodsDTO)
    loadGoods(usePagination.value)
  } catch (err: any) {
    error.value = err.message || '删除失败'
    console.error('删除错误:', err)
  } finally {
    loading.value = false
  }
}

const handleEdit = (item: Goods) => {
  editingGoods.value = item
  formData.value = {
    id: item.id,
    name: item.name,
    price: item.price,
    number: item.number,
    warehouseId: item.warehouseId,
  }
  showModal.value = true
}

const resetForm = () => {
  formData.value = {
    name: '',
    price: 0,
    number: 0,
    warehouseId: 0,
  }
}

const handleOpenAddModal = async () => {
  // 如果仓库列表为空，先加载仓库列表
  if (availableWarehouses.value.length === 0) {
    await loadWarehousesForSelect()
  }
  showModal.value = true
}

const handleOpenBatchModal = async () => {
  // 如果仓库列表为空，先加载仓库列表
  if (availableWarehouses.value.length === 0) {
    await loadWarehousesForSelect()
  }
  showBatchModal.value = true
}

const handleModifyWarehouse = (item: Goods) => {
  modifyingGoods.value = item
  newWarehouseId.value = 0
  showWarehouseModal.value = true
}

const addBatchGoodsItem = () => {
  batchGoodsList.value.push({ name: '', price: 0, number: 0 })
}

const removeBatchGoodsItem = (index: number) => {
  if (batchGoodsList.value.length > 1) {
    batchGoodsList.value.splice(index, 1)
  }
}

const resetBatchForm = () => {
  batchWarehouseId.value = 0
  batchGoodsList.value = [{ name: '', price: 0, number: 0 }]
  error.value = ''
}

const clearBatchError = () => {
  error.value = ''
}

const isBatchItemInvalid = (item: { name: string; price: number; number: number }): boolean => {
  return !item.name || !item.name.trim() || !item.price || item.price <= 0 || !item.number || item.number <= 0
}

// 计算是否可以保存
const canBatchSave = computed(() => {
  if (!batchWarehouseId.value || batchWarehouseId.value === 0) {
    return false
  }
  const validGoods = batchGoodsList.value.filter(item => 
    item.name && item.name.trim() && item.price > 0 && item.number > 0
  )
  return validGoods.length > 0
})

const handleBatchSave = async () => {
  // 验证仓库
  if (!batchWarehouseId.value || batchWarehouseId.value === 0) {
    error.value = '请选择仓库'
    return
  }
  
  // 验证商品列表
  const validGoods = batchGoodsList.value.filter(item => 
    item.name && item.name.trim() && item.price > 0 && item.number > 0
  )
  
  if (validGoods.length === 0) {
    error.value = '请至少添加一个有效的商品（名称、价格、数量都必须填写且大于0）'
    return
  }
  
  // 检查是否有无效的商品行
  const invalidItems = batchGoodsList.value.filter(item => isBatchItemInvalid(item))
  if (invalidItems.length > 0) {
    error.value = `有 ${invalidItems.length} 个商品信息不完整，请完善后再保存`
    return
  }
  
  try {
    loading.value = true
    error.value = ''
    
    // 构建批量插入数据，所有商品使用同一个仓库ID
    const batchData: GoodsDTO[] = validGoods.map(item => ({
      name: item.name.trim(),
      price: item.price,
      number: item.number,
      warehouseId: batchWarehouseId.value,
    }))
    
    await goodsApi.saveListInSameWarehouse(batchData)
    showBatchModal.value = false
    resetBatchForm()
    loadGoods(usePagination.value)
  } catch (err: any) {
    error.value = err.message || '批量保存失败'
    console.error('批量保存错误:', err)
  } finally {
    loading.value = false
  }
}

const handleSaveWarehouse = async () => {
  if (!modifyingGoods.value || !newWarehouseId.value) {
    error.value = '请选择新仓库'
    return
  }
  if (newWarehouseId.value === modifyingGoods.value.warehouseId) {
    error.value = '新仓库不能与当前仓库相同'
    return
  }
  try {
    loading.value = true
    error.value = ''
    // 只传id和warehouseId，其他字段不传避免验证失败
    const data: GoodsDTO = {
      id: modifyingGoods.value.id,
      warehouseId: newWarehouseId.value,
    } as GoodsDTO
    await goodsApi.modWarehouse(data)
    showWarehouseModal.value = false
    modifyingGoods.value = null
    newWarehouseId.value = 0
    loadGoods(usePagination.value)
  } catch (err: any) {
    error.value = err.message || '修改仓库失败'
    console.error('修改仓库错误:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.goods-management {
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
  background: white;
  cursor: pointer;
}

.btn-warning {
  background-color: #ff9800;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-warning:hover:not(:disabled) {
  background-color: #f57c00;
}

.btn-warning:disabled {
  background-color: #ccc;
  cursor: not-allowed;
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

.advanced-search-modal {
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.advanced-search-form {
  margin: 20px 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 15px;
}

.form-row .form-group {
  margin-bottom: 0;
}

.form-row .form-group input[type="datetime-local"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-row .form-group input[type="datetime-local"]:focus {
  outline: none;
  border-color: #1890ff;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

/* 批量新增模态框样式 */
.batch-add-modal {
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
}

.batch-goods-list {
  margin: 20px 0;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.batch-header h4 {
  margin: 0;
  color: #333;
}

.batch-goods-table {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.batch-goods-table .table {
  margin: 0;
}

.batch-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.batch-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
}

.btn-success {
  background-color: #52c41a;
  color: white;
}

.btn-success:hover {
  background-color: #73d13d;
}

/* 批量新增表单验证样式 */
.field-error {
  display: block;
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.field-error-small {
  display: block;
  color: #ff4d4f;
  font-size: 11px;
  margin-top: 2px;
}

.invalid-row {
  background-color: #fff1f0;
}

.invalid-row td {
  border-color: #ffccc7;
}

.batch-input:invalid {
  border-color: #ff4d4f;
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
