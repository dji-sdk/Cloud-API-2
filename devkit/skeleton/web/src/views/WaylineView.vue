<!--
  Copyright 2025-2026 DJI. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<script setup lang="ts">
/**
 * 航线管理视图 — 双模式
 *
 * 默认模式：航线库列表（WaylineLibrary）
 * 编辑器模式：当 URL query 包含 mode=create 或 mode=edit 时进入 WaylineEditor SDK
 *
 * 参考：skills/dji-flighthub-sdk/references/wayline-editor.md
 */
import { onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFH2Config } from '@/composables/useFH2Config'
import { useSDKLoader } from '@/composables/useSDKLoader'
import { ensureFH2Init } from '@/composables/useFH2'
import { injectLink, injectScriptUMD } from '@/utils/sdk'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import WaylineLibrary from '@/components/wayline/WaylineLibrary.vue'
import type { WaylineEditor } from '@/types/fh2'

const route  = useRoute()
const router = useRouter()

const { status, errorMsg, markReady, markError } = useSDKLoader()

/** 当前模式：无参数=库模式，create/edit=编辑器模式 */
const mode = computed(() => (route.query.mode as string) || '')
const waylineId = computed(() => route.query.wayline_id as string | undefined)
const isEditorMode = computed(() => mode.value === 'create' || mode.value === 'edit')

let editor: WaylineEditor | null = null
let initInProgress = false

/** 库模式事件：跳转创建 */
function handleCreate(): void {
  router.push({ path: '/wayline', query: { mode: 'create' } })
}

/** 库模式事件：跳转编辑 */
function handleEdit(uuid: string): void {
  router.push({ path: '/wayline', query: { mode: 'edit', wayline_id: uuid } })
}

/** 加载编辑器 SDK 资源并实例化 WaylineEditor。 */
async function loadEditorSDK(cfg: ReturnType<typeof useFH2Config>): Promise<WaylineEditor> {
  injectLink(`${cfg.address}/paasjs-demo/wayline-edit-sdk/index.css`)
  await injectScriptUMD(`${cfg.address}/paasjs-demo/wayline-edit-sdk/index.js`, 'WaylineEditor')

  if (!window.WaylineEditor || typeof window.WaylineEditor !== 'function') {
    throw new Error('WaylineEditor SDK 加载完毕，但 window.WaylineEditor 未暴露。')
  }

  // 鉴权配置：api 和 ws 共用同一 auth 对象（对齐 demo.html）
  const auth = {
    authKeys: ['X-User-Token', ...(cfg.sdkAuthKey ? [cfg.sdkAuthKey] : [])],
    headers: () => ({
      'X-User-Token': localStorage.getItem('x-user-token') ?? '',
      ...(cfg.sdkAuthKey ? { [cfg.sdkAuthKey]: cfg.sdkAuthValue } : {}),
    }),
  }

  return new window.WaylineEditor({
    hostUrl: cfg.address,
    api: {
      baseUrl: `${cfg.baseUrl}/gateway`,
      auth,
    },
    ws: { url: cfg.wssUrl, auth },
    // 关键：WASM 运行时依赖（PPE/WPMZ 等）资源 base。
    // 不传则默认从 wayline-edit-sdk 自身目录加载，FH2 服务器该目录缺失会 load failed。
    runtimeDepsBasePath: `${cfg.address}/paasjs-demo/sdk-runtime`,
    map: {
      mode: 'internal',
      env: {
        cesiumBaseUrl: `${cfg.address}/paasjs-demo/sdk-runtime/cesium/`,
        terrainKey: '',
        featureGateway: [],
      },
    },
    oss: { objectKeyPrefix: 'skeleton_wayline' },
    features: { thirdPartyAlgo: true },
    locale: { language: 'zh' },
  })
}

/** 注册路由相关事件（须在 loadMap/mount 之前注册，确保任何阶段错误都能收到）。 */
function bindEditorEvents(inst: WaylineEditor): void {
  inst.on('wayline-save', (id: unknown) => { console.log('[WaylineEditor] 航线已保存:', id) })
  inst.on('wayline-cancel', () => { router.push('/wayline') })
  inst.on('wayline-back',   () => { router.push('/wayline') })
  inst.on('error', (e: unknown) => {
    // 断言原因：SDK 错误事件结构已知但无导出类型，手动声明
    const err = e as { code: number; message: string }
    console.error('[WaylineEditor] error:', err.code, err.message)
  })
}

/** 地图容器元素，loadMap 与 mount 共用。 */
function getMapContainer(): HTMLElement {
  const container = document.getElementById('map-app-global')
  if (!container) {
    throw new Error('DOM 容器 #map-app-global 不存在')
  }
  return container
}

/** 编辑器挂载容器。 */
function getEditorContainer(): HTMLElement {
  const container = document.getElementById('uranus-wayline-app')
  if (!container) {
    throw new Error('DOM 容器 #uranus-wayline-app 不存在')
  }
  return container
}

/** 编辑器初始视角（深圳，与 demo.html 一致）。 */
const DEFAULT_MAP_CENTER = { longitude: 114.0579, latitude: 22.5431, height: 5000 }

/**
 * 启动编辑器渲染（create 或 edit 模式）。
 * 调用顺序对齐 demo.html：loadMap → mount → render/create。
 *
 * markReady 在 mount 完成后立即调用：遮罩 SDKLoadingMask 是 z-index:99998 的
 * 实色全屏遮罩，若推迟到 create()/render() 之后才隐藏，会挡住 SDK 弹出的
 * 创建/编辑对话框导致无法交互（demo.html 的 create 由独立按钮触发，不在初始化流程）。
 */
async function renderEditor(inst: WaylineEditor): Promise<void> {
  // edit 模式参数校验须在 loadMap/mount 之前，避免无效路径已挂载编辑器再抛错
  if (mode.value === 'edit' && !waylineId.value) {
    throw new Error('编辑模式需要 wayline_id 参数')
  }

  // ① loadMap 必须在 mount 之前（internal 模式 SDK 自建 Cesium 地图）
  await inst.loadMap(getMapContainer(), DEFAULT_MAP_CENTER)

  // ② 挂载编辑器 UI
  inst.mount(getEditorContainer())

  // ③ 编辑器已可交互，立即隐藏 loading 遮罩（对话框/地图操作不再被挡）
  markReady()

  if (mode.value === 'edit') {
    inst.render('edit', { waylineId: waylineId.value })
  } else {
    // create 模式：create() 弹出创建对话框，resolve 表示用户已保存
    inst.create()
      .then((res) => {
        if (!editor) return
        console.log('[WaylineEditor] 航线创建成功:', res.waylineId)
        router.push('/wayline')
      })
      .catch((err: Error) => {
        if (!editor) return
        if (err?.message === 'User cancelled') {
          router.push('/wayline')
          return
        }
        console.error('[WaylineEditor] create 失败:', err.message)
      })
  }
}

/** 初始化编辑器 SDK */
async function initEditor(): Promise<void> {
  if (initInProgress) return
  destroyEditor()
  initInProgress = true
  try {
    const cfg = useFH2Config()
    await ensureFH2Init()
    editor = await loadEditorSDK(cfg)
    // 错误事件须在 loadMap/mount 之前注册，确保任何阶段的错误都能收到
    bindEditorEvents(editor)
    await renderEditor(editor)
  } catch (e: unknown) {
    // 若已实例化编辑器但在渲染中途抛错（如校验失败、mount 后异常），
    // 须销毁残留实例，避免挂在 DOM 上藏在错误面板背后。
    if (editor) {
      destroyEditor()
    }
    markError(e)
  } finally {
    initInProgress = false
  }
}

/** 销毁编辑器 */
function destroyEditor(): void {
  if (editor) {
    editor.destroy()
    editor = null
  }
}

/**
 * 编辑器目标标识：编辑器模式下由 mode + waylineId 组合决定；库模式为空字符串。
 * 用于侦测同布尔值内的切换（如 edit 航线 A → edit 航线 B、create → edit），
 * 仅依赖 isEditorMode 会漏掉这类同 true 内的变更。
 */
const editorTarget = computed(() =>
  isEditorMode.value ? `${mode.value}:${waylineId.value ?? ''}` : '',
)

onMounted(() => {
  if (editorTarget.value) {
    initEditor()
  }
})

watch(editorTarget, (val, prev) => {
  if (!val) {
    destroyEditor()
  } else if (val !== prev) {
    initEditor()
  }
})

onUnmounted(() => {
  destroyEditor()
})
</script>

<template>
  <!-- 库模式（默认） -->
  <WaylineLibrary
    v-if="!isEditorMode"
    @create="handleCreate"
    @edit="handleEdit"
  />

  <!-- 编辑器模式 -->
  <div v-else class="wayline-page">
    <SDKLoadingMask
      :status="status"
      :error-msg="errorMsg"
      loading-text="正在加载航线编辑器…"
    />

    <div class="fh2-container">
      <div id="wayline-header"></div>
      <div class="project-details">
        <div id="project-middle-container"></div>
        <div id="project-right-micro-app" class="right-micro-app">
          <div class="maps-micro-app">
            <div id="project-map-app-placeholder" class="map-app-placeholder">
              <div id="map-app-global" class="map-app-container"></div>
            </div>
          </div>
          <div id="wayline-app-container">
            <div id="uranus-wayline-app"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wayline-page {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* ── FH2 航线编辑器布局（class 名/DOM 结构对齐 demo.html，不可随意修改） ── */

.fh2-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.fh2-container :deep(.map-app-container > div) {
  width: 100%;
  height: 100%;
}

.fh2-container :deep(.project-details) {
  position: relative;
  width: 100%;
  display: flex;
  flex: 1;
  overflow: hidden;
}

.fh2-container :deep(.project-details .right-micro-app) {
  position: relative;
  display: flex;
  flex: 1;
}

.fh2-container :deep(.project-details .right-micro-app .maps-micro-app) {
  display: flex;
  flex: 1;
  height: 100%;
}

.fh2-container :deep(.project-details .right-micro-app .map-app-placeholder) {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.fh2-container :deep(.project-details .right-micro-app .map-app-container) {
  height: 100%;
  min-height: 0;
}

.fh2-container :deep(.project-details .right-micro-app #wayline-app-container) {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
</style>
