// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * OSD / SEI 表格展示用格式化工具。
 *
 * SDK 原始单位与展示换算见 docs/0_整体方案.md §7.3.1。
 */

/** 水平/垂直速度：SDK 为 m/s，直接展示 */
export function formatSpeedMps(rawMPerSec: unknown, digits = 1): string {
  const n = Number(rawMPerSec)
  if (rawMPerSec == null || rawMPerSec === '' || !Number.isFinite(n)) return '--'
  return `${n.toFixed(digits)} m/s`
}

/** 风速：FHSDK 原始值为 dm/s（分米/秒），直接展示 */
export function formatWindSpeed(rawDmPerSec: unknown): string {
  const n = Number(rawDmPerSec)
  if (rawDmPerSec == null || rawDmPerSec === '' || !Number.isFinite(n)) return '--'
  return `${Math.round(n)} dm/s`
}

/** SEI altitude：FHSDK 原始值为 mm（相对起飞点，向上为正），直接展示 */
export function formatSeiAltitudeMm(rawMm: unknown): string {
  const n = Number(rawMm)
  if (rawMm == null || rawMm === '' || !Number.isFinite(n)) return '--'
  return `${Math.round(n)} mm`
}

export function formatCoord(lat?: unknown, lng?: unknown): string {
  const la = Number(lat)
  const lo = Number(lng)
  if (!Number.isFinite(la) || !Number.isFinite(lo) || (la === 0 && lo === 0)) {
    return '--'
  }
  return `${la.toFixed(6)}, ${lo.toFixed(6)}`
}

export function formatNum(v: unknown, unit = '', digits = 1): string {
  const n = Number(v)
  if (v == null || v === '' || !Number.isFinite(n)) return '--'
  return `${n.toFixed(digits)}${unit}`
}

const WIND_DIRS = ['', '北', '东北', '东', '东南', '南', '西南', '西', '西北']

export function formatWindDir(v: unknown): string {
  const n = Number(v)
  if (!Number.isFinite(n) || n < 1 || n > 8) return v != null && v !== '' ? String(v) : '--'
  return WIND_DIRS[n] ?? String(v)
}

export function formatFlightTime(sec: unknown): string {
  const s = Number(sec)
  if (!Number.isFinite(s) || s < 0) return '--'
  const m = Math.floor(s / 60)
  const r = Math.floor(s % 60)
  return `${m}:${r.toString().padStart(2, '0')}`
}

/** SEI 整型经纬度 → 度 */
export function decodeSeiCoord(encoded: unknown, scale: 'lat' | 'lng'): string {
  const n = Number(encoded)
  if (!Number.isFinite(n)) return '--'
  const factor = scale === 'lat' ? 180 / 2 ** 32 : 360 / 2 ** 32
  return (n * factor).toFixed(6)
}

const RAD_TO_DEG = 180 / Math.PI

/** SEI 传感器物理量（μm）→ H/V/D 视场角（度） */
export function computeSeiFovDeg(
  fovHorizontalUm: number | undefined,
  fovVerticalUm: number | undefined,
  focalLengthUm: number | undefined,
): { hDeg: number; vDeg: number; dDeg: number } | null {
  if (!focalLengthUm || focalLengthUm <= 0) return null
  const hUm = fovHorizontalUm ?? 0
  const vUm = fovVerticalUm ?? 0
  if (hUm <= 0 || vUm <= 0) return null
  const toDeg = (sensorUm: number) => 2 * Math.atan(sensorUm / (2 * focalLengthUm)) * RAD_TO_DEG
  const hDeg = toDeg(hUm)
  const vDeg = toDeg(vUm)
  const dDeg = toDeg(Math.hypot(hUm, vUm))
  if (hDeg <= 0 || vDeg <= 0 || dDeg <= 0) return null
  return { hDeg, vDeg, dDeg }
}

/** SEI FOV 行：原始 μm + 换算 HFOV/VFOV/DFOV（度） */
export function formatSeiFovCell(
  fovHorizontalUm: number | undefined,
  fovVerticalUm: number | undefined,
  focalLengthUm: number | undefined,
  digits = 1,
): string {
  const raw = `H ${formatNum(fovHorizontalUm, ' μm', 0)} · V ${formatNum(fovVerticalUm, ' μm', 0)} · f ${formatNum(focalLengthUm, ' μm', 0)}`
  const fov = computeSeiFovDeg(fovHorizontalUm, fovVerticalUm, focalLengthUm)
  if (!fov) return raw
  const deg = (n: number) => `${n.toFixed(digits)}°`
  return `${raw} · HFOV ${deg(fov.hDeg)} · VFOV ${deg(fov.vDeg)} · DFOV ${deg(fov.dDeg)}`
}
