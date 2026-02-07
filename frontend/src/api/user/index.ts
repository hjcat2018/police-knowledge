import request from '@/utils/request'
import { User, PageResult } from '@/types/api'

export function getUserList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
}): Promise<PageResult<User>> {
  return request.get('/v1/users', { params })
}

export function getUserById(id: number): Promise<User> {
  return request.get(`/v1/users/${id}`)
}

export function createUser(data: Partial<User>): Promise<User> {
  return request.post('/v1/users', data)
}

export function updateUser(id: number, data: Partial<User>): Promise<User> {
  return request.put(`/v1/users/${id}`, data)
}

export function deleteUser(id: number): Promise<void> {
  return request.delete(`/v1/users/${id}`)
}

export function batchDeleteUsers(ids: number[]): Promise<void> {
  return request.delete('/v1/users/batch', { data: ids })
}

export function changeUserStatus(id: number, status: number): Promise<void> {
  return request.put(`/v1/users/${id}/status`, null, { params: { status } })
}

export function resetPassword(id: number): Promise<void> {
  return request.put(`/v1/users/${id}/reset-password`)
}
