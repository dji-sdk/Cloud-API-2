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
import { ref, onMounted, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useChat } from '@/composables/useChat'
import ChatMessageList from './ChatMessageList.vue'
import ChatInput from './ChatInput.vue'

const chatStore = useChatStore()
const { sendMessage, confirmTool, clearMessages } = useChat()

/** 是否正在拖拽调整宽度 */
const isDragging = ref<boolean>(false)

/** 拖拽开始时的鼠标 X 坐标 */
const dragStartX = ref<number>(0)

/** 拖拽开始时的抽屉宽度 */
const dragStartWidth = ref<number>(0)

/** 最小宽度 */
const MIN_WIDTH = 320

/** 最大宽度 */
const MAX_WIDTH = 600

/** 开始拖拽 */
function startDrag(event: MouseEvent): void {
  isDragging.value = true
  dragStartX.value = event.clientX
  dragStartWidth.value = chatStore.drawerWidth
  event.preventDefault()
}

/** 拖拽移动 */
function onMouseMove(event: MouseEvent): void {
  if (!isDragging.value) {
    return
  }
  const delta = dragStartX.value - event.clientX
  const newWidth = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, dragStartWidth.value + delta))
  chatStore.drawerWidth = newWidth
}

/** 结束拖拽 */
function stopDrag(): void {
  isDragging.value = false
}

/** 处理 Tool 确认 */
async function handleConfirm(confirmId: string, approved: boolean): Promise<void> {
  await confirmTool(confirmId, approved)
}

/** 处理发送消息 */
async function handleSend(content: string): Promise<void> {
  await sendMessage(content)
}

/** 处理快捷建议点击 */
async function handleQuickPrompt(prompt: string): Promise<void> {
  await sendMessage(prompt)
}

/** 处理清空消息 */
function handleClear(): void {
  clearMessages()
}

/**
 * 处理全局快捷键 Ctrl+/ (Windows/Linux) 或 Cmd+/ (macOS)。
 */
function handleKeydown(event: KeyboardEvent): void {
  if (event.key === '/' && (event.ctrlKey || event.metaKey)) {
    event.preventDefault()
    chatStore.toggleDrawer()
  }
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', stopDrag)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', stopDrag)
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <!-- 抽屉主体 -->
  <Transition name="drawer">
    <div
      v-if="chatStore.isOpen"
      class="chat-drawer"
      :style="{ width: `${chatStore.drawerWidth}px` }"
    >
      <!-- 左侧拖拽手柄 -->
      <div
        class="chat-drawer__resize-handle"
        :class="{ 'chat-drawer__resize-handle--dragging': isDragging }"
        @mousedown="startDrag"
      />

      <!-- 抽屉头部 -->
      <div class="chat-drawer__header">
        <div class="chat-drawer__header-left">
          <span class="chat-drawer__status-dot" />
          <span class="chat-drawer__title">Copilot</span>
          <span class="chat-drawer__badge">意图驱动</span>
        </div>
        <div class="chat-drawer__header-actions">
          <button
            class="chat-drawer__icon-btn"
            title="清空对话"
            @click="handleClear"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
            </svg>
          </button>
          <button
            class="chat-drawer__icon-btn"
            title="关闭 (Ctrl+/)"
            @click="chatStore.toggleDrawer"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <ChatMessageList v-if="chatStore.messages.length > 0" @confirm="handleConfirm" />

      <!-- 欢迎页（仅在无消息时显示） -->
      <div v-else class="chat-drawer__welcome">
        <div class="welcome-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="11.5" r="1" fill="currentColor"/>
            <circle cx="8" cy="11.5" r="1" fill="currentColor"/>
            <circle cx="16" cy="11.5" r="1" fill="currentColor"/>
          </svg>
        </div>
        <h3 class="welcome-title">你好，我是 CloudAPI2 平台 AI 助手</h3>
        <p class="welcome-desc">我可以帮你管理无人机设备、执行飞行任务和查询媒体文件</p>
        <div class="chat-drawer__quick-prompts">
          <button
            v-for="prompt in chatStore.quickPrompts"
            :key="prompt"
            class="chat-drawer__chip"
            @click="handleQuickPrompt(prompt)"
          >
            {{ prompt }}
          </button>
        </div>
      </div>

      <!-- 快捷建议（对话中时不显示，已移入欢迎页） -->

      <!-- 输入框 -->
      <ChatInput :disabled="chatStore.isStreaming" @send="handleSend" />
    </div>
  </Transition>
</template>

<style scoped>
/* 抽屉容器 — 覆盖式，浮在主内容上方 */
.chat-drawer {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--app-bg-panel, #161b22);
  border-left: 1px solid var(--app-border, #30363d);
  position: fixed;
  top: 0;
  right: 0;
  z-index: 1000;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

/* 抽屉滑入动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: transform 0.25s ease, opacity 0.2s ease;
  overflow: hidden;
}

.drawer-enter-from,
.drawer-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 拖拽手柄 */
.chat-drawer__resize-handle {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  background: transparent;
  transition: background 0.15s;
  z-index: 10;
}

.chat-drawer__resize-handle:hover,
.chat-drawer__resize-handle--dragging {
  background: #7c3aed;
}

/* 头部 */
.chat-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 16px;
  border-bottom: 1px solid var(--app-border, #30363d);
  flex-shrink: 0;
}

.chat-drawer__header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-drawer__status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
}

.chat-drawer__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-text, #e6edf3);
}

.chat-drawer__badge {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(124, 58, 237, 0.2);
  color: #a78bfa;
}

.chat-drawer__header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 图标按钮 */
.chat-drawer__icon-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--app-text-secondary, #8b949e);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}

.chat-drawer__icon-btn:hover {
  background: var(--app-bg-card-hover, #242d3d);
  color: var(--app-text, #e6edf3);
}

/* 欢迎页 */
.chat-drawer__welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  text-align: center;
  gap: 12px;
}

.welcome-icon {
  margin-bottom: 8px;
  color: var(--app-primary);
}

.welcome-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text, #e6edf3);
  margin: 0;
}

.welcome-desc {
  font-size: 14px;
  color: var(--app-text-secondary, #8b949e);
  margin: 0 0 12px 0;
  line-height: 1.5;
}

/* 快捷建议 chips */
.chat-drawer__quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  flex-shrink: 0;
}

.chat-drawer__chip {
  padding: 6px 14px;
  border-radius: 16px;
  border: 1px solid var(--app-border, #30363d);
  background: var(--app-bg-card, #1c2333);
  color: var(--app-text-secondary, #8b949e);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.chat-drawer__chip:hover {
  background: rgba(124, 58, 237, 0.1);
  color: #a78bfa;
  border-color: rgba(124, 58, 237, 0.3);
}
</style>
