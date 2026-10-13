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
import { ref, computed } from 'vue'
import { useWaylineList } from '@/composables/useWaylineList'
import { deleteWaylines, downloadWaylines, importWayline, verifyWaylinePersisted } from '@/api/wayline'
import { useFH2Config } from '@/composables/useFH2Config'
import { ensureFH2Init } from '@/composables/useFH2'
import { formatDateTime } from '@/utils/format'
import { injectScriptUMD } from '@/utils/sdk'
import WaylineConcatDialog from './WaylineConcatDialog.vue'
import WaylineDeleteDialog from './WaylineDeleteDialog.vue'
import WaylineImportDialog from './WaylineImportDialog.vue'
import { Plus, Merge, Pencil, Search, RefreshCw, ChevronLeft, ChevronRight, MapPin, Upload, Download, Trash2 } from 'lucide-vue-next'

const emit = defineEmits<{
  create: []
  edit: [uuid: string]
}>()

const {
  filters,
  pagination,
  totalPages,
  waylines,
  selectedUuids,
  isAllSelected,
  loading,
  error,
  fetchWaylines,
  invalidateCache,
  nextPage,
  prevPage,
  toggleSelect,
  toggleSelectAll,
  selectedWaylines,
  clearSelection,
} = useWaylineList()

const showConcatDialog = ref(false)
const concatLoading = ref(false)
const concatError = ref('')

const showDeleteDialog = ref(false)
const deleteLoading = ref(false)
const deleteError = ref('')

const showImportDialog = ref(false)
const importLoading = ref(false)
const importError = ref('')

const exportError = ref('')

/** 从已加载数据中提取可用机型，兼容真实接口与 Mock 两种模式。 */
const droneTypes = computed(() => [...new Set(waylines.value.map(w => w.droneType))])
/** 从已加载数据中提取可用负载，兼容真实接口与 Mock 两种模式。 */
const payloadTypes = computed(() => [...new Set(waylines.value.map(w => w.payloadType))])



function formatDistance(meters: number): string {
  if (meters >= 1000) {
    return `${(meters / 1000).toFixed(1)} km`
  }
  return `${meters} m`
}

function handleConcatOpen(): void {
  concatError.value = ''
  showConcatDialog.value = true
}

async function handleConcatConfirm(name: string): Promise<void> {
  const uuids = [...selectedUuids.value]
  if (uuids.length < 2) return

  concatLoading.value = true
  concatError.value = ''
  try {
    const cfg = useFH2Config()
    await ensureFH2Init()

    if (!window.WaylineEditor) {
      await injectScriptUMD(`${cfg.address}/paasjs-demo/wayline-edit-sdk/index.js`, 'WaylineEditor')
    }
    if (!window.WaylineEditor || typeof window.WaylineEditor.concatWayline !== 'function') {
      throw new Error('WaylineEditor SDK 加载后仍未暴露 concatWayline 方法')
    }

    // concatWayline 静态方法内部依赖 mount() 中初始化的 HTTP client
    // 须创建临时实例并 mount 到隐藏 DOM 触发 HTTP 初始化
    const auth = {
      authKeys: ['X-User-Token', ...(cfg.sdkAuthKey ? [cfg.sdkAuthKey] : [])],
      headers: () => ({
        'X-User-Token': localStorage.getItem('x-user-token') ?? '',
        ...(cfg.sdkAuthKey ? { [cfg.sdkAuthKey]: cfg.sdkAuthValue } : {}),
      }),
    }
    const tempEl = document.createElement('div')
    tempEl.style.cssText = 'position:fixed;left:-9999px;top:-9999px;width:1px;height:1px;overflow:hidden'
    document.body.appendChild(tempEl)
    let editorInst: InstanceType<typeof window.WaylineEditor> | null = null
    try {
      editorInst = new window.WaylineEditor({
        hostUrl: cfg.address,
        api: { baseUrl: `${cfg.baseUrl}/gateway`, auth },
        ws: { url: cfg.wssUrl, auth },
        runtimeDepsBasePath: `${cfg.address}/paasjs-demo/sdk-runtime`,
        map: {
          mode: 'internal',
          env: { cesiumBaseUrl: `${cfg.address}/paasjs-demo/sdk-runtime/cesium/`, terrainKey: '', featureGateway: [] },
        },
        oss: { objectKeyPrefix: 'skeleton_wayline' },
        locale: { language: 'zh' },
      })
      editorInst.mount(tempEl)
    } catch (mountErr) {
      console.warn('[WaylineLibrary] mount() 部分失败（HTTP client 可能已初始化）:', mountErr)
    }

    try {
      // concatWayline 返回的是 SDK 内部句柄（如 987c2a07），非平台落库的 wayline_uuid，
      // 仅用于日志对账，不能作为成功判据。落库与否须按名称回查列表确认。
      const handle = await window.WaylineEditor.concatWayline(name, uuids)
      console.log('[WaylineLibrary] 拼接调用返回句柄:', handle)

      const persisted = await verifyWaylinePersisted(name)
      if (!persisted) {
        // 不关闭弹窗、不清空选择，便于用户直接重试
        throw new Error(`拼接请求已提交，但未在航线库中查到「${name}」，可能未落库，请稍后刷新确认或重试`)
      }

      showConcatDialog.value = false
      clearSelection()
      invalidateCache()
      await fetchWaylines()
    } finally {
      try { editorInst?.destroy() } catch { /* ignore */ }
      document.body.removeChild(tempEl)
    }
  } catch (e: unknown) {
    console.error('[WaylineLibrary] 拼接异常:', e)
    if (e instanceof Error) {
      concatError.value = e.message
    } else if (typeof e === 'string') {
      concatError.value = e
    } else {
      concatError.value = JSON.stringify(e)
    }
  } finally {
    concatLoading.value = false
  }
}

async function handleDeleteConfirm(): Promise<void> {
  const uuids = [...selectedUuids.value]
  if (uuids.length === 0) return

  deleteLoading.value = true
  deleteError.value = ''
  try {
    await deleteWaylines(uuids)
    showDeleteDialog.value = false
    clearSelection()
    invalidateCache()
  } catch (e) {
    deleteError.value = e instanceof Error ? e.message : '删除失败'
  } finally {
    deleteLoading.value = false
  }
}

async function handleExport(): Promise<void> {
  const uuids = [...selectedUuids.value]
  if (uuids.length === 0) return

  exportError.value = ''
  try {
    await downloadWaylines(uuids)
  } catch (e) {
    exportError.value = e instanceof Error ? e.message : '导出失败'
  }
}

async function handleImportConfirm(file: File, name: string): Promise<void> {
  importLoading.value = true
  importError.value = ''
  try {
    await importWayline(file, name)
    showImportDialog.value = false
    invalidateCache()
  } catch (e) {
    importError.value = e instanceof Error ? e.message : '导入失败'
  } finally {
    importLoading.value = false
  }
}
</script>

<template>
  <div class="wayline-library">
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="toolbar-title">航线库</h2>
      <div class="toolbar-actions">
        <button class="action-btn action-btn--primary" @click="emit('create')">
          <Plus :size="14" />
          创建航线
        </button>
        <button class="action-btn action-btn--ghost" @click="showImportDialog = true">
          <Upload :size="14" />
          导入
        </button>
        <button
          class="action-btn action-btn--ghost"
          :disabled="selectedUuids.size === 0"
          @click="handleExport"
        >
          <Download :size="14" />
          导出
          <span v-if="selectedUuids.size > 0" class="select-count">
            ({{ selectedUuids.size }})
          </span>
        </button>
        <button
          class="action-btn action-btn--ghost"
          :disabled="selectedUuids.size < 2"
          @click="handleConcatOpen"
        >
          <Merge :size="14" />
          拼接航线
          <span v-if="selectedUuids.size > 0" class="select-count">
            ({{ selectedUuids.size }})
          </span>
        </button>
        <button
          class="action-btn action-btn--danger"
          :disabled="selectedUuids.size === 0"
          @click="showDeleteDialog = true"
        >
          <Trash2 :size="14" />
          删除
          <span v-if="selectedUuids.size > 0" class="select-count select-count--danger">
            ({{ selectedUuids.size }})
          </span>
        </button>
      </div>
    </div>

    <!-- 过滤器 -->
    <div class="filters">
      <div class="filter-group">
        <select v-model="filters.droneType" class="filter-select">
          <option value="">全部机型</option>
          <option v-for="dt in droneTypes" :key="dt" :value="dt">{{ dt }}</option>
        </select>
        <select v-model="filters.payloadType" class="filter-select">
          <option value="">全部负载</option>
          <option v-for="pt in payloadTypes" :key="pt" :value="pt">{{ pt }}</option>
        </select>
      </div>
      <div class="filter-search">
        <Search :size="14" class="search-icon" />
        <input
          v-model="filters.keyword"
          type="text"
          class="search-input"
          placeholder="搜索航线名称…"
        />
      </div>
      <button class="refresh-btn" title="刷新" @click="fetchWaylines">
        <RefreshCw :size="14" :class="{ spinning: loading }" />
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-banner">
      {{ error }}
      <button class="error-retry" @click="fetchWaylines">重试</button>
    </div>
    <div v-if="concatError" class="error-banner">
      {{ concatError }}
    </div>
    <div v-if="deleteError" class="error-banner">
      {{ deleteError }}
      <button class="error-retry" @click="deleteError = ''">关闭</button>
    </div>
    <div v-if="exportError" class="error-banner">
      {{ exportError }}
      <button class="error-retry" @click="exportError = ''">关闭</button>
    </div>
    <div v-if="importError" class="error-banner">
      {{ importError }}
      <button class="error-retry" @click="importError = ''">关闭</button>
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <table class="wayline-table">
        <thead>
          <tr>
            <th class="col-check">
              <input
                type="checkbox"
                :checked="isAllSelected"
                :indeterminate="selectedUuids.size > 0 && !isAllSelected"
                @change="toggleSelectAll"
              />
            </th>
            <th class="col-name">航线名称</th>
            <th class="col-type">类型</th>
            <th class="col-waypoints">航点</th>
            <th class="col-distance">距离</th>
            <th class="col-drone">机型</th>
            <th class="col-payload">负载</th>
            <th class="col-time">更新时间</th>
            <th class="col-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading && waylines.length === 0">
            <td colspan="9" class="empty-cell">加载中…</td>
          </tr>
          <tr v-else-if="waylines.length === 0">
            <td colspan="9" class="empty-cell">暂无航线数据</td>
          </tr>
          <tr
            v-for="wl in waylines"
            :key="wl.uuid"
            class="wayline-row"
            :class="{ 'wayline-row--selected': selectedUuids.has(wl.uuid) }"
          >
            <td class="col-check">
              <input
                type="checkbox"
                :checked="selectedUuids.has(wl.uuid)"
                @change="toggleSelect(wl.uuid)"
              />
            </td>
            <td class="col-name">
              <span class="wayline-name">{{ wl.name }}</span>
            </td>
            <td class="col-type">
              <span class="type-tag" :class="`type-tag--${wl.waylineType}`">
                {{ wl.waylineType }}
              </span>
            </td>
            <td class="col-waypoints">
              <span class="waypoint-count">
                <MapPin :size="12" class="waypoint-icon" />
                {{ wl.waypointCount ?? '—' }}
              </span>
            </td>
            <td class="col-distance">{{ wl.estimatedDistance ? formatDistance(wl.estimatedDistance) : '—' }}</td>
            <td class="col-drone">
              <span class="drone-tag">{{ wl.droneType }}</span>
            </td>
            <td class="col-payload">{{ wl.payloadType }}</td>
            <td class="col-time">{{ wl.updatedAt ? formatDateTime(wl.updatedAt) : '—' }}</td>
            <td class="col-action">
              <button
                class="edit-btn"
                title="编辑航线"
                @click="emit('edit', wl.uuid)"
              >
                <Pencil :size="14" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span class="pagination-info">
        共 {{ pagination.total }} 条
      </span>
      <div class="pagination-controls">
        <button
          class="page-btn"
          :disabled="pagination.page <= 1"
          @click="prevPage"
        >
          <ChevronLeft :size="14" />
        </button>
        <span class="page-current">{{ pagination.page }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="pagination.page >= totalPages"
          @click="nextPage"
        >
          <ChevronRight :size="14" />
        </button>
      </div>
    </div>

    <!-- 拼接对话框 -->
    <WaylineConcatDialog
      v-if="showConcatDialog"
      :selected-waylines="selectedWaylines"
      :loading="concatLoading"
      @confirm="handleConcatConfirm"
      @cancel="showConcatDialog = false"
    />

    <!-- 删除确认对话框 -->
    <WaylineDeleteDialog
      v-if="showDeleteDialog"
      :selected-waylines="selectedWaylines"
      :loading="deleteLoading"
      @confirm="handleDeleteConfirm"
      @cancel="showDeleteDialog = false"
    />

    <!-- 导入对话框 -->
    <WaylineImportDialog
      v-if="showImportDialog"
      :loading="importLoading"
      :error="importError"
      @confirm="handleImportConfirm"
      @cancel="showImportDialog = false"
    />
  </div>
</template>

<style scoped>
.wayline-library {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--app-bg);
}

/* ── 工具栏 ── */

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  flex-shrink: 0;
}

.toolbar-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text);
  margin: 0;
  letter-spacing: -0.02em;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.action-btn--primary {
  background: var(--app-primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(6, 182, 212, 0.25);
}

.action-btn--primary:hover {
  background: var(--app-primary-hover);
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.35);
  transform: translateY(-1px);
}

.action-btn--ghost {
  background: var(--app-bg-surface);
  border-color: var(--app-border);
  color: var(--app-text-secondary);
}

.action-btn--ghost:hover:not(:disabled) {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.action-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.action-btn--danger {
  background: var(--app-bg-surface);
  border-color: var(--app-border);
  color: var(--app-danger);
}

.action-btn--danger:hover:not(:disabled) {
  background: var(--app-danger-bg);
  border-color: var(--app-danger);
}

.select-count {
  font-size: 12px;
  color: var(--app-primary);
  font-weight: 600;
}

.select-count--danger {
  color: var(--app-danger);
}

/* ── 过滤器 ── */

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px 16px;
  flex-shrink: 0;
}

.filter-group {
  display: flex;
  gap: 8px;
}

.filter-select {
  padding: 7px 12px;
  padding-right: 28px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  cursor: pointer;
  outline: none;
  transition: all var(--transition-base);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' fill='none' stroke='%236b7280' stroke-width='2' stroke-linecap='round'%3E%3Cpath d='M3 4.5 6 7.5 9 4.5'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 8px center;
}

.filter-select:hover {
  border-color: var(--app-border-strong);
  background-color: var(--app-bg-card);
}

.filter-select:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 2px var(--app-primary-bg);
}

.filter-search {
  flex: 1;
  position: relative;
  max-width: 280px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--app-text-muted);
  pointer-events: none;
  transition: color var(--transition-base);
}

.filter-search:focus-within .search-icon {
  color: var(--app-primary);
}

.search-input {
  width: 100%;
  padding: 7px 12px 7px 32px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
  transition: all var(--transition-base);
}

.search-input:hover {
  border-color: var(--app-border-strong);
}

.search-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 2px var(--app-primary-bg);
}

.search-input::placeholder {
  color: var(--app-text-muted);
  opacity: 0.7;
}

.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
  margin-left: auto;
}

.refresh-btn:hover {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.spinning {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ── 错误横幅 ── */

.error-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 24px;
  margin: 0 24px 12px;
  background: var(--app-danger-bg);
  color: var(--app-danger);
  font-size: 13px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  border: 1px solid rgba(248, 113, 113, 0.2);
}

.error-retry {
  padding: 4px 10px;
  border: 1px solid var(--app-danger);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-danger);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.error-retry:hover {
  background: var(--app-danger);
  color: #fff;
}

/* ── 表格 ── */

.table-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding: 0 24px;
}

.wayline-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 2px;
  table-layout: auto;
}

.wayline-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.wayline-table th {
  padding: 10px 16px;
  text-align: left;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-muted);
  background: var(--app-bg);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
  border-bottom: 1px solid var(--app-border);
}

.wayline-table td {
  padding: 14px 16px;
  font-size: 13px;
  color: var(--app-text-secondary);
  background: var(--app-bg-panel);
  border-top: 1px solid transparent;
  border-bottom: 1px solid transparent;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.wayline-table tr td:first-child {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
}

.wayline-table tr td:last-child {
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.col-check { width: 44px; text-align: center; }
.col-name { width: 28%; }
.col-type { width: 80px; }
.col-waypoints { width: 64px; text-align: center; }
.col-distance { width: 72px; }
.col-drone { width: 140px; }
.col-payload { width: 130px; }
.col-time { width: 108px; }
.col-action { width: 48px; text-align: center; }

/* ── 自定义 Checkbox ── */

.wayline-table input[type="checkbox"] {
  appearance: none;
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--app-border-strong);
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  position: relative;
  transition: all var(--transition-base);
  vertical-align: middle;
}

.wayline-table input[type="checkbox"]:hover {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
}

.wayline-table input[type="checkbox"]:checked {
  background: var(--app-primary);
  border-color: var(--app-primary);
}

.wayline-table input[type="checkbox"]:checked::after {
  content: '';
  position: absolute;
  left: 4.5px;
  top: 1.5px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 1.5px 1.5px 0;
  transform: rotate(45deg);
}

.wayline-table input[type="checkbox"]:indeterminate {
  background: var(--app-primary);
  border-color: var(--app-primary);
}

.wayline-table input[type="checkbox"]:indeterminate::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 6.5px;
  width: 8px;
  height: 1.5px;
  background: #fff;
  border-radius: 1px;
}

/* ── 行样式 ── */

.wayline-row {
  transition: all var(--transition-base);
}

.wayline-row td {
  background: var(--app-bg-panel);
}

.wayline-row:hover td {
  background: var(--app-bg-card);
}

.wayline-row--selected td {
  background: var(--app-primary-bg);
  border-top-color: rgba(6, 182, 212, 0.15);
  border-bottom-color: rgba(6, 182, 212, 0.15);
}

.wayline-row--selected:hover td {
  background: var(--app-primary-bg-hover);
}

.wayline-name {
  font-weight: 500;
  color: var(--app-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 240px;
}

.drone-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.01em;
  background: rgba(6, 182, 212, 0.12);
  color: var(--app-primary);
  border: 1px solid rgba(6, 182, 212, 0.2);
}

/* ── 航线类型标签 ── */

.type-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  border: 1px solid var(--app-border);
}

/* 类型标签配色按航线类型语义分组：
   - 航点航线（核心巡检）：info 蓝
   - 面状/带状航线（航拍测绘）：success 绿
   - 斜面/几何体/贴近摄影（建模）：warning 黄
   - AI 巡逻（智能）：primary 青
   未匹配类型回退到 .type-tag 默认灰 */
.type-tag--航点航线 {
  background: rgba(96, 165, 250, 0.12);
  color: var(--app-info);
  border-color: rgba(96, 165, 250, 0.25);
}

.type-tag--面状航线,
.type-tag--带状航线 {
  background: rgba(52, 211, 153, 0.12);
  color: var(--app-success);
  border-color: rgba(52, 211, 153, 0.25);
}

.type-tag--斜面航线,
.type-tag--几何体航线,
.type-tag--贴近摄影航线 {
  background: rgba(251, 191, 36, 0.12);
  color: var(--app-warning);
  border-color: rgba(251, 191, 36, 0.25);
}

.type-tag--AI巡逻航线 {
  background: rgba(6, 182, 212, 0.12);
  color: var(--app-primary);
  border-color: rgba(6, 182, 212, 0.2);
}

/* ── 航点数 ── */

.waypoint-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--app-text-secondary);
  font-variant-numeric: tabular-nums;
}

.waypoint-icon {
  color: var(--app-text-muted);
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
  opacity: 0.5;
}

.wayline-row:hover .edit-btn {
  opacity: 1;
}

.edit-btn:hover {
  background: var(--app-primary-bg);
  color: var(--app-primary);
  transform: scale(1.1);
}

.empty-cell {
  text-align: center;
  padding: 64px 12px;
  color: var(--app-text-muted);
  font-size: 14px;
  background: transparent !important;
}

/* ── 分页 ── */

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  flex-shrink: 0;
}

.pagination-info {
  font-size: 12px;
  color: var(--app-text-muted);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
}

.page-btn:hover:not(:disabled) {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.page-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.page-current {
  font-size: 13px;
  color: var(--app-text-secondary);
  min-width: 56px;
  text-align: center;
  font-weight: 500;
}
</style>
