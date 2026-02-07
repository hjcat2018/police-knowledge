class SummaryWebSocket {
  private ws: WebSocket | null = null
  private onProgressCallback: ((current: number, total: number) => void) | null = null
  private onCompleteCallback: ((summary: string) => void) | null = null
  private onErrorCallback: ((error: string) => void) | null = null

  connect(docId: number) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    this.ws = new WebSocket(`${protocol}//${host}/ws/summary/progress/${docId}`)

    this.ws.onopen = () => {
      console.log('摘要进度WebSocket已连接')
    }

    this.ws.onmessage = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'progress' && this.onProgressCallback) {
          this.onProgressCallback(data.current, data.total)
        } else if (data.type === 'complete' && this.onCompleteCallback) {
          this.onCompleteCallback(data.summary)
          this.close()
        } else if (data.type === 'error' && this.onErrorCallback) {
          this.onErrorCallback(data.message)
          this.close()
        }
      } catch (e) {
        console.error('WebSocket消息解析失败:', e)
      }
    }

    this.ws.onerror = (error: Event) => {
      console.error('WebSocket错误:', error)
      this.onErrorCallback?.('WebSocket连接失败')
    }

    this.ws.onclose = () => {
      console.log('WebSocket连接已关闭')
    }

    return this
  }

  onProgress(callback: (current: number, total: number) => void) {
    this.onProgressCallback = callback
    return this
  }

  onComplete(callback: (summary: string) => void) {
    this.onCompleteCallback = callback
    return this
  }

  onError(callback: (error: string) => void) {
    this.onErrorCallback = callback
    return this
  }

  close() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.onProgressCallback = null
    this.onCompleteCallback = null
    this.onErrorCallback = null
  }

  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }
}

export const summaryWS = new SummaryWebSocket()
