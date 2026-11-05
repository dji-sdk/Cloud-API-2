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
 * 底座（FH2 上云 API）相关的统一中文错误文案。
 *
 * <p>依赖底座的 5 个页面（直播大屏 / 设备管理 / 驾驶舱 / 航线编辑器 / 飞行回放）都会
 * 在底座缺失或不可达时报错。若各页面自行拼装文案，同一个故障会出现五种说法。
 * 这里统一为「缺什么 + 当前配置 + 怎么补」三段式，任何页面直接复用即可。
 *
 * <p>之所以集中成纯函数而非 composable：文案是纯字符串计算，不持有响应式状态，
 * 便于在 catch 分支、computed 和单测中直接调用。
 */

/** 底座地址在本地开发与容器部署下的配置方式（多处复用，避免文案漂移）。 */
const CONFIG_GUIDE =
  '本地开发：复制 skeleton/web/.env.example 为 skeleton/web/.env，填写 VITE_FH2_ADDRESS。\n'
  + '容器部署：在 docker compose 的 environment（或 deploy.env）中设置 VITE_FH2_ADDRESS'
  + '（底座门户地址，如 http://10.0.0.1），然后重启容器。\n'
  + '若确认已配置，请先按 Ctrl/Cmd+Shift+R 强制刷新，排除旧的 config.js 缓存。'

/** 底座不可用的原因。 */
export type PaasUnavailableReason =
  /** 未配置 FH2_ADDRESS（或 VITE_FH2_ADDRESS），前端不知道底座在哪 */
  | 'not-configured'
  /** 已配置但探测/加载失败：主机不可达、端口不通、证书错误、资源 404 等 */
  | 'unreachable'

/**
 * 生成「底座不可用」的完整中文提示。
 *
 * @param reason  不可用原因
 * @param address 当前配置的底座地址；未配置时传空串
 * @returns 多行中文提示，调用方需保证容器可换行
 */
export function formatPaasUnavailableMessage(
  reason: PaasUnavailableReason,
  address: string,
): string {
  if (reason === 'not-configured') {
    return `未配置底座地址：前端不知道 FH2 上云 API 部署在哪里，依赖底座的功能无法使用。\n${CONFIG_GUIDE}`
  }
  return (
    `无法连接底座：${address} 未响应。\n`
    + '请确认 FH2 上云 API 已启动，且当前网络可访问该地址'
    + `（可先用浏览器打开 ${address} 验证）。`
  )
}

/**
 * 生成「底座不可用」的一句话说明，用于导航项 title 等空间受限的位置。
 *
 * @param reason  不可用原因
 * @param address 当前配置的底座地址；未配置时传空串
 * @returns 一句话中文说明
 */
export function formatPaasShortHint(
  reason: PaasUnavailableReason,
  address: string,
): string {
  if (reason === 'not-configured') {
    return '需要底座：尚未配置 FH2 上云 API 地址'
  }
  return `需要底座：${address} 未响应`
}

/**
 * 生成「底座静态资源加载失败」的中文提示。
 *
 * @param resource 资源的中文名称，如「设备管理 SDK」
 * @param url      实际请求地址，便于用户直接粘贴到浏览器验证
 * @returns 多行中文提示
 */
export function formatPaasResourceErrorMessage(
  resource: string,
  url: string,
): string {
  return (
    `${resource}加载失败：${url}\n`
    + '请确认底座可以访问该地址；若底座部署在同源反代之后，需检查 FH2_ASSET_HOST 是否配置正确。'
  )
}

/**
 * 浏览器原生网络错误的消息特征（小写）。
 *
 * <p>各内核文案不同，但语义相同：请求根本没到达服务端。这类消息是英文且对用户
 * 毫无操作价值，必须改写成中文指引；其余异常（SDK 自抛的中文错误等）原样保留，
 * 避免把有用信息也覆盖掉。
 */
const BROWSER_NETWORK_ERROR_PATTERNS = [
  'failed to fetch',
  'networkerror',
  'load failed',
  'network request failed',
]

/**
 * 判断异常消息是否属于浏览器原生网络错误。
 *
 * @param message 异常消息
 * @returns true 表示是浏览器网络层报错
 */
function isBrowserNetworkError(message: string): boolean {
  const lower = message.toLowerCase()
  return BROWSER_NETWORK_ERROR_PATTERNS.some((pattern) => lower.includes(pattern))
}

/**
 * 把任意异常转成可展示的中文文案。
 *
 * <p>只用于兜底：优先使用上面几个具体函数，只有在无法归类时才落到这里。
 * 浏览器原生的 `TypeError: Failed to fetch` 一类网络错误会被改写成中文指引，
 * 其余异常消息（通常是本仓库自抛的中文错误）原样透传，避免丢失细节。
 *
 * @param e 捕获到的异常
 * @returns 中文文案
 */
export function formatUnknownError(e: unknown): string {
  if (!(e instanceof Error) || !e.message) {
    return `未知错误：${String(e)}`
  }
  if (isBrowserNetworkError(e.message)) {
    return '网络请求失败：目标地址不可达，或被浏览器拦截（断网 / 跨域 / 证书错误）。'
      + '请打开开发者工具的 Network 面板确认具体原因。'
  }
  return e.message
}
