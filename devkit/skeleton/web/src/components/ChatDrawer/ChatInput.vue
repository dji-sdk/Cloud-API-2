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
import { ref, watch } from 'vue'
import { useVoiceInput } from '@/composables/useVoiceInput'

interface Props {
  disabled: boolean
}

interface Emits {
  (e: 'send', content: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

/** 当前输入内容 */
const inputContent = ref<string>('')

/** 语音输入 */
const { isListening, isSupported, error, interimText, finalText, toggle } = useVoiceInput()

/**
 * 录音开始时的输入框快照，用于拼接识别结果。
 * 在 handleMicClick() 中同步赋值，确保早于任何 interim 更新。
 */
let recordingBaseText = ''

/**
 * interim 文本实时更新输入框（预览效果）。
 * 用 !finalText.value 守卫：当 final 结果和 interim 清空在同一 tick 发生时，
 * 不能用 interim='' 覆盖 finalText watcher 已写入的结果。
 */
watch(interimText, (text) => {
  if (!finalText.value) {
    inputContent.value = recordingBaseText + text
  }
})

/** final 结果落定：追加到 baseText，然后清空快照 */
watch(finalText, (text) => {
  if (text) {
    inputContent.value = recordingBaseText + text
    recordingBaseText = ''
  }
})

/** 点击麦克风按钮：录音前同步记录 baseText，再切换录音状态 */
function handleMicClick(): void {
  if (!isListening.value) {
    recordingBaseText = inputContent.value
  }
  toggle()
}

/** 发送消息 */
function handleSend(): void {
  const content = inputContent.value.trim()
  if (!content || props.disabled) {
    return
  }
  emit('send', content)
  inputContent.value = ''
  recordingBaseText = ''
}

/**
 * 按键处理：Enter 发送，Shift+Enter 换行。
 */
function handleKeydown(event: KeyboardEvent): void {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="chat-input">
    <div
      v-if="error"
      class="chat-input__error"
    >
      {{ error }}
    </div>
    <div class="chat-input__row">
      <button
        v-if="isSupported"
        class="chat-input__mic"
        :class="{ 'chat-input__mic--recording': isListening }"
        :disabled="disabled"
        type="button"
        :aria-label="isListening ? '停止录音' : '开始录音'"
        @click="handleMicClick"
      >
        <svg
          v-if="error"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
        >
          <line x1="12" y1="5" x2="12" y2="13" />
          <circle cx="12" cy="17" r="1" fill="currentColor" stroke="none" />
        </svg>
        <svg
          v-else-if="!isListening"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
          <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
          <line x1="12" y1="19" x2="12" y2="23" />
          <line x1="8" y1="23" x2="16" y2="23" />
        </svg>
        <svg
          v-else
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="currentColor"
          stroke="none"
        >
          <rect x="6" y="4" width="4" height="16" rx="1" />
          <rect x="14" y="4" width="4" height="16" rx="1" />
        </svg>
      </button>
      <input
        v-model="inputContent"
        class="chat-input__field"
        :class="{ 'chat-input__field--recording': isListening }"
        type="text"
        :disabled="disabled"
        :placeholder="isListening ? '正在录音…' : '用一句话下发任务…'"
        @keydown="handleKeydown"
      >
      <button
        class="chat-input__send"
        :disabled="disabled || !inputContent.trim()"
        @click="handleSend"
      >
        {{ disabled ? '…' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-input {
  padding: 12px 16px 16px;
  border-top: 1px solid var(--app-border, #30363d);
  background: var(--app-bg-panel, #161b22);
  flex-shrink: 0;
}

.chat-input__row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--app-bg-card, #1c2333);
  border: 1px solid var(--app-border, #30363d);
  border-radius: 22px;
  padding: 4px 4px 4px 16px;
  transition: border-color 0.15s;
}

.chat-input__row:focus-within {
  border-color: #7c3aed;
}

.chat-input__field {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--app-text, #e6edf3);
  font-size: 13px;
  line-height: 1.5;
  outline: none;
  font-family: inherit;
}

.chat-input__field::placeholder {
  color: var(--app-text-muted, #848d97);
}

.chat-input__field--recording {
  color: rgba(230, 237, 243, 0.5);
}

.chat-input__field:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-input__send {
  padding: 8px 18px;
  border: none;
  border-radius: 18px;
  background: #7c3aed;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
  flex-shrink: 0;
}

.chat-input__send:hover:not(:disabled) {
  opacity: 0.85;
}

.chat-input__send:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.chat-input__error {
  font-size: 12px;
  color: #f87171;
  padding: 0 4px 6px;
}

.chat-input__mic {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--app-text-muted, #848d97);
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s, background 0.15s;
}

.chat-input__mic:hover:not(:disabled) {
  color: var(--app-text, #e6edf3);
  background: var(--app-bg-hover, #21262d);
}

.chat-input__mic:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.chat-input__mic--recording {
  color: #f87171;
  animation: mic-pulse 1.2s ease-in-out infinite;
}

@keyframes mic-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
