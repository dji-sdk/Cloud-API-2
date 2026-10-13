<!--
  Copyright (c) 2026 SZ DJI Technology Co., Ltd.
  SPDX-License-Identifier: Apache-2.0
-->

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  drcConnected: boolean
  liveStarted: boolean
  videoProjectionEnabled: boolean
}>()

const collapsed = ref(false)

interface LegendItem {
  swatch: string
  label: string
  src: string
  detail?: string
  needDrc?: boolean
  needLive?: boolean
}

const groups: { title: string; items: LegendItem[] }[] = [
  {
    title: 'MQTT 慢通道',
    items: [
      { swatch: 'model', label: '飞机模型', src: '位置 / 姿态' },
      { swatch: 'dock', label: '机场', src: 'dockLocation' },
    ],
  },
  {
    title: 'SEI · 需直播',
    items: [
      {
        swatch: 'axis',
        label: '相机三轴',
        src: '码流 SEI',
        detail: '红 +Z 光轴 · 绿 +Y 画面下 · 蓝 +X 画面右',
        needLive: true,
      },
      { swatch: 'frustum', label: 'FPV 视锥', src: 'SEI FOV', needLive: true },
      { swatch: 'footprint', label: '地面交线', src: '黄框', needLive: true },
      { swatch: 'sei', label: '橙点', src: '光轴几何落点', needLive: true },
      { swatch: 'projection', label: '贴地图', src: '视频投影', needLive: true },
    ],
  },
  {
    title: 'DRC · 需连接',
    items: [{ swatch: 'fast', label: '绿点', src: '激光实测落点', needDrc: true }],
  },
]

function itemMuted(
  item: LegendItem,
  drcConnected: boolean,
  liveStarted: boolean,
  videoProjectionEnabled: boolean,
): boolean {
  if (item.needDrc && !drcConnected) return true
  if (item.needLive && !liveStarted) return true
  if (item.swatch === 'projection' && liveStarted && !videoProjectionEnabled) return true
  return false
}

function itemSrc(item: LegendItem, videoProjectionEnabled: boolean): string {
  if (item.swatch === 'projection' && !videoProjectionEnabled) {
    return '视频投影（关）'
  }
  return item.src
}
</script>

<template>
  <div class="map-legend" :class="{ collapsed }">
    <div class="map-legend-hd">
      <span>地图图例</span>
      <button type="button" class="map-legend-toggle" @click="collapsed = !collapsed">
        {{ collapsed ? '展开' : '收起' }}
      </button>
    </div>
    <div v-show="!collapsed" class="map-legend-body">
      <section v-for="group in groups" :key="group.title" class="map-legend-group">
        <div class="map-legend-group-title">{{ group.title }}</div>
        <ul class="map-legend-list">
          <li v-for="item in group.items" :key="item.label" class="map-legend-item">
            <div class="map-legend-row">
              <span class="map-legend-mark" :class="`mark-${item.swatch}`" aria-hidden="true" />
              <span class="map-legend-label">{{ item.label }}</span>
              <span
                class="map-legend-src"
                :class="{ muted: itemMuted(item, props.drcConnected, props.liveStarted, props.videoProjectionEnabled) }"
              >
                {{ itemSrc(item, props.videoProjectionEnabled) }}
              </span>
            </div>
            <p
              v-if="item.detail"
              class="map-legend-detail"
              :class="{ muted: itemMuted(item, props.drcConnected, props.liveStarted, props.videoProjectionEnabled) }"
            >
              {{ item.detail }}
            </p>
          </li>
        </ul>
      </section>
      <p class="map-legend-tip">
        绿点（DRC 实测）vs 橙点（SEI 几何）均对应<strong>画面中心</strong>
      </p>
    </div>
  </div>
</template>
