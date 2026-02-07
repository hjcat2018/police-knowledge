import request from '@/utils/request'

export interface ChatFile {
  name: string
  size: string
  type: string
  url?: string
  content?: string
  uploading?: boolean
  uploaded?: boolean
}

export interface ChatMessage {
  id?: number
  conversationId: number
  role: 'user' | 'assistant'
  content: string
  references?: string
  createdTime?: string
  tokenCount?: number
  kbId?: number
  modelName?: string
  modelId?: string
  files?: ChatFile[]
}

export interface Conversation {
  id: number
  title: string
  mode?: string
  createdTime: string
  updatedTime: string
}

export function createConversation(data?: { title?: string; mode?: string }) {
  return request.post<Conversation>('/v1/conversations', data)
}

export function listConversations(params: { page?: number; size?: number; mode?: string }) {
  return request.get<{ records: Conversation[]; total: number }>('/v1/conversations', { params })
}

export function getConversation(id: number) {
  return request.get<Conversation>(`/v1/conversations/${id}`)
}

export function getMessages(conversationId: number) {
  return request.get<ChatMessage[]>(`/v1/conversations/${conversationId}/messages`)
}

export function sendMessage(conversationId: number, data: { content: string; kbId?: number; topK?: number }) {
  return request.post<ChatMessage>(`/v1/conversations/${conversationId}/messages`, data)
}

export function deleteConversation(id: number) {
  return request.delete(`/v1/conversations/${id}`)
}

export interface UploadResult {
  docUrl: string
  sourceTime: string
  content: string
}

export function uploadFile(file: File): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/v1/documents/parse-and-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
