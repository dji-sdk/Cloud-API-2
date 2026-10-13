<!--
  Copyright 2025-2026 DJI. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import type { ChatMessage } from '@/types/chat'

/** markdown-it 实例，启用安全模式（不允许原始 HTML） */
const md = new MarkdownIt({ html: false, linkify: true, typographer: true })

interface Props {
  message: ChatMessage
}

interface Emits {
  (e: 'confirm', confirmId: string, approved: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

/** 是否为用户消息 */
const isUser = computed<boolean>(() => props.message.role === 'user')

/** 将 assistant 消息内容渲染为 HTML */
const renderedContent = computed<string>(() => {
  if (isUser.value) {
    return ''
  }
  return md.render(props.message.content || '')
})

/** 是否显示工具执行状态提示（工具执行期间有 statusText 时显示，token 到达后自动清除） */
const showStatus = computed<boolean>(() =>
  !isUser.value && !!props.message.statusText,
)

/** 是否需要显示确认按钮（待处理状态） */
const showConfirmButtons = computed<boolean>(() => {
  const confirm = props.message.confirmRequired
  return confirm !== undefined && confirm.status === 'pending'
})

/** 确认状态文字 */
const confirmStatusText = computed<string>(() => {
  const status = props.message.confirmRequired?.status
  if (status === 'approved') {
    return '已批准'
  }
  if (status === 'rejected') {
    return '已拒绝'
  }
  return ''
})

/** 处理批准 */
function handleApprove(): void {
  const confirmId = props.message.confirmRequired?.confirmId
  if (confirmId) {
    emit('confirm', confirmId, true)
  }
}

/** 处理拒绝 */
function handleReject(): void {
  const confirmId = props.message.confirmRequired?.confirmId
  if (confirmId) {
    emit('confirm', confirmId, false)
  }
}
</script>

<template>
  <div class="chat-message" :class="isUser ? 'chat-message--user' : 'chat-message--assistant'">
    <!-- 用户消息：纯文本，右对齐 -->
    <div v-if="isUser" class="chat-message__bubble chat-message__bubble--user">
      {{ message.content }}
    </div>

    <!-- AI 消息：markdown 渲染，左对齐 -->
    <div v-else class="chat-message__bubble chat-message__bubble--assistant">
      <!-- 工具执行状态提示：工具调用期间显示，有内容后消失 -->
      <div v-if="showStatus" class="chat-message__status">
        <span class="chat-message__status-dot" />
        <span class="chat-message__status-text">{{ message.statusText }}…</span>
      </div>

      <!-- eslint-disable-next-line vue/no-v-html -->
      <div v-if="renderedContent" class="chat-message__markdown" v-html="renderedContent" />

      <!-- Tool 确认区域 -->
      <div v-if="message.confirmRequired" class="chat-message__confirm">
        <div class="chat-message__confirm-info">
          <span class="chat-message__confirm-tool">{{ message.confirmRequired.toolName }}</span>
          <span class="chat-message__confirm-desc">{{ message.confirmRequired.description }}</span>
        </div>

        <!-- 待处理：显示确认/拒绝按钮 -->
        <div v-if="showConfirmButtons" class="chat-message__confirm-actions">
          <button class="chat-message__btn chat-message__btn--approve" @click="handleApprove">
            批准
          </button>
          <button class="chat-message__btn chat-message__btn--reject" @click="handleReject">
            拒绝
          </button>
        </div>

        <!-- 已处理：显示状态文字 -->
        <span
          v-else-if="confirmStatusText"
          class="chat-message__confirm-status"
          :class="message.confirmRequired.status === 'approved'
            ? 'chat-message__confirm-status--approved'
            : 'chat-message__confirm-status--rejected'"
        >
          {{ confirmStatusText }}
        </span>
      </div>
    </div>

    <!-- 时间戳 -->
    <time class="chat-message__time">
      {{ new Date(message.timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}
    </time>
  </div>
</template>

<style scoped>
.chat-message {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

/* 用户消息靠右 */
.chat-message--user {
  align-items: flex-end;
}

/* AI 消息靠左 */
.chat-message--assistant {
  align-items: flex-start;
}

/* 消息气泡基础样式 */
.chat-message__bubble {
  max-width: 88%;
  border-radius: 14px;
  padding: 12px 16px;
  font-size: 13.5px;
  line-height: 1.65;
  word-break: break-word;
}

/* 用户气泡：紫色调 */
.chat-message__bubble--user {
  background: rgba(124, 58, 237, 0.15);
  color: var(--app-text, #e6edf3);
  border-bottom-right-radius: 4px;
}

/* AI 气泡：深色卡片 */
.chat-message__bubble--assistant {
  background: var(--app-bg-card, #1c2333);
  color: var(--app-text, #e6edf3);
  border-bottom-left-radius: 4px;
  border: 1px solid var(--app-border, #30363d);
}

/* markdown 内容重置 */
.chat-message__markdown :deep(p) {
  margin: 0 0 8px;
}

.chat-message__markdown :deep(p:last-child) {
  margin-bottom: 0;
}

.chat-message__markdown :deep(ul),
.chat-message__markdown :deep(ol) {
  padding-left: 20px;
  margin: 4px 0;
}

.chat-message__markdown :deep(code) {
  background: rgba(0, 0, 0, 0.25);
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.chat-message__markdown :deep(pre) {
  background: rgba(0, 0, 0, 0.35);
  border-radius: 6px;
  padding: 10px 12px;
  overflow-x: auto;
  margin: 6px 0;
}

.chat-message__markdown :deep(pre code) {
  background: none;
  padding: 0;
}

.chat-message__markdown :deep(blockquote) {
  border-left: 3px solid var(--app-primary, #1a73e8);
  padding-left: 10px;
  margin: 4px 0;
  color: var(--app-text-secondary, #8b949e);
}

/* Tool 确认区域 */
.chat-message__confirm {
  margin-top: 10px;
  padding: 10px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  border: 1px solid var(--app-border, #30363d);
}

.chat-message__confirm-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.chat-message__confirm-tool {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-primary, #1a73e8);
  font-family: monospace;
}

.chat-message__confirm-desc {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
  line-height: 1.5;
}

.chat-message__confirm-actions {
  display: flex;
  gap: 8px;
}

/* 确认/拒绝按钮 */
.chat-message__btn {
  padding: 5px 14px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}

.chat-message__btn:hover {
  opacity: 0.85;
}

.chat-message__btn--approve {
  background: var(--app-success, #3fb950);
  color: #fff;
}

.chat-message__btn--reject {
  background: var(--app-bg-card-hover, #242d3d);
  color: var(--app-text, #e6edf3);
  border: 1px solid var(--app-border, #30363d);
}

/* 状态文字 */
.chat-message__confirm-status {
  font-size: 12px;
  font-weight: 500;
}

.chat-message__confirm-status--approved {
  color: var(--app-success, #3fb950);
}

.chat-message__confirm-status--rejected {
  color: var(--app-danger, #f85149);
}

/* 时间戳 */
.chat-message__time {
  font-size: 11px;
  color: var(--app-text-muted, #848d97);
}

/* 工具执行状态提示 */
.chat-message__status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0 6px;
}

.chat-message__status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--app-primary, #1a73e8);
  flex-shrink: 0;
  animation: status-pulse 1.2s ease-in-out infinite;
}

.chat-message__status-text {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
}

@keyframes status-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.4; transform: scale(0.85); }
}
</style>
