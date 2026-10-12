// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

export interface TelemetryMessage {
  uuid: string
  /** mqtt_osd | drc_camera | sei | payload_range */
  type: 'mqtt_osd' | 'drc_camera' | 'sei' | 'payload_range'
  ts: number
  gatewaySn: string
  /** mqtt_osd 子字段，如 location / attitude / gimbalAttitude */
  field?: string
  data: Record<string, unknown>
}

type Handler = (msg: TelemetryMessage) => void

let ws: WebSocket | null = null
const handlers = new Set<Handler>()

/** 与 REST 同源，走 Vite 代理 /api → :8080；须 withCredentials 以携带 JSESSIONID */
function wsUrl() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  return `${proto}://${location.host}/api/v1/ws/telemetry`
}

function attachHandlers(socket: WebSocket) {
  socket.onmessage = (ev) => {
    try {
      const msg = JSON.parse(ev.data) as TelemetryMessage
      handlers.forEach((h) => h(msg))
    } catch {
      /* ignore */
    }
  }
  socket.onclose = () => {
    if (ws === socket) ws = null
  }
}

export function connectTelemetry(onMessage: Handler): Promise<void> {
  handlers.add(onMessage)
  if (ws?.readyState === WebSocket.OPEN) {
    return Promise.resolve()
  }
  if (ws?.readyState === WebSocket.CONNECTING) {
    return new Promise((resolve, reject) => {
      ws!.addEventListener('open', () => resolve(), { once: true })
      ws!.addEventListener('error', () => reject(new Error('WebSocket 连接失败')), { once: true })
    })
  }
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(wsUrl())
    ws = socket
    attachHandlers(socket)
    socket.addEventListener('open', () => resolve(), { once: true })
    socket.addEventListener('error', () => reject(new Error('WebSocket 连接失败')), { once: true })
  })
}

export function disconnectTelemetry(onMessage?: Handler) {
  if (onMessage) handlers.delete(onMessage)
  if (handlers.size === 0 && ws) {
    ws.close()
    ws = null
  }
}

export function isWsOpen() {
  return ws?.readyState === WebSocket.OPEN
}
