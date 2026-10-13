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
 * 向 <head> 动态注入 CSS link 标签（幂等：同 href 不重复注入）。
 */
export function injectLink(href: string): void {
  if (document.querySelector(`link[href="${href}"]`)) {
    return
  }
  const link = document.createElement('link')
  link.rel  = 'stylesheet'
  link.href = href
  document.head.appendChild(link)
}

/**
 * 向 <head> 动态注入 script 标签并等待加载完成（幂等：同 src 不重复注入）。
 */
export function injectScript(src: string): Promise<void> {
  const existing = document.querySelector(`script[src="${src}"]`)
  if (existing) {
    return Promise.resolve()
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src    = src
    script.onload  = () => resolve()
    script.onerror = () => reject(new Error(`Failed to load script: ${src}`))
    document.head.appendChild(script)
  })
}

/**
 * 注入 UMD 格式的 SDK 脚本，确保其导出挂载到 window 全局。
 *
 * 背景：Vite 开发环境可能存在 `module` / `exports` 全局变量（HMR client 引入），
 * 导致 UMD 包走 CommonJS 分支而非 window 分支。此函数在注入前临时屏蔽这些变量，
 * 加载完毕后恢复，从而强制 UMD 包挂载到 `window[globalName]`。
 */
export function injectScriptUMD(src: string, globalName: string): Promise<void> {
  const existing = document.querySelector(`script[src="${src}"]`)
  if (existing) {
    return Promise.resolve()
  }

  // 暂存可能干扰 UMD 检测的全局变量
  // 断言原因：需要 delete window.module 等动态属性，TS 的 Window 接口不允许 delete 操作
  const win = window as Record<string, unknown>
  const savedModule  = win.module
  const savedExports = win.exports
  const savedDefine  = win.define

  // 临时屏蔽，使 UMD 走 `e.GlobalName = factory()` 分支
  delete win.module
  delete win.exports
  delete win.define

  return new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.onload = () => {
      // 恢复之前的全局变量
      if (savedModule !== undefined) { win.module = savedModule }
      if (savedExports !== undefined) { win.exports = savedExports }
      if (savedDefine !== undefined) { win.define = savedDefine }
      resolve()
    }
    script.onerror = () => {
      // 恢复
      if (savedModule !== undefined) { win.module = savedModule }
      if (savedExports !== undefined) { win.exports = savedExports }
      if (savedDefine !== undefined) { win.define = savedDefine }
      reject(new Error(`Failed to load UMD script: ${src}`))
    }
    document.head.appendChild(script)
  })
}
