// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PayloadListData, PayloadVo } from '@/api'

/** 浏览器会话侧状态：SDK/WS/直播/DRC 连接与 Payload 选择（与 HttpSession 一一对应） */
export const useSessionStore = defineStore('session', () => {
  const sdkReady = ref(false)
  const wsConnected = ref(false)
  const liveStarted = ref(false)
  const drcConnected = ref(false)
  const selectedGatewaySn = ref<string | null>(null)

  const payloadOptions = ref<PayloadVo[]>([])
  const payloadDraft = ref('')
  const payloadConfigured = ref(false)
  const confirmedPayloadIndex = ref<string | null>(null)
  const defaultPayloadIndex = ref<string | null>(null)

  function applyPayloadList(data: PayloadListData) {
    payloadOptions.value = data.payloads ?? []
    payloadConfigured.value = !!data.configured
    confirmedPayloadIndex.value = data.selectedPayloadIndex ?? null
    if (data.defaultPayloadIndex) {
      defaultPayloadIndex.value = data.defaultPayloadIndex
    }
    if (data.configured && data.selectedPayloadIndex) {
      payloadDraft.value = data.selectedPayloadIndex
      return
    }
    if (!payloadDraft.value) {
      const hint = data.defaultPayloadIndex || data.payloads?.[0]?.payloadIndex
      if (hint) payloadDraft.value = hint
    }
  }

  function resetPayload() {
    payloadOptions.value = []
    payloadDraft.value = ''
    payloadConfigured.value = false
    confirmedPayloadIndex.value = null
    defaultPayloadIndex.value = null
  }

  return {
    sdkReady,
    wsConnected,
    liveStarted,
    drcConnected,
    selectedGatewaySn,
    payloadOptions,
    payloadDraft,
    payloadConfigured,
    confirmedPayloadIndex,
    defaultPayloadIndex,
    applyPayloadList,
    resetPayload,
  }
})
