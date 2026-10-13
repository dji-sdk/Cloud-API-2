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

/**
 * 运行时配置读取工具
 *
 * 优先从 window.__FH_CONFIG__（容器启动时注入）读取，
 * fallback 到 import.meta.env（Vite 构建时变量，供本地开发使用）。
 */

interface RuntimeConfig {
  [key: string]: string | undefined
}

/**
 * 获取运行时配置值
 * @param key - 配置键名（不含 VITE_ 前缀），如 'FH2_ADDRESS'
 * @param fallback - 默认值
 */
export function getRuntimeConfig(key: string, fallback = ''): string {
  const rc = (window as unknown as { __FH_CONFIG__?: RuntimeConfig }).__FH_CONFIG__
  if (rc && rc[key] !== undefined && rc[key] !== '') {
    return rc[key] as string
  }
  return (import.meta.env[`VITE_${key}`] as string) ?? fallback
}
