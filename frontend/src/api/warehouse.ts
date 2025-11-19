import request from './request'
import { Warehouse, WarehouseDTO, WarehouseAndUserDTO, PageDTO } from '../types'

export const warehouseApi = {
  save: (data: WarehouseDTO) => {
    return request.post('/warehouse/admin/save', data)
  },

  mod: (data: WarehouseDTO) => {
    return request.put('/warehouse/admin/mod', data)
  },

  delete: (id: number) => {
    return request.delete('/warehouse/admin/delete', { params: { id } })
  },

  findByNameLike: (name: string) => {
    return request.get<Warehouse[]>('/warehouse/admin/findByNameLike', { params: { name } })
  },

  listPage: (data: PageDTO) => {
    return request.post<Warehouse[]>('/warehouse/admin/listPage', data)
  },

  findById: (id: number) => {
    return request.get<Warehouse>('/warehouse/admin/findById', { params: { id } })
  },

  saveRelation: (data: WarehouseAndUserDTO) => {
    return request.post('/warehouse/admin/saveRelation', data)
  },

  modRelation: (data: WarehouseAndUserDTO) => {
    return request.put('/warehouse/admin/modRelation', data)
  },

  deleteRelation: (userID: number, warehouseID: number) => {
    return request.delete('/warehouse/admin/deleteRelation', {
      params: { userID, warehouseID },
    })
  },

  findRelation: (userID: number, warehouseID: number) => {
    return request.get('/warehouse/admin/findRelation', {
      params: { userID, warehouseID },
    })
  },

  findRelationForUser: (data: PageDTO) => {
    return request.post('/warehouse/admin/findRelationForUser', data)
  },

  findRelationForWarehouse: (data: PageDTO) => {
    return request.post('/warehouse/admin/findRelationForWarehouse', data)
  },
}
