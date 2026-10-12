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
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatMessage from './ChatMessage.vue'

interface Emits {
  (e: 'confirm', confirmId: string, approved: boolean): void
}

const emit = defineEmits<Emits>()

const chatStore = useChatStore()

/** 消息列表容器 DOM 引用 */
const listRef = ref<HTMLElement | null>(null)

/** 滚动到消息列表底部 */
async function scrollToBottom(): Promise<void> {
  await nextTick()
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

/** 监听消息变化，自动滚动到底部 */
watch(
  () => chatStore.messages.length,
  () => {
    scrollToBottom()
  },
)

/** 监听最后一条消息的内容变化（流式追加时），同步滚动 */
watch(
  () => {
    const last = chatStore.messages[chatStore.messages.length - 1]
    return last?.content ?? ''
  },
  () => {
    scrollToBottom()
  },
)

/** 转发 confirm 事件到父组件 */
function handleConfirm(confirmId: string, approved: boolean): void {
  emit('confirm', confirmId, approved)
}
</script>

<template>
  <div ref="listRef" class="chat-message-list">
    <!-- 空状态提示 -->
    <div v-if="chatStore.messages.length === 0" class="chat-message-list__empty">
      <p class="chat-message-list__empty-text">向 AI 助手发送消息开始对话</p>
    </div>

    <!-- 消息列表 -->
    <template v-else>
      <ChatMessage
        v-for="msg in chatStore.messages"
        :key="msg.id"
        :message="msg"
        @confirm="handleConfirm"
      />
    </template>

    <!-- 流式响应打字指示器 -->
    <div v-if="chatStore.isStreaming" class="chat-message-list__typing">
      <span class="chat-message-list__typing-dot" />
      <span class="chat-message-list__typing-dot" />
      <span class="chat-message-list__typing-dot" />
    </div>
  </div>
</template>

<style scoped>
.chat-message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* 美化滚动条 */
  scrollbar-width: thin;
  scrollbar-color: var(--app-border, #30363d) transparent;
}

.chat-message-list::-webkit-scrollbar {
  width: 4px;
}

.chat-message-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-message-list::-webkit-scrollbar-thumb {
  background: var(--app-border, #30363d);
  border-radius: 2px;
}

/* 空状态 */
.chat-message-list__empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-message-list__empty-text {
  font-size: 13px;
  color: var(--app-text-muted, #848d97);
  text-align: center;
}

/* 打字指示器 */
.chat-message-list__typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  align-self: flex-start;
}

.chat-message-list__typing-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-text-muted, #848d97);
  animation: typing-bounce 1.2s ease-in-out infinite;
}

.chat-message-list__typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.chat-message-list__typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  40% {
    transform: translateY(-6px);
    opacity: 1;
  }
}
</style>
