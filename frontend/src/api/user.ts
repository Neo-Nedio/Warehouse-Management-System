import request from './request'
import { LoginRequest, LoginResponse, User, UserDTO, PageDTO } from '../types'

export const userApi = {
  getCode: (email: string) => {
    return request.get('/user/code', { params: { email } })
  },

  loginByPassword: (data: LoginRequest) => {
    return request.post<LoginResponse>('/user/loginByPassword', data)
  },

  loginByCode: (data: LoginRequest) => {
    return request.post<LoginResponse>('/user/loginByCode', data)
  },

  updatePassword: (data: LoginRequest) => {
    return request.post('/user/updatePassword', data)
  },

  refreshToken: (refreshToken: string) => {
    return request.post<LoginResponse>('/user/refresh', null, {
      params: { RefreshToken: refreshToken },
    })
  },

  logout: () => {
    return request.get('/user/logOut')
  },

  findByNameLike: (name: string) => {
    return request.get<User[]>('/user/findByNameLike', { params: { name } })
  },

  listPage: (data: PageDTO) => {
    return request.post<User[]>('/user/listPage', data)
  },

  findById: (id: number) => {
    return request.get<User>('/user/findById', { params: { id } })
  },

  save: (data: UserDTO) => {
    return request.post('/user/save', data)
  },

  mod: (data: UserDTO) => {
    return request.put('/user/mod', data)
  },

  delete: (id: number) => {
    return request.delete('/user/delete', { params: { id } })
  },
}
