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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppTopNav from '@/components/AppTopNav.vue'
import ChatDrawer from '@/components/ChatDrawer/ChatDrawer.vue'
import ToastContainer from '@/components/ToastContainer.vue'

const route = useRoute()

const isPublicPage = computed(() => !!route.meta.public)
</script>

<template>
  <div v-if="isPublicPage" class="layout-fullscreen">
    <router-view />
  </div>
  <div v-else class="layout-app">
    <AppTopNav />
    <main class="layout-main">
      <router-view v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>
    <ChatDrawer />
    <ToastContainer />
  </div>
</template>

<style scoped>
.layout-fullscreen {
  height: 100vh;
}

.layout-app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.layout-main {
  flex: 1;
  overflow-y: auto;
}
</style>
