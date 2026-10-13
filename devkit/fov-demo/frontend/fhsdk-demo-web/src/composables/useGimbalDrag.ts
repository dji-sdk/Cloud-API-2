// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import type { Ref } from 'vue'

const NEUTRAL = 1024
const DRAG_SENSITIVITY = 2.2

/**
 * 直播区指针拖拽 → 杆量 yaw / gimbalPitch（DRC stick_control）。
 */
export function useGimbalDrag(options: {
  layerRef: Ref<HTMLElement | null>
  enabled: () => boolean
  setGimbalDrag: (value: { yaw: number; gimbalPitch: number } | null) => void
  clampStick: (v: number) => number
  onDragStart?: () => void
  onDragEnd?: () => void
}) {
  let yawStick = NEUTRAL
  let gimbalPitchStick = NEUTRAL
  let dragging = false
  let lastX = 0
  let lastY = 0

  function resetSticks() {
    yawStick = NEUTRAL
    gimbalPitchStick = NEUTRAL
    options.setGimbalDrag(null)
  }

  function onPointerDown(e: PointerEvent) {
    if (!options.enabled()) return
    dragging = true
    lastX = e.clientX
    lastY = e.clientY
    options.layerRef.value?.setPointerCapture(e.pointerId)
    options.onDragStart?.()
  }

  function onPointerMove(e: PointerEvent) {
    if (!dragging || !options.enabled()) return
    const dx = e.clientX - lastX
    const dy = e.clientY - lastY
    lastX = e.clientX
    lastY = e.clientY
    if (dx === 0 && dy === 0) return

    yawStick = options.clampStick(yawStick + dx * DRAG_SENSITIVITY)
    gimbalPitchStick = options.clampStick(gimbalPitchStick - dy * DRAG_SENSITIVITY)
    options.setGimbalDrag({ yaw: yawStick, gimbalPitch: gimbalPitchStick })
  }

  function onPointerUp(e: PointerEvent) {
    if (!dragging) return
    dragging = false
    try {
      options.layerRef.value?.releasePointerCapture(e.pointerId)
    } catch {
      /* already released */
    }
    resetSticks()
    options.onDragEnd?.()
  }

  function mount() {
    const el = options.layerRef.value
    if (!el) return
    el.addEventListener('pointerdown', onPointerDown)
    el.addEventListener('pointermove', onPointerMove)
    el.addEventListener('pointerup', onPointerUp)
    el.addEventListener('pointercancel', onPointerUp)
  }

  function unmount() {
    const el = options.layerRef.value
    if (!el) return
    el.removeEventListener('pointerdown', onPointerDown)
    el.removeEventListener('pointermove', onPointerMove)
    el.removeEventListener('pointerup', onPointerUp)
    el.removeEventListener('pointercancel', onPointerUp)
    dragging = false
    resetSticks()
  }

  return { mount, unmount, resetSticks }
}
