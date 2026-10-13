// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import client, { ensureSessionInitialized, type ApiResponse } from './client'

/**
 * 后端 REST API 封装（前缀 /api/v1，见 ApiController）。
 * 所有请求依赖 Cookie 中的 HttpSession，须先 initSession()。
 */
export interface DeviceVo {
  gatewaySn: string
  gatewayName: string
  online: boolean
  droneSn?: string
  droneName?: string
  droneOnline?: boolean
}

export interface PayloadVo {
  payloadIndex: string
  deviceSn: string
  videoId: string
}

export interface PayloadListData {
  payloads: PayloadVo[]
  selectedPayloadIndex?: string | null
  configured: boolean
  defaultPayloadIndex?: string
}

export async function initSession() {
  await ensureSessionInitialized()
}

export async function fetchDevices() {
  const { data } = await client.get<ApiResponse<{ devices: DeviceVo[] }>>('/devices')
  return data.data?.devices ?? []
}

export async function selectDevice(gatewaySn: string) {
  await client.post('/devices/select', { gatewaySn })
}

export async function deselectDevice() {
  await client.delete('/devices/select')
}

export async function fetchPayloads() {
  const { data } = await client.get<ApiResponse<PayloadListData>>('/devices/payloads')
  return data.data!
}

export async function confirmPayload(payloadIndex: string) {
  const { data } = await client.post<ApiResponse<PayloadListData>>('/devices/payload', { payloadIndex })
  return data.data!
}

export async function connectDrc() {
  await client.post('/control/drc-link')
}

export async function disconnectDrc() {
  await client.delete('/control/drc-link')
}

export async function sendStick(body: {
  roll: number
  pitch: number
  throttle: number
  yaw: number
  gimbalPitch?: number
  durationMs?: number
}) {
  await client.post('/control/stick', body)
}

export async function resetGimbal(body: {
  payloadIndex?: string
  resetMode: number
}) {
  await client.post('/control/gimbal-reset', body)
}

export async function fetchStreamUrl() {
  const { data } = await client.get<ApiResponse<{ whepUrl: string; streamKey: string; payloadIndex?: string }>>('/live/stream-url')
  return data.data!
}

export async function startLive() {
  const { data } = await client.post<ApiResponse<{ whepUrl: string; streamKey: string; payloadIndex?: string }>>(
    '/live/start',
    {},
    { timeout: 60000 },
  )
  return data.data!
}

export async function stopLive() {
  await client.delete('/live/stop')
}
