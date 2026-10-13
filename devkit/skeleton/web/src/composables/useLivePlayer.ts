/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ref, onBeforeUnmount } from 'vue'
import { useFH2Config } from './useFH2Config'
import type {
  DjiLivePlayerInstance,
  DjiLivePlayerOptions,
  FillMode,
  LivePlayerInitOptions,
} from '@/types/liveStream'

/** SDK 脚本加载 Promise 单例 */
let _sdkLoadPromise: Promise<void> | null = null

/**
 * 动态注入 DJILivePlayer SDK 的 CSS 和 UMD JS。
 * SDK 文件路径为 FH2 前端服务的 /paasjs-demo/multi-stream-cdn/sdk/ 目录。
 */
function loadLivePlayerSDK(address: string): Promise<void> {
  if (_sdkLoadPromise) return _sdkLoadPromise

  _sdkLoadPromise = new Promise((resolve, reject) => {
    if (window.DJILivePlayer) {
      resolve()
      return
    }

    const baseUrl = `${address}/paasjs-demo/multi-stream-cdn/sdk`

    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = `${baseUrl}/style.css`
    document.head.appendChild(link)

    const script = document.createElement('script')
    script.src = `${baseUrl}/sdk.umd.js`
    script.onload = () => {
      if (window.DJILivePlayer) {
        resolve()
      } else {
        _sdkLoadPromise = null
        reject(new Error('DJILivePlayer SDK 加载成功但全局对象未注册'))
      }
    }
    script.onerror = () => {
      _sdkLoadPromise = null
      reject(new Error(`DJILivePlayer SDK 加载失败: ${baseUrl}/sdk.umd.js`))
    }
    document.head.appendChild(script)
  })

  return _sdkLoadPromise
}

/**
 * 直播播放器管理 Composable。
 * 负责 SDK 初始化、播放器实例创建/销毁和批量管理。
 */
export function useLivePlayer() {
  const initialized = ref(false)
  const players = new Map<string, DjiLivePlayerInstance>()

  /**
   * 初始化 DJILivePlayer SDK。
   * 加载 UMD 脚本并调用全局 initLivePlayer。
   */
  async function initSDK(): Promise<void> {
    if (initialized.value && window.DJILivePlayer?.isLivePlayerInitialized()) {
      return
    }

    const cfg = useFH2Config()
    await loadLivePlayerSDK(cfg.address)

    if (!window.DJILivePlayer) {
      throw new Error('DJILivePlayer SDK 未就绪')
    }

    if (!window.DJILivePlayer.isLivePlayerInitialized()) {
      const initOptions: LivePlayerInitOptions = {
        assetHost: cfg.address,
        serverUrl: cfg.baseUrl,
        auth: {
          authKeys: ['X-User-Token', 'X-Auth-Token'],
          headers: () => ({
            'X-User-Token': localStorage.getItem('x-user-token') ?? '',
          }),
        },
        debug: true,
        locale: 'zh-CN',
      }
      window.DJILivePlayer.initLivePlayer(initOptions)
    }

    initialized.value = true
  }

  /**
   * 创建单个播放器实例。
   * @returns 播放器实例；如果 SDK 未初始化则返回 null
   */
  function createPlayer(
    containerId: string,
    sn: string,
    fillMode: FillMode = 'contain',
    showOsd = true,
  ): DjiLivePlayerInstance | null {
    if (!window.DJILivePlayer || !initialized.value) {
      return null
    }

    const options: DjiLivePlayerOptions = {
      container: `#${containerId}`,
      sn,
      fillMode,
      showOsd,
    }

    const player = new window.DJILivePlayer.DjiLivePlayer(options)
    players.set(containerId, player)
    return player
  }

  /** 销毁单个播放器 */
  function destroyPlayer(containerId: string): void {
    const player = players.get(containerId)
    if (player) {
      player.destroy()
      players.delete(containerId)
    }
  }

  /** 销毁所有播放器实例 */
  function destroyAll(): void {
    for (const player of players.values()) {
      player.destroy()
    }
    players.clear()
  }

  /** 批量更新填充模式 */
  function setAllFillMode(mode: FillMode): void {
    for (const player of players.values()) {
      player.setFillMode(mode)
    }
  }

  /** 批量更新 OSD 显示 */
  function setAllShowOsd(show: boolean): void {
    for (const player of players.values()) {
      player.setShowOsd(show)
    }
  }

  onBeforeUnmount(() => {
    destroyAll()
  })

  return {
    initialized,
    initSDK,
    createPlayer,
    destroyPlayer,
    destroyAll,
    setAllFillMode,
    setAllShowOsd,
  }
}
