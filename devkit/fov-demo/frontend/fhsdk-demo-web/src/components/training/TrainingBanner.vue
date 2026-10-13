<!--
  Copyright (c) 2026 SZ DJI Technology Co., Ltd.
  SPDX-License-Identifier: Apache-2.0
-->

<script setup lang="ts">
/** 顶栏下方：三层数据通道摘要（培训话术） */
</script>

<template>
  <div class="training-banner">
    <span class="training-banner-item">
      <strong>MQTT OSD</strong> ~0.5Hz → 飞机/机场模型、信息条
    </span>
    <span class="training-banner-sep">｜</span>
    <span class="training-banner-item">
      <strong>SEI</strong> ~30fps（需直播）→ 相机三轴、视锥、光轴落点
    </span>
    <span class="training-banner-sep">｜</span>
    <span class="training-banner-item">
      <strong>DRC OSD</strong>（需 DRC）→ 激光测距落点（画面中心，设备实测）
    </span>
  </div>
</template>
