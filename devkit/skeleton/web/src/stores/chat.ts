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

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatMessage, ToolConfirmation } from '@/types/chat'

/** 聊天状态 store。messages 和 isStreaming 不持久化；isOpen 和 drawerWidth 持久化到 localStorage。 */
export const useChatStore = defineStore('chat', () => {
  /** 聊天消息列表（不持久化，刷新后清空） */
  const messages = ref<ChatMessage[]>([])

  /** 抽屉是否展开（持久化，记住用户偏好） */
  const isOpen = ref<boolean>(false)

  /** 抽屉宽度，单位 px（持久化，记住用户拖拽调整的宽度） */
  const drawerWidth = ref<number>(380)

  /** 是否正在流式响应（不持久化） */
  const isStreaming = ref<boolean>(false)

  /** 是否存在待确认的 Tool 调用 */
  const hasPendingConfirmation = computed<boolean>(() =>
    messages.value.some(
      (msg) => msg.confirmRequired?.status === 'pending',
    ),
  )

  /**
   * 追加一条新消息。
   * @param msg 消息对象
   */
  function addMessage(msg: ChatMessage): void {
    messages.value.push(msg)
  }

  /**
   * 向最后一条 assistant 消息追加 token 片段。
   * 若最后一条不是 assistant 消息，则创建一条新的。
   * 有内容到达时同时清除 statusText（工具执行结束，开始输出正文）。
   * @param token 文本片段
   * @param msgId 用于首次创建时的消息 id
   */
  function appendToLast(token: string, msgId: string): void {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.content += token
      if (token) {
        last.statusText = undefined
      }
    } else {
      messages.value.push({
        id: msgId,
        role: 'assistant',
        content: token,
        timestamp: Date.now(),
      })
    }
  }

  /**
   * 设置最后一条 assistant 消息的工具执行状态提示。
   * 若最后一条不是 assistant 消息，则先创建占位消息。
   * @param text 工具操作描述（用户可见，如"查询设备列表"）
   * @param msgId 用于首次创建时的消息 id
   */
  function setLastStatus(text: string, msgId: string): void {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.statusText = text
    } else {
      messages.value.push({
        id: msgId,
        role: 'assistant',
        content: '',
        timestamp: Date.now(),
        statusText: text,
      })
    }
  }

  /**
   * 更新指定消息的确认状态。
   * @param confirmId 确认 id
   * @param status 新状态
   */
  function updateConfirmation(
    confirmId: string,
    status: ToolConfirmation['status'],
  ): void {
    for (const msg of messages.value) {
      if (msg.confirmRequired?.confirmId === confirmId) {
        msg.confirmRequired.status = status
        break
      }
    }
  }

  /** 清空所有消息。 */
  function clearMessages(): void {
    messages.value = []
  }

  /** 切换抽屉展开/收起状态。 */
  function toggleDrawer(): void {
    isOpen.value = !isOpen.value
  }

  /** 快捷建议列表（空对话时在欢迎页展示） */
  const quickPrompts = ref<string[]>([
    '查询在线设备列表',
    '查一下今天的告警统计',
    '帮我查一下航线库有哪些航线',
    '查看最近的媒体文件',
  ])

  return {
    messages,
    isOpen,
    drawerWidth,
    isStreaming,
    hasPendingConfirmation,
    quickPrompts,
    addMessage,
    appendToLast,
    setLastStatus,
    updateConfirmation,
    clearMessages,
    toggleDrawer,
  }
}, {
  persist: {
    // 仅持久化用户偏好；消息列表和流式状态不写入 localStorage
    pick: ['isOpen', 'drawerWidth'],
  },
})
