import request from '@/utils/request'
import type { OriginScope } from '@/types/api'

export interface SearchResult {
  documentId: number
  title: string
  summary: string
  content: string
  score: number
  kbName?: string
}

export interface SearchParams {
  keyword: string
  kbId?: number | null
  categoryId?: number | null
  originScope?: OriginScope | null
  originDepartment?: string | null
  topK?: number
}

export function semanticSearch(params: SearchParams): Promise<SearchResult[]> {
  return request.post('/v1/search/semantic', params)
}

export function hybridSearch(params: SearchParams): Promise<SearchResult[]> {
  return request.post('/v1/search/hybrid', params)
}
