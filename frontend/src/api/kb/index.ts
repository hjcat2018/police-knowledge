import request from '@/utils/request'
import { PageResult } from '@/types/api'

export interface KnowledgeBase {
  id: number
  name: string
  code: string
  description: string
  cover: string
  categoryId: number
  categoryName: string
  status: number
  isPublic: number
  viewCount: number
  favoriteCount: number
  docCount: number
  createdTime: string
  createdBy: string
  updatedTime: string
}

export function getKnowledgeBaseList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
}): Promise<PageResult<KnowledgeBase>> {
  return request.get('/v1/kb', { params })
}

export function getAllKnowledgeBases(): Promise<KnowledgeBase[]> {
  return request.get('/v1/kb/list')
}

export function getKnowledgeBaseById(id: number): Promise<KnowledgeBase> {
  return request.get(`/v1/kb/${id}`)
}

export function createKnowledgeBase(data: Partial<KnowledgeBase>): Promise<KnowledgeBase> {
  return request.post('/v1/kb', data)
}

export function updateKnowledgeBase(id: number, data: Partial<KnowledgeBase>): Promise<KnowledgeBase> {
  return request.put(`/v1/kb/${id}`, data)
}

export function deleteKnowledgeBase(id: number): Promise<void> {
  return request.delete(`/v1/kb/${id}`)
}

export function batchDeleteKnowledgeBases(ids: number[]): Promise<void> {
  return request.delete('/v1/kb/batch', { data: ids })
}

export function changeKnowledgeBaseStatus(id: number, status: number): Promise<void> {
  return request.put(`/v1/kb/${id}/status`, null, { params: { status } })
}
