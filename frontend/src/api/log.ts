import request from './request'
import { OperationLog, OperationLogDTO, PageDTO } from '../types'

export const logApi = {
  findAll: () => {
    return request.get<OperationLog[]>('/log/findAll')
  },

  findAllByType: (type: number) => {
    return request.get<OperationLog[]>(`/log/findAllByType/${type}`)
  },

  findAllByTypeAndPage: (data: PageDTO) => {
    return request.post<OperationLog[]>('/log/findAllByTypeAndPage', data)
  },

  findAllByTypeAndWarehouseId: (type: number, warehouseId: number) => {
    return request.get<OperationLog[]>('/log/findAllByTypeAndWarehouseId', {
      params: { type, warehouseId },
    })
  },

  findByWarehouseId: (warehouseId: number) => {
    return request.get<OperationLog[]>(`/log/findByWarehouseId/${warehouseId}`)
  },

  findByUpdateName: (name: string) => {
    return request.get(`/log/findByUpdateName/${name}`)
  },

  findByGoodsName: (name: string) => {
    return request.get<OperationLog[]>('/log/findByGoodsName', { params: { name } })
  },

  findByGoodsId: (goodsId: number) => {
    return request.get<OperationLog[]>(`/log/findByGoodsId/${goodsId}`)
  },

  findByAnyCondition: (data: OperationLogDTO) => {
    return request.post<OperationLog[]>('/log/findByAnyCondition', data)
  },

  findByTime: (startTime: string, endTime: string) => {
    return request.get<OperationLog[]>('/log/findByTime', {
      params: { startTime, endTime },
    })
  },
}
