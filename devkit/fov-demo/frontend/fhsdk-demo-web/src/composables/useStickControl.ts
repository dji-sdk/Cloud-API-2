// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

const NEUTRAL = 1024
const STICK_DELTA = 300
const STICK_MIN = 364
const STICK_MAX = 1684

export type StickPayload = {
  roll: number
  pitch: number
  throttle: number
  yaw: number
  gimbalPitch: number
}

function clampStick(v: number) {
  return Math.max(STICK_MIN, Math.min(STICK_MAX, Math.round(v)))
}

/**
 * 键盘杆量（WASD 平移 + RF 升降 + QE 偏航）+ 可选云台拖拽覆盖 yaw/gimbalPitch。
 * DJI 杆量中性值 1024，范围约 364–1684；每 50ms 下发一次。
 */
export function useStickControl(options: {
  enabled: () => boolean
  onSend: (stick: StickPayload) => void
}) {
  const keys = new Set<string>()
  let timer: number | undefined
  let gimbalDrag: { yaw: number; gimbalPitch: number } | null = null

  function stickFromKeys(): StickPayload {
    let roll = NEUTRAL
    let pitch = NEUTRAL
    let throttle = NEUTRAL
    let yaw = NEUTRAL
    let gimbalPitch = NEUTRAL

    if (keys.has('W')) pitch = NEUTRAL + STICK_DELTA
    if (keys.has('S')) pitch = NEUTRAL - STICK_DELTA
    if (keys.has('A')) roll = NEUTRAL - STICK_DELTA
    if (keys.has('D')) roll = NEUTRAL + STICK_DELTA
    if (keys.has('R')) throttle = NEUTRAL + STICK_DELTA
    if (keys.has('F')) throttle = NEUTRAL - STICK_DELTA

    if (gimbalDrag) {
      yaw = gimbalDrag.yaw
      gimbalPitch = gimbalDrag.gimbalPitch
    } else {
      if (keys.has('Q')) yaw = NEUTRAL - STICK_DELTA
      if (keys.has('E')) yaw = NEUTRAL + STICK_DELTA
    }

    return { roll, pitch, throttle, yaw, gimbalPitch }
  }

  function hasActiveInput() {
    return keys.size > 0 || gimbalDrag !== null
  }

  function ensureTimer() {
    if (timer) return
    timer = window.setInterval(() => {
      if (hasActiveInput() && options.enabled()) {
        options.onSend(stickFromKeys())
      }
    }, 50)
  }

  function clearTimerIfIdle() {
    if (!hasActiveInput() && timer) {
      clearInterval(timer)
      timer = undefined
    }
  }

  function onKeyDown(e: KeyboardEvent) {
    const k = e.key.toUpperCase()
    if (!'WASDRFQE'.includes(k) || !options.enabled()) return
    e.preventDefault()
    keys.add(k)
    ensureTimer()
  }

  function onKeyUp(e: KeyboardEvent) {
    keys.delete(e.key.toUpperCase())
    clearTimerIfIdle()
  }

  function isActive(k: string) {
    return keys.has(k) && options.enabled()
  }

  /** 直播画面拖拽时覆盖 yaw / gimbalPitch（与 QE 互斥） */
  function setGimbalDrag(value: { yaw: number; gimbalPitch: number } | null) {
    gimbalDrag = value
      ? { yaw: clampStick(value.yaw), gimbalPitch: clampStick(value.gimbalPitch) }
      : null
    if (gimbalDrag) ensureTimer()
    else clearTimerIfIdle()
  }

  function mount() {
    window.addEventListener('keydown', onKeyDown)
    window.addEventListener('keyup', onKeyUp)
  }

  function unmount() {
    window.removeEventListener('keydown', onKeyDown)
    window.removeEventListener('keyup', onKeyUp)
    if (timer) clearInterval(timer)
    timer = undefined
    keys.clear()
    gimbalDrag = null
  }

  return { mount, unmount, isActive, setGimbalDrag, clampStick }
}
