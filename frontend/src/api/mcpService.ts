import request from '@/utils/request'

export interface McpService {
  id: number
  name: string
  code: string
  description: string
  enabled: number
  configUrl: string
  configAuthType: string
  configCredentials: string
  configTimeout: number
  configMethod: string
  sort: number
  command: string
  args: string
  env: string
  createdTime?: string
  updatedTime?: string
}

export interface CreateMcpServiceRequest {
  name: string
  code: string
  description?: string
  enabled?: number
  configUrl?: string
  configAuthType?: string
  configCredentials?: string
  configTimeout?: number
  configMethod?: string
  sort?: number
  command?: string
  args?: string
  env?: string
}

export function getMcpServices() {
  return request.get<McpService[]>('/v1/mcp/services')
}

export function getMcpService(id: number) {
  return request.get<McpService>(`/v1/mcp/services/${id}`)
}

export function createMcpService(data: CreateMcpServiceRequest) {
  return request.post<number>('/v1/mcp/services', data)
}

export function updateMcpService(id: number, data: CreateMcpServiceRequest) {
  return request.put(`/v1/mcp/services/${id}`, data)
}

export function deleteMcpService(id: number) {
  return request.delete(`/v1/mcp/services/${id}`)
}

export function toggleMcpService(id: number) {
  return request.put(`/v1/mcp/services/${id}/toggle`)
}
