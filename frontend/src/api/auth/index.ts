import request from '@/utils/request'
import { LoginParams, LoginResponse, UserInfo, ChangePasswordParams } from '@/types/api'

export function login(data: LoginParams): Promise<LoginResponse> {
  return request.post('/v1/auth/login', data)
}

export function logout(): Promise<void> {
  return request.post('/v1/auth/logout')
}

export function getUserInfo(): Promise<UserInfo> {
  return request.get('/v1/auth/info')
}

export function changePassword(data: ChangePasswordParams): Promise<void> {
  return request.post('/v1/auth/change-password', data)
}

export function checkLoginStatus(): Promise<{ isLogin: boolean }> {
  return request.get('/v1/auth/check-status')
}
