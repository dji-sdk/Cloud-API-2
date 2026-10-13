<!--
  Copyright (c) 2026 SZ DJI Technology Co., Ltd.
  SPDX-License-Identifier: Apache-2.0
-->

<script setup lang="ts">
defineProps<{
  drcConnected: boolean
}>()
</script>

<template>
  <div class="video-center-hint" aria-hidden="true">
    <div class="video-center-cross" />
    <div class="video-center-label">
      <div>画面中心</div>
      <div class="video-center-sub">光轴 / 激光测距参考</div>
    </div>
    <div class="video-center-footnote">
      空间标注仅在地图，不在 video 上叠 AR
      <span v-if="drcConnected"> · 按住画面拖动 → 云台</span>
    </div>
  </div>
</template>
