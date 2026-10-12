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

/** Tool 调用信息 */
export interface ToolCallInfo {
  toolName: string
  args: Record<string, unknown>
  result?: string
}

/** Tool 确认请求 */
export interface ToolConfirmation {
  confirmId: string
  toolName: string
  description: string
  status: 'pending' | 'approved' | 'rejected'
}

/** 单条聊天消息 */
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  toolCalls?: ToolCallInfo[]
  confirmRequired?: ToolConfirmation
  /** 工具执行中的状态提示（工具调用完成后清除） */
  statusText?: string
}

/** SSE 事件类型 */
export type ChatSseEventType = 'token' | 'status' | 'tool_call' | 'tool_result' | 'confirm_required' | 'error' | 'done' | 'navigation'

/** SSE 事件 */
export interface ChatSseEvent {
  type: ChatSseEventType
  data: Record<string, unknown>
}

/** 导航事件数据（type=navigation） */
export interface NavigationEventData {
  path: string
  query?: Record<string, string>
}

/** 历史消息（发送给后端） */
export interface HistoryMessage {
  role: 'user' | 'assistant'
  content: string
}
