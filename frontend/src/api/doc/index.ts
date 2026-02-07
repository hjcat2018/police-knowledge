import request from '@/utils/request'
import { PageResult } from '@/types/api'

export interface Document {
  id: number
  title: string
  type?: string
  typeName?: string
  summary?: string
  content?: string
  kbId?: number
  kbName?: string
  categoryId?: number
  categoryName?: string
  tags?: string
  status?: number
  statusName?: string
  cover?: string
  source?: string
  author?: string
  originScope?: string
  originDepartment?: string
  viewCount?: number
  favoriteCount?: number
  isTop?: number
  isHot?: number
  publishTime?: string
  sourceTime?: string
  docUrl?: string
  createdTime?: string
  updatedTime?: string
}

export interface DocumentParams {
  pageNum?: number
  pageSize?: number
  kbId?: number
  keyword?: string
  originScope?: string | null
  originDepartment?: string | null
}

export function getDocumentList(params: DocumentParams): Promise<PageResult<Document>> {
  return request.get('/v1/documents', { params })
}

export function getDocumentById(id: number): Promise<Document> {
  return request.get(`/v1/documents/${id}`)
}

export function createDocument(data: Partial<Document>): Promise<Document> {
  return request.post('/v1/documents', data)
}

export function updateDocument(id: number, data: Partial<Document>): Promise<Document> {
  return request.put(`/v1/documents/${id}`, data)
}

export function deleteDocument(id: number): Promise<void> {
  return request.delete(`/v1/documents/${id}`)
}

export function batchDeleteDocuments(ids: number[]): Promise<void> {
  return request.delete('/v1/documents/batch', { data: ids })
}

export function changeDocumentStatus(id: number, status: number): Promise<void> {
  return request.put(`/v1/documents/${id}/status`, null, { params: { status } })
}

export function getHotDocuments(): Promise<Document[]> {
  return request.get('/v1/documents/hot')
}

export function getRecentDocuments(): Promise<Document[]> {
  return request.get('/v1/documents/recent')
}

export function viewDocument(id: number): Promise<void> {
  return request.post(`/v1/documents/${id}/view`)
}

export function parseFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/v1/documents/parse-file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function createDocumentWithFile(data: {
  title: string
  summary?: string
  kbId: number
  categoryId?: number
  tags?: string
  file?: File
  content?: string
}): Promise<Document> {
  const formData = new FormData()
  formData.append('title', data.title)
  formData.append('kbId', String(data.kbId))
  if (data.summary) formData.append('summary', data.summary)
  if (data.categoryId) formData.append('categoryId', String(data.categoryId))
  if (data.tags) formData.append('tags', data.tags)
  if (data.file) formData.append('file', data.file)
  if (data.content) formData.append('content', data.content)
  
  return request.post('/v1/documents/create-with-file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
