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
import type { TaskPrecision, TaskStrategy, RepeatType } from '@/types/flightTask'
import {
  CHANNEL_UNSUPPORTED_HINT,
  UNSUPPORTED_STRATEGY_SAVE_HINT,
  isStrategySupported,
} from '@/utils/taskCapability'
import SegmentedControl from './SegmentedControl.vue'
import { Info } from 'lucide-vue-next'

const props = defineProps<{
  precision: TaskPrecision
  strategy: TaskStrategy
  beginAt: number | null
  endAt: number | null
  repeatType: RepeatType | null
  repeatInterval: number | null
  minBatteryCapacity: number | null
  /** 编辑模式：详情接口无法提供重复规则的真实截止时间，需提示用户重新确认 */
  isEdit?: boolean
}>()

const emit = defineEmits<{
  'update:precision': [value: TaskPrecision]
  'update:strategy': [value: TaskStrategy]
  'update:beginAt': [value: number | null]
  'update:endAt': [value: number | null]
  'update:repeatType': [value: RepeatType | null]
  'update:repeatInterval': [value: number | null]
  'update:minBatteryCapacity': [value: number | null]
}>()

const PRECISION_OPTIONS = [
  { value: 'rtk', label: '高精度 RTK' },
  { value: 'gnss', label: 'GNSS' },
]

/**
 * 任务策略选项。
 *
 * repeat / continuous 当前在 PaaS 通道无法送达（原因见 `@/utils/taskCapability`），
 * 置灰而非删除：编辑历史重复任务时仍需把选中态渲染出来。
 */
const STRATEGY_OPTIONS = [
  { value: 'immediate', label: '立即' },
  { value: 'once', label: '单次定时' },
  { value: 'repeat', label: '重复定时', disabled: !isStrategySupported('repeat') },
  { value: 'continuous', label: '连续执行', disabled: !isStrategySupported('continuous') },
]

const REPEAT_TYPE_OPTIONS = [
  { value: 'daily', label: '每天' },
  { value: 'weekly', label: '每周' },
  { value: 'monthly', label: '每月' },
]

/**
 * 当前策略是否已被通道置灰。
 *
 * 置灰只挡「点击」，挡不住「带着历史值直接保存」：编辑一张重复任务时
 * `applyDetail` 会把 strategy 预填为 `repeat`，用户不动任何东西直接提交，
 * 请求仍会带 task_type=2，服务端按「条件任务」处理并丢弃重复周期，任务退化成
 * 只执行一次。因此这里额外暴露一个标志位，供模板在编辑态给出后果提示。
 */
const isCurrentStrategyUnsupported = computed(() => !isStrategySupported(props.strategy))

function toLocalDatetime(timestamp: number | null): string {
  if (!timestamp) return ''
  const d = new Date(timestamp * 1000)
  const pad = (n: number): string => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function fromLocalDatetime(value: string): number | null {
  if (!value) return null
  return Math.floor(new Date(value).getTime() / 1000)
}
</script>

<template>
  <div class="strategy-section">
    <div class="form-field">
      <label class="field-label">
        任务精度
        <Info :size="14" class="info-icon" />
      </label>
      <SegmentedControl
        :options="PRECISION_OPTIONS"
        :model-value="precision"
        @update:model-value="emit('update:precision', $event as TaskPrecision)"
      />
    </div>

    <div class="form-field">
      <label class="field-label">任务策略</label>
      <SegmentedControl
        :options="STRATEGY_OPTIONS"
        :model-value="strategy"
        :disabled-hint="CHANNEL_UNSUPPORTED_HINT"
        @update:model-value="emit('update:strategy', $event as TaskStrategy)"
      />
      <p v-if="isEdit && isCurrentStrategyUnsupported" class="field-warning">
        {{ UNSUPPORTED_STRATEGY_SAVE_HINT }}
      </p>
    </div>

    <!-- 单次定时 -->
    <div v-if="strategy === 'once'" class="form-field">
      <label class="field-label">执行时间</label>
      <input
        class="field-input"
        type="datetime-local"
        :value="toLocalDatetime(beginAt)"
        @input="emit('update:beginAt', fromLocalDatetime(($event.target as HTMLInputElement).value))"
      />
    </div>

    <!-- 重复定时 -->
    <template v-if="strategy === 'repeat'">
      <div class="form-field">
        <label class="field-label">开始时间</label>
        <input
          class="field-input"
          type="datetime-local"
          :value="toLocalDatetime(beginAt)"
          @input="emit('update:beginAt', fromLocalDatetime(($event.target as HTMLInputElement).value))"
        />
      </div>
      <div class="form-field">
        <label class="field-label">截止时间</label>
        <input
          class="field-input"
          type="datetime-local"
          :value="toLocalDatetime(endAt)"
          @input="emit('update:endAt', fromLocalDatetime(($event.target as HTMLInputElement).value))"
        />
        <p v-if="isEdit" class="field-hint">
          平台不会返回重复任务的真实截止时间，请重新选择本次的截止时间
        </p>
      </div>
      <div class="form-field">
        <label class="field-label">重复周期</label>
        <div class="repeat-config">
          <SegmentedControl
            :options="REPEAT_TYPE_OPTIONS"
            :model-value="repeatType || 'daily'"
            @update:model-value="emit('update:repeatType', $event as RepeatType)"
          />
          <div class="interval-input">
            <span class="interval-label">间隔</span>
            <input
              class="field-input interval-num"
              type="number"
              min="1"
              :value="repeatInterval || 1"
              @input="emit('update:repeatInterval', Number(($event.target as HTMLInputElement).value))"
            />
          </div>
        </div>
      </div>
    </template>

    <!-- 连续执行 -->
    <div v-if="strategy === 'continuous'" class="form-field">
      <label class="field-label">最低执行电量 (%)</label>
      <input
        class="field-input"
        type="number"
        min="10"
        max="100"
        placeholder="50"
        :value="minBatteryCapacity"
        @input="emit('update:minBatteryCapacity', ($event.target as HTMLInputElement).value === '' ? null : Number(($event.target as HTMLInputElement).value))"
      />
    </div>
  </div>
</template>

<style scoped>
.strategy-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 500;
}

.info-icon {
  color: var(--app-text-muted);
  cursor: help;
}

.field-hint {
  margin: 0;
  font-size: 12px;
  color: var(--app-text-muted);
}

/* 编辑历史任务时告知「保存即丢失策略」：比 field-hint 更需要被看见，
   用警示色而非弱化色 */
.field-warning {
  margin: 0;
  font-size: 12px;
  color: var(--app-warning);
}

.field-input {
  padding: 10px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-base);
  color-scheme: dark;
}

.field-input:focus {
  border-color: var(--app-primary);
}

.repeat-config {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.interval-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.interval-label {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.interval-num {
  width: 80px;
}
</style>
