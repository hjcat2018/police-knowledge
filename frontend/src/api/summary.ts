import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface SummaryRequest {
  content: string
}

export interface SummaryResponse {
  status: 'pending' | 'processing' | 'completed' | 'failed'
  chunks: number
  length: number
  summary: string
  docId: number
  current?: number
  total?: number
  updatedTime?: string
}

export interface BatchSummaryRequest {
  docIds: number[]
}

export function generateSummarySync(content: string) {
  return request.post<Result<string>>('/api/v1/summary/sync', { content })
}

export function generateSummaryAsync(docId: number) {
  return request.post<Result<{ docId: number; status: string; message: string }>>(
    `/api/v1/summary/async/${docId}`
  )
}

export function getSummaryStatus(docId: number) {
  return request.get<Result<SummaryResponse>>(`/api/v1/summary/status/${docId}`)
}

export function regenerateSummary(docId: number) {
  return request.post<Result<any>>(`/api/v1/summary/regenerate/${docId}`)
}

export function batchGenerateSummary(docIds: number[]) {
  return request.post<Result<any>>('/api/v1/summary/batch', { docIds })
}
