/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { useChatStore } from '@/stores/chat'
import { useToastStore } from '@/stores/toast'
import { authFetch } from '@/api/auth'
import { API_BASE } from '@/api/baseUrl'
import type { ChatMessage, ChatSseEvent, HistoryMessage, NavigationEventData } from '@/types/chat'
import { useRouter } from 'vue-router'

/** 生成简单的唯一 id（基于时间戳 + 随机后缀）。 */
function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

/** 将 store 中的 messages 转换为发送给后端的历史格式（过滤 system 消息）。 */
function toHistoryMessages(messages: ChatMessage[]): HistoryMessage[] {
  return messages
    .filter((m): m is ChatMessage & { role: 'user' | 'assistant' } =>
      m.role === 'user' || m.role === 'assistant',
    )
    .map((m) => ({ role: m.role, content: m.content }))
}

/**
 * 聊天功能 composable。
 * 封装 SSE 流式请求、Tool 确认以及消息清空，供组件直接调用。
 */
export function useChat() {
  const chatStore = useChatStore()
  const toastStore = useToastStore()
  const router = useRouter()

  /**
   * 发送一条用户消息，通过 SSE 接收 AI 流式回复。
   * @param content 用户输入内容
   */
  async function sendMessage(content: string): Promise<void> {
    if (chatStore.isStreaming || !content.trim()) {
      return
    }

    // 1. 先将用户消息写入 store
    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content,
      timestamp: Date.now(),
    }
    chatStore.addMessage(userMsg)
    chatStore.isStreaming = true

    // 2. 构建历史消息（不含刚追加的用户消息，避免重复；后端会把 content 拼到末尾）
    const history = toHistoryMessages(chatStore.messages.slice(0, -1))

    try {
      const response = await authFetch(`${API_BASE}/api/v1/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: content, history }),
      })

      if (!response.ok || !response.body) {
        chatStore.addMessage({
          id: generateId(),
          role: 'assistant',
          content: `请求失败：${response.status} ${response.statusText}`,
          timestamp: Date.now(),
        })
        return
      }

      await parseSseStream(response.body)
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err)
      chatStore.addMessage({
        id: generateId(),
        role: 'assistant',
        content: `网络错误：${errorMsg}`,
        timestamp: Date.now(),
      })
    } finally {
      chatStore.isStreaming = false
    }
  }

  /**
   * 解析 SSE 响应体，将事件分发到 store。
   * @param body 响应体 ReadableStream
   */
  async function parseSseStream(body: ReadableStream<Uint8Array>): Promise<void> {
    const reader = body.getReader()
    const decoder = new TextDecoder()
    // 用于首次创建 assistant 消息时的固定 id（同一次回复复用同一个 id）
    const assistantMsgId = generateId()
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        // 最后一段可能不完整，留到下次处理
        buffer = lines.pop() ?? ''

        let currentEventName = ''
        for (const line of lines) {
          const trimmed = line.trim()

          if (trimmed.startsWith('event:')) {
            currentEventName = trimmed.slice('event:'.length).trim()
            continue
          }

          if (!trimmed.startsWith('data:')) {
            if (trimmed === '') {
              currentEventName = ''
            }
            continue
          }

          const jsonStr = trimmed.slice('data:'.length).trim()
          if (!jsonStr) {
            continue
          }

          let event: ChatSseEvent
          try {
            const parsed = JSON.parse(jsonStr) as Record<string, unknown>
            event = {
              type: (currentEventName || parsed['type'] || 'token') as ChatSseEvent['type'],
              data: parsed,
            }
          } catch {
            continue
          }

          currentEventName = ''
          handleSseEvent(event, assistantMsgId)

          if (event.type === 'done' || event.type === 'error') {
            return
          }
        }
      }
    } finally {
      reader.releaseLock()
    }
  }

  /**
   * 根据 SSE 事件类型更新 store。
   * @param event SSE 事件
   * @param assistantMsgId 当前 assistant 消息的 id
   */
  function handleSseEvent(event: ChatSseEvent, assistantMsgId: string): void {
    switch (event.type) {
      case 'token': {
        const token = typeof event.data['content'] === 'string' ? event.data['content'] : ''
        if (token) {
          chatStore.appendToLast(token, assistantMsgId)
        }
        break
      }
      case 'status': {
        const text = typeof event.data['text'] === 'string' ? event.data['text'] : ''
        if (text) {
          chatStore.setLastStatus(text, assistantMsgId)
        }
        break
      }
      case 'confirm_required': {
        const confirmId = String(event.data['confirmId'] ?? '')
        const toolName = String(event.data['toolName'] ?? '')
        const description = String(event.data['description'] ?? '')
        // 将确认请求附加到最后一条 assistant 消息（若不存在则先创建占位消息）
        chatStore.appendToLast('', assistantMsgId)
        const last = chatStore.messages[chatStore.messages.length - 1]
        if (last) {
          last.confirmRequired = {
            confirmId,
            toolName,
            description,
            status: 'pending',
          }
        }
        break
      }
      case 'error': {
        const msg = typeof event.data['message'] === 'string'
          ? event.data['message']
          : '发生未知错误'
        chatStore.addMessage({
          id: generateId(),
          role: 'assistant',
          content: `错误：${msg}`,
          timestamp: Date.now(),
        })
        break
      }
      case 'done':
      case 'tool_call':
      case 'tool_result':
        // tool_call / tool_result 当前仅记录，不展示在消息流中
        break
      case 'navigation': {
        const nav = event.data as NavigationEventData
        if (!nav?.path) {
          break
        }
        toastStore.show('起飞指令已下达', 'success', 8000, {
          label: '前往驾驶舱',
          onClick: () => router.push({ path: nav.path, query: nav.query }),
        })
        break
      }
    }
  }

  /**
   * 提交 Tool 确认结果（批准或拒绝）。
   * @param confirmId 确认 id
   * @param approved 是否批准
   */
  async function confirmTool(confirmId: string, approved: boolean): Promise<void> {
    // 乐观更新 UI 状态
    chatStore.updateConfirmation(confirmId, approved ? 'approved' : 'rejected')

    try {
      const response = await authFetch(`${API_BASE}/api/v1/chat/confirm/${confirmId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ approved }),
      })

      if (!response.ok) {
        // 服务端拒绝时回滚为 pending，让用户重试
        chatStore.updateConfirmation(confirmId, 'pending')
      }
    } catch {
      // 网络错误同样回滚
      chatStore.updateConfirmation(confirmId, 'pending')
    }
  }

  /** 清空所有聊天消息，委托给 store。 */
  function clearMessages(): void {
    chatStore.clearMessages()
  }

  return {
    sendMessage,
    confirmTool,
    clearMessages,
  }
}
