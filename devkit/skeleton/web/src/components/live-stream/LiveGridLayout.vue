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
import { computed, type CSSProperties } from 'vue'
import type { GridLayoutKey, PlayerSlot } from '@/types/liveStream'
import { LAYOUT_CONFIGS } from './layoutConfig'
import { getContainerId } from './utils'
import LiveGridSlot from './LiveGridSlot.vue'

const props = defineProps<{
  layout: GridLayoutKey
  slots: PlayerSlot[]
}>()

const emit = defineEmits<{
  close: [index: number]
}>()

const config = computed(() => LAYOUT_CONFIGS[props.layout])
const isSurround = computed(() => props.layout === 'c13')

const gridStyle = computed((): CSSProperties => {
  if (isSurround.value) {
    return {
      display: 'grid',
      gridTemplateColumns: 'repeat(4, 1fr)',
      gridTemplateRows: 'repeat(4, 1fr)',
      gridTemplateAreas: `
        "s0 s1 s2 s3"
        "s4 main main s5"
        "s6 main main s7"
        "s8 s9 s10 s11"
      `.trim(),
      gap: '2px',
    }
  }
  const repeat = config.value.repeat
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${repeat}, 1fr)`,
    gridTemplateRows: `repeat(${repeat}, 1fr)`,
    gap: '2px',
  }
})

function getSlotStyle(index: number): CSSProperties | undefined {
  if (isSurround.value) {
    return index === 0
      ? { gridArea: 'main' }
      : { gridArea: `s${index - 1}` }
  }
  const span = config.value.firstItemSpan
  if (span && index === 0) {
    return {
      gridColumn: `1 / ${span + 1}`,
      gridRow: `1 / ${span + 1}`,
    }
  }
  return undefined
}
</script>

<template>
  <div class="live-grid" :style="gridStyle">
    <LiveGridSlot
      v-for="(slot, index) in slots"
      :key="slot.id ?? `empty-${index}`"
      :slot="slot"
      :container-id="getContainerId(slot)"
      :style="getSlotStyle(index)"
      @close="emit('close', index)"
    />
  </div>
</template>

<style scoped>
.live-grid {
  width: 100%;
  height: 100%;
  background: var(--app-bg);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
</style>
