import request from '@/utils/request'
import { PageResult } from '@/types/api'

export interface SysDict {
  id: number
  kind: string
  code: string
  detail: string
  alias?: string
  description?: string
  icon?: string
  color?: string
  extConfig?: string
  parentId: number
  parentCode: string
  level: number
  sort: number
  status: number
  leaf: number
  isSystem?: number
  isPublic?: number
  viewCount?: number
  favoriteCount?: number
  docCount?: number
  deleted?: number
  children?: SysDict[]
  remark?: string
  createdTime?: string
  updatedTime?: string
}

// ==================== 字典类型管理 ====================

export interface DictTypeDTO {
  kind: string
  detail: string
  alias?: string
  description?: string
  icon?: string
  color?: string
  sort?: number
  status?: number
}

export function getDictTypes(): Promise<SysDict[]> {
  return request.get('/v1/dict/types')
}

export function getDictTypeById(id: number): Promise<SysDict> {
  return request.get(`/v1/dict/types/${id}`)
}

export function createDictType(data: DictTypeDTO): Promise<void> {
  return request.post('/v1/dict/types', data)
}

export function updateDictType(id: number, data: DictTypeDTO): Promise<void> {
  return request.put(`/v1/dict/types/${id}`, data)
}

export function deleteDictType(id: number): Promise<void> {
  return request.delete(`/v1/dict/types/${id}`)
}

// ==================== 字典数据管理 ====================

export interface DictDataDTO {
  id?: number
  kind: string
  code: string
  detail: string
  alias?: string
  description?: string
  icon?: string
  color?: string
  extConfig?: string
  level: number
  parentId?: number | null
  parentCode?: string | null
  sort?: number
  status?: number
  leaf?: number
  isSystem?: number
  isPublic?: number
  remark?: string
}

export interface DictQueryParams {
  pageNum?: number
  pageSize?: number
  kind?: string
  keyword?: string
}

export function getDictPage(params: DictQueryParams): Promise<PageResult<SysDict>> {
  return request.get('/v1/dict', { params })
}

export function getDictTree(kind: string): Promise<SysDict[]> {
  return request.get('/v1/dict/tree', { params: { kind } })
}

export function getParentOptions(kind: string): Promise<SysDict[]> {
  return request.get('/v1/dict/options', { params: { kind } })
}

export function getChildren(kind: string, parentCode?: string | null): Promise<SysDict[]> {
  const params: Record<string, string> = { kind }
  if (parentCode) {
    params.parentCode = parentCode
  }
  return request.get('/v1/dict/children', { params })
}

export function getChildrenById(kind: string, parentId?: number | null): Promise<SysDict[]> {
  const params: Record<string, any> = { kind }
  if (parentId !== undefined && parentId !== null) {
    params.parentId = parentId
  }
  return request.get('/v1/dict/children-by-id', { params })
}

export function getDictById(id: number): Promise<SysDict> {
  return request.get(`/v1/dict/${id}`)
}

export function createDict(data: DictDataDTO): Promise<void> {
  return request.post('/v1/dict', data)
}

export function updateDict(id: number, data: DictDataDTO): Promise<void> {
  return request.put(`/v1/dict/${id}`, data)
}

export function deleteDict(id: number): Promise<void> {
  return request.delete(`/v1/dict/${id}`)
}

// ==================== 导入导出 ====================

export interface ImportResult {
  successCount: number
  skipCount: number
  errors: string[]
}

export interface ExportDTO {
  kind: string
  code: string
  detail: string
  alias: string | null
  description: string | null
  parentCode: string | null
  level: number
  sort: number
  leaf: number
  status: number
}

export function importDict(kind: string, file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('kind', kind)
  formData.append('file', file)
  return request.post('/v1/dict/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportDict(kind?: string, keyword?: string): Promise<ExportDTO[]> {
  const params: Record<string, string> = {}
  if (kind) params.kind = kind
  if (keyword) params.keyword = keyword
  return request.get('/v1/dict/export', { params })
}

export function getImportTemplate(): Promise<ExportDTO[]> {
  return request.get('/v1/dict/template')
}

// ==================== 兼容旧接口 ====================

export function getDictByKind(kind: string): Promise<SysDict[]> {
  return request.get(`/v1/dict/type/${kind}`)
}

export function getKbCategoryList(status?: number): Promise<SysDict[]> {
  const params: Record<string, any> = {}
  if (status !== undefined) params.status = status
  return request.get('/v1/dict/kb-category/tree', { params })
}

export function getOriginScopeList(status?: number): Promise<SysDict[]> {
  const params: Record<string, any> = {}
  if (status !== undefined) params.status = status
  return request.get('/v1/dict/origin-scope', { params })
}

export function getOriginDepartmentList(status?: number): Promise<SysDict[]> {
  const params: Record<string, any> = {}
  if (status !== undefined) params.status = status
  return request.get('/v1/dict/origin-department/tree', { params })
}
