// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { TelemetryMessage } from '@/ws/telemetry'

const TRAINING_HINTS_KEY = 'fhsdk-demo.trainingHints'

function readTrainingHints(): boolean {
  try {
    const v = localStorage.getItem(TRAINING_HINTS_KEY)
    return v == null ? true : v === '1'
  } catch {
    return true
  }
}

/** 底部消息日志（REST / WS 调用轨迹，便于联调） */
export const useUiStore = defineStore('ui', () => {
  const logs = ref<string[]>([])
  /** 培训说明 UI（图例、画面中心、三层数据摘要等），默认开启 */
  const showTrainingHints = ref(readTrainingHints())

  watch(showTrainingHints, (on) => {
    try {
      localStorage.setItem(TRAINING_HINTS_KEY, on ? '1' : '0')
    } catch {
      /* ignore */
    }
  })

  function pushLog(line: string) {
    logs.value.unshift(`[${new Date().toLocaleTimeString()}] ${line}`)
    if (logs.value.length > 80) logs.value.pop()
  }

  function logWs(msg: TelemetryMessage) {
    pushLog(`WS uuid=${msg.uuid.slice(0, 8)}… type=${msg.type}${msg.field ? ` field=${msg.field}` : ''}`)
  }

  function logRest(method: string, path: string) {
    pushLog(`REST ${method} ${path}`)
  }

  return { logs, showTrainingHints, pushLog, logWs, logRest }
})
