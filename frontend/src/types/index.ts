export interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
  timestamp: number
}

export interface LoginRequest {
  email: string
  password?: string
  code?: number
}

export interface LoginResponse {
  user: User
  tokens: {
    accessToken: string
    refreshToken: string
  }
}

export interface User {
  id: number
  name: string
  email: string
  phone: string
  sex: number
  age: number
  roleId: number
  managedWarehouseIds?: number[]
}

export interface UserDTO {
  id?: number
  name: string
  email: string
  phone: string
  password?: string
  sex: number
  age: number
  roleId: number
}

export interface Goods {
  id: number
  name: string
  price: number
  number: number
  warehouseId: number
  status: number
  createTime?: string
  createUser?: string
  updateTime?: string
  updateUser?: string
}

export interface GoodsDTO {
  id?: number
  name: string
  price: number
  number: number
  warehouseId: number
  createTime?: string
  createUser?: string
  updateTime?: string
  updateUser?: string
  startCreateTime?: string
  endCreateTime?: string
  startUpdateTime?: string
  endUpdateTime?: string
}

export interface Warehouse {
  id: number
  name: string
  description: string
}

export interface WarehouseDTO {
  id?: number
  name: string
  description: string
}

export interface PageDTO {
  pageSize: number
  pageNum: number
  param?: Record<string, any>
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

export interface GoodsInWarehouseVO {
  id: number
  name: string
  description: string
  goods: Goods[]
}

export interface OperationLog {
  id: number
  operateType: string
  goodsId: number
  goodsName: string
  formerWarehouseId?: number
  newWarehouseId?: number
  updateTime: string
  updateUser: string
}

export interface OperationLogDTO {
  id?: number
  operateType: string
  goodsId?: number
  goodsName?: string
  formerWarehouseId?: number
  newWarehouseId?: number
  updateTime?: string
  updateUser?: string
  startTime?: string
  endTime?: string
}

export interface WarehouseAndUserDTO {
  Id?: number
  warehouseId: number
  userId: number
}

export interface WarehouseHaveUserVO {
  id: number
  name: string
  description: string
  users: User[]
}

export interface UserHaveWarehouseVO {
  id: number
  name: string
  email: string
  phone: string
  sex: number
  age: number
  roleId: number
  managedWarehouse: Warehouse[]
}

export interface UserHavaOperationVO {
  id: number
  name: string
  operationLogs: OperationLog[]
}
