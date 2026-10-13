/*
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

import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { fileURLToPath, URL } from 'node:url'
import { dirname, resolve, sep } from 'node:path'
import { createReadStream, statSync, cpSync, existsSync, rmSync } from 'node:fs'

const __dirname = dirname(fileURLToPath(import.meta.url))

/**
 * 开发模式下从 node_modules/cesium/Build/Cesium 提供静态文件。
 * 生产构建通过 package.json build 脚本拷贝到 dist/cesium/。
 */
function cesiumDevPlugin(): Plugin {
  const cesiumRoot = resolve(__dirname, 'node_modules/cesium/Build/Cesium')
  return {
    name: 'vite-plugin-cesium-dev',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = req.url ?? ''
        if (!url.startsWith('/cesium/')) {
          next()
          return
        }
        const filePath = resolve(cesiumRoot, url.slice('/cesium/'.length))
        try {
          const stat = statSync(filePath)
          if (!stat.isFile()) {
            next()
            return
          }
          const ext = filePath.split('.').pop() ?? ''
          const mimeTypes: Record<string, string> = {
            js: 'application/javascript',
            css: 'text/css',
            json: 'application/json',
            png: 'image/png',
            jpg: 'image/jpeg',
            gif: 'image/gif',
            svg: 'image/svg+xml',
            wasm: 'application/wasm',
            woff: 'font/woff',
            woff2: 'font/woff2',
          }
          res.setHeader('Content-Type', mimeTypes[ext] ?? 'application/octet-stream')
          res.setHeader('Cache-Control', 'public, max-age=86400')
          createReadStream(filePath).pipe(res)
        } catch {
          next()
        }
      })
    },
  }
}

/**
 * 将使用说明文档（skeleton/docs/user-guide/）提供给前端。
 * - build：同步到 dist/docs/user-guide/，并发布 使用说明文档.html 为 index.html
 * - serve：通过 dev server 中间件直接伺服 docs/user-guide/，保证开发模式可访问
 * 源文件与部署指南/使用说明.md 统一维护在 docs/ 下，不塞进 web/ 源码树，
 * 用户菜单入口路径为 /docs/user-guide/index.html。
 */
function copyUserGuidePlugin(): Plugin {
  const srcDir = resolve(__dirname, '../docs/user-guide')
  const mimeTypes: Record<string, string> = {
    '.html': 'text/html; charset=utf-8',
    '.md': 'text/markdown; charset=utf-8',
    '.png': 'image/png',
    '.css': 'text/css',
    '.js': 'application/javascript',
  }
  return {
    name: 'vite-plugin-copy-user-guide',
    // 开发模式：伺服 docs/user-guide/ 静态资源
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = req.url ?? ''
        if (!url.startsWith('/docs/user-guide/')) {
          next()
          return
        }
        let decoded: string
        try {
          decoded = decodeURIComponent(url.split('?')[0].split('#')[0].slice('/docs/user-guide/'.length))
        } catch {
          res.statusCode = 400
          res.end('Bad Request')
          return
        }
        const requested = decoded || '使用说明文档.html'
        const rel = requested === 'index.html' ? '使用说明文档.html' : requested
        const filePath = resolve(srcDir, rel)
        if (!filePath.startsWith(srcDir + sep) || !existsSync(filePath) || !statSync(filePath).isFile()) {
          res.statusCode = 404
          res.end('Not Found')
          return
        }
        const ext = filePath.slice(filePath.lastIndexOf('.')).toLowerCase()
        res.setHeader('Content-Type', mimeTypes[ext] ?? 'application/octet-stream')
        createReadStream(filePath).pipe(res)
      })
    },
    // 生产构建：拷贝到 dist/docs/user-guide/
    // 注意：不设 apply: 'build'，否则 configureServer 在 dev 模式不会注册。
    // closeBundle 本身只在 build 阶段触发，serve 阶段不会执行。
    closeBundle() {
      const dest = resolve(__dirname, 'dist/docs/user-guide')
      if (!existsSync(srcDir)) {
        // 文档目录缺失不阻塞构建，仅提示
        console.warn('[vite-plugin-copy-user-guide] 未找到 docs/user-guide，跳过拷贝')
        return
      }
      cpSync(srcDir, dest, { recursive: true })
      // 源文件名为 使用说明文档.html（与 docs/ 下其他文档命名一致），
      // 发布为 index.html 供用户菜单入口 /docs/user-guide/index.html 访问
      const srcHtml = resolve(dest, '使用说明文档.html')
      if (existsSync(srcHtml)) {
        cpSync(srcHtml, resolve(dest, 'index.html'))
        rmSync(srcHtml)
      }
      console.log(`[vite-plugin-copy-user-guide] 已拷贝使用说明到 ${dest}`)
    },
  }
}

export default defineConfig({
  plugins: [vue(), basicSsl(), cesiumDevPlugin(), copyUserGuidePlugin()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    rollupOptions: {
      external: ['cesium'],
      output: {
        globals: { cesium: 'Cesium' },
      },
    },
  },
  server: {
    proxy: {
      // 后端 context-path=/pf-devkit/ai-plugin/skeleton，api 与 mock 共享前缀，单条代理即可
      '/pf-devkit/ai-plugin/skeleton': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    environment: 'jsdom',
    globals: false,
  },
})
