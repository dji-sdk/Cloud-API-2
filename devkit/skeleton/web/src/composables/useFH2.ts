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

import type { FH2InitConfig } from '@/types/fh2'
import { useFH2Config } from './useFH2Config'

/** 模块级单例：保证 paas.js 只加载一次，window.FH2 只初始化一次。 */
let _loadPromise: Promise<void> | null = null
let _isInitialized = false

/**
 * 构建 FH2 SDK 鉴权对象。
 * authKeys 声明需要透传的鉴权头名称，headers 闭包在每次请求时读取最新 token。
 * sdkAuthKey / sdkAuthValue 用于附加平台要求的额外鉴权头（如 user_id）。
 */
function buildAuth(cfg: { sdkAuthKey: string; sdkAuthValue: string }): {
  authKeys: string[]
  headers: () => Record<string, string>
} {
  return {
    authKeys: ['X-User-Token', ...(cfg.sdkAuthKey ? [cfg.sdkAuthKey] : [])],
    headers: () => ({
      'X-User-Token': localStorage.getItem('x-user-token') ?? '',
      ...(cfg.sdkAuthKey ? { [cfg.sdkAuthKey]: cfg.sdkAuthValue } : {}),
    }),
  }
}

/**
 * paas.js 加载完成后 window.FH2 由其内部异步注册，轮询等待就绪。
 * paas.js 监听 DOMContentLoaded 来触发注册；SPA 中需手动 re-dispatch。
 */
function waitForFH2(timeout = 30000, paasUrl?: string): Promise<void> {
  return new Promise((resolve, reject) => {
    if (window.FH2) { resolve(); return }
    const start = Date.now()
    const check = setInterval(() => {
      if (window.FH2) {
        clearInterval(check)
        resolve()
      } else if (Date.now() - start > timeout) {
        clearInterval(check)
        reject(new Error(`Timeout waiting for window.FH2 — paas.js: ${paasUrl ?? 'unknown'}`))
      }
    }, 100)
  })
}

/**
 * 动态加载 paas.js（走 FH2 Web 门户 80 端口，不是 API 的 30812 端口）。
 * 幂等：已在加载中时直接返回同一 Promise；超时后重试也不会重复插入 script 标签。
 */
function loadPaasScript(address: string): Promise<void> {
  if (_loadPromise) return _loadPromise
  _loadPromise = new Promise((resolve, reject) => {
    if (window.FH2) { resolve(); return }
    const paasUrl = `${address}/paas.js`

    // 防止超时 reject 后再次调用时重复插入 script 标签
    const existing = document.querySelector(`script[src="${paasUrl}"]`)
    if (existing) {
      // script 已存在但 FH2 未就绪 → 只需等待（可能上一次超时后 FH2 仍在初始化）
      waitForFH2(30000, paasUrl).then(resolve).catch((err) => {
        _loadPromise = null
        reject(err)
      })
      return
    }

    const script = document.createElement('script')
    script.src = paasUrl
    script.setAttribute('fh2', '')
    script.onload = () => {
      if (document.readyState !== 'loading') {
        document.dispatchEvent(new Event('DOMContentLoaded'))
      }
      waitForFH2(30000, paasUrl).then(resolve).catch((err) => {
        _loadPromise = null
        reject(err)
      })
    }
    script.onerror = () => {
      _loadPromise = null
      reject(new Error(`Failed to load paas.js from ${paasUrl}`))
    }
    document.head.appendChild(script)
  })
  return _loadPromise
}

/**
 * 确保 FH2 SDK（paas.js 模式）已初始化。
 * 多次调用安全：第一次真正执行，后续直接返回。
 */
export async function ensureFH2Init(): Promise<void> {
  if (_isInitialized && window.FH2) return

  const cfg = useFH2Config()

  // paas.js 由 FH2 Web 门户（80 端口）提供，address 不含端口
  await loadPaasScript(cfg.address)

  if (!_isInitialized) {
    const initConfig: FH2InitConfig = {
      serverUrl:    cfg.baseUrl,
      wssUrl:       cfg.wssUrl,
      hostUrl:      cfg.address,
      prjId:        cfg.prjId,
    }

    window.FH2.initConfig(initConfig)

    const auth = buildAuth(cfg)
    window.FH2.initNotifyCenter(cfg.wssUrl, auth)
    // 第三个参数 wssUrl 用于地图 doodle 推送插件订阅实时更新（对齐 demo real-time.html）
    window.FH2.loadMap(cfg.baseUrl, auth, cfg.wssUrl)

    _isInitialized = true
  }
}

/** 重置初始化状态（供测试使用）。 */
export function resetFH2State(): void {
  _loadPromise = null
  _isInitialized = false
}
