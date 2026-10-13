// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * WHEP 拉流播放（Oryx SRS）。
 * 开发环境将本机 Oryx 地址替换为 Vite 代理 /oryx，避免浏览器 CORS。
 */
export function resolveWhepUrl(whepUrl: string): string {
  if (import.meta.env.DEV) {
    return whepUrl.replace(/^https?:\/\/127\.0\.0\.1:(2022|12022)/, '/oryx')
  }
  return whepUrl
}

export type PlayWhepOptions = {
  /** WHIP 尚未就绪时的 WHEP 重试次数，默认 5 */
  maxConnectRetries?: number
  /** 连接失败后的重连次数，默认 3 */
  maxReconnects?: number
  /** 重试间隔（毫秒），默认 1500 */
  retryDelayMs?: number
}

export async function playWhep(
  video: HTMLVideoElement,
  whepUrl: string,
  options: PlayWhepOptions = {},
): Promise<() => void> {
  const url = resolveWhepUrl(whepUrl)
  const maxConnectRetries = options.maxConnectRetries ?? 5
  const maxReconnects = options.maxReconnects ?? 3
  const retryDelayMs = options.retryDelayMs ?? 1500

  let stopped = false
  let pc: RTCPeerConnection | null = null
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  let connectAttempts = 0
  let reconnects = 0

  const sleep = (ms: number) => new Promise<void>((resolve) => {
    retryTimer = setTimeout(resolve, ms)
  })

  const cleanup = () => {
    stopped = true
    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }
    pc?.close()
    pc = null
    video.srcObject = null
  }

  const negotiate = async (): Promise<void> => {
    if (pc) {
      pc.onconnectionstatechange = null
      pc.close()
      pc = null
    }
    pc = new RTCPeerConnection()
    pc.addTransceiver('video', { direction: 'recvonly' })
    pc.ontrack = async (ev) => {
      video.playsInline = true
      video.muted = true
      video.crossOrigin = 'anonymous'
      video.srcObject = ev.streams[0] ?? new MediaStream([ev.track])
      try {
        await video.play()
      } catch {
        /* autoplay policy */
      }
    }
    pc.onconnectionstatechange = () => {
      if (stopped || !pc) return
      if (pc.connectionState !== 'failed' && pc.connectionState !== 'disconnected') return
      if (reconnects >= maxReconnects) return
      reconnects++
      void (async () => {
        await sleep(retryDelayMs)
        if (stopped) return
        try {
          await negotiate()
        } catch {
          /* next state change or user stop */
        }
      })()
    }

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/sdp' },
      body: offer.sdp,
      signal: AbortSignal.timeout(15000),
    })
    if (!res.ok) {
      pc.close()
      pc = null
      throw new Error(`WHEP failed: ${res.status}`)
    }
    const answer = await res.text()
    await pc.setRemoteDescription({ type: 'answer', sdp: answer })
  }

  while (!stopped && connectAttempts <= maxConnectRetries) {
    try {
      await negotiate()
      return cleanup
    } catch (e) {
      connectAttempts++
      if (connectAttempts > maxConnectRetries || stopped) {
        cleanup()
        throw e
      }
      await sleep(retryDelayMs)
    }
  }

  cleanup()
  throw new Error('WHEP connect gave up')
}
