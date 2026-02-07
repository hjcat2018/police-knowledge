export interface LoginParams {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
  deviceType?: string
}

export interface LoginResponse {
  userId: number
  username: string
  realName: string
  avatar: string
  phone: string
  email: string
  token: string
  expiresIn: number
  tokenType: string
  roles: string[]
  permissions: string[]
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  avatar: string
  phone: string
  email: string
  gender: number
  idCard: string
  policeNo: string
  roles: string[]
  permissions: string[]
}

export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface User {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  avatar: string
  gender: number
  idCard: string
  policeNo: string
  status: number
  lastLoginTime: string
  remark: string
  roles?: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export type OriginScope = 
  | 'national' 
  | 'ministerial' 
  | 'provincial' 
  | 'municipal' 
  | 'county' 
  | 'unit'

export const ORIGIN_SCOPE_OPTIONS = [
  { value: 'national', label: '国家级' },
  { value: 'ministerial', label: '部级' },
  { value: 'provincial', label: '省级' },
  { value: 'municipal', label: '市级' },
  { value: 'county', label: '县级' },
  { value: 'unit', label: '单位级' }
]
