import request from '@/utils/request'

export interface PromptTemplate {
  id: number
  name: string
  content: string
  variables: string
  description: string
  isDefault: number
  isSystem: number
  sort: number
  createdBy?: number
  createdTime?: string
  updatedBy?: number
  updatedTime?: string
}

export interface CreateTemplateRequest {
  name: string
  content: string
  variables?: string
  description?: string
  sort?: number
}

export enum TemplateType {
  SYSTEM = 'system',
  MY = 'my',
  SHARED = 'shared'
}

export function getTemplates(type: TemplateType = TemplateType.MY) {
  return request.get<PromptTemplate[]>('/v1/templates', {
    params: { type }
  })
}

export function getTemplate(id: number) {
  return request.get<PromptTemplate>(`/v1/templates/${id}`)
}

export function createTemplate(data: CreateTemplateRequest, isSystem: 0 | 1 | 2 = 0) {
  return request.post<number>('/v1/templates', { ...data, isSystem })
}

export function createSharedTemplate(data: CreateTemplateRequest) {
  return request.post<number>('/v1/templates', { ...data, isSystem: 2 })
}

export function updateTemplate(id: number, data: CreateTemplateRequest) {
  return request.put(`/v1/templates/${id}`, data)
}

export function deleteTemplate(id: number) {
  return request.delete(`/v1/templates/${id}`)
}

export function setDefault(id: number) {
  return request.put(`/v1/templates/${id}/default`)
}
