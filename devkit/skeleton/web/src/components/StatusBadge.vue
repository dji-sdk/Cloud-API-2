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

<template>
  <span :class="['status-badge', online ? 'status-badge--online' : 'status-badge--offline']">
    {{ online ? '在线' : '离线' }}
  </span>
</template>

<script setup lang="ts">
defineProps<{ online: boolean }>()
</script>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge--online {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.12);
}

.status-badge--offline {
  color: #8c8c8c;
  background: rgba(140, 140, 140, 0.12);
}
</style>
