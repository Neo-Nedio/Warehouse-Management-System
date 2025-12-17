import request from './request'
import { Goods, GoodsDTO, PageDTO, GoodsInWarehouseVO, PageResult } from '../types'

export const goodsApi = {
  save: (data: GoodsDTO) => {
    return request.post('/goods/save', data)
  },

  saveListInSameWarehouse: (data: GoodsDTO[]) => {
    return request.post('/goods/saveListInSameWarehouse', data)
  },

  modMessage: (data: GoodsDTO) => {
    return request.put('/goods/mod/message', data)
  },

  modWarehouse: (data: GoodsDTO) => {
    return request.put('/goods/mod/warehouse', data)
  },

  delete: (data: GoodsDTO) => {
    return request.delete('/goods/delete', { data })
  },

  findGoodsByID: (id: number) => {
    return request.get<Goods>('/goods/findGoodsByID', { params: { id } })
  },

  findByNameLike: (name: string) => {
    return request.get<Goods[]>('/goods/findByNameLike', { params: { name } })
  },

  listPage: (data: PageDTO) => {
    return request.post<PageResult<Goods>>('/goods/listPage', data)
  },

  findGoodsByWarehouseID: (id: number) => {
    return request.get<Goods[]>('/goods/findGoodsByWarehouseID', { params: { id } })
  },

  findGoodsAllByManagedWarehouseIds: () => {
    return request.get<GoodsInWarehouseVO[]>('/goods/findGoodsAllByManagedWarehouseIds')
  },

  findGoodsByNameLikeInByManagedWarehouseIds: (name: string) => {
    return request.get<GoodsInWarehouseVO[]>('/goods/findGoodsByNameLikeInByManagedWarehouseIds', {
      params: { name },
    })
  },

  findGoodsByAnyCondition: (data: GoodsDTO) => {
    return request.post<Goods[]>('/goods/findGoodsByAnyCondition', data)
  },

  // 导出仓库商品Excel
  export: (warehouseId: number) => {
    return request.get('/goods/export', {
      params: { warehouseId },
      responseType: 'blob', // 重要：指定响应类型为blob
    })
  },
}
