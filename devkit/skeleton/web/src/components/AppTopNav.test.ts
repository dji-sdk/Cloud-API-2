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

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import { createPinia } from 'pinia'
import AppTopNav from './AppTopNav.vue'
import { useChatStore } from '@/stores/chat'
import type { PaasUnavailableReason } from '@/utils/paasMessages'

// vi.mock 工厂会被提升到 import 之前执行，因此被引用的可变状态必须用 vi.hoisted 一并提升
const probeState = vi.hoisted(() => ({
  chatAvailable: false,
  chatProbed: false,
  paasAvailable: false,
  paasProbed: false,
  paasReason: null as PaasUnavailableReason | null,
  paasAddress: '',
}))

vi.mock('vue-router', () => ({
  useRoute: (): { path: string } => ({ path: '/' }),
}))

// UserDropdown 会拉起 auth/theme store 与路由跳转，与本组件要验证的入口逻辑无关
vi.mock('./UserDropdown.vue', async () => {
  const { defineComponent, h } = await import('vue')
  return {
    default: defineComponent({
      name: 'UserDropdown',
      setup() {
        return () => h('div', { class: 'user-dropdown-stub' })
      },
    }),
  }
})

// 用 computed 包住 hoisted 的普通对象，既能保留响应式，又避免工厂内持有 ref
vi.mock('@/composables/useChatAvailability', async () => {
  const { computed } = await import('vue')
  return {
    useChatAvailability: () => ({
      available: computed(() => probeState.chatAvailable),
      probed: computed(() => probeState.chatProbed),
      refresh: vi.fn(),
    }),
  }
})

vi.mock('@/composables/usePaasAvailability', async () => {
  const { computed } = await import('vue')
  return {
    usePaasAvailability: () => ({
      availability: computed(() => ({
        available: probeState.paasAvailable,
        reason: probeState.paasReason,
        address: probeState.paasAddress,
      })),
      probed: computed(() => probeState.paasProbed),
      refresh: vi.fn(),
    }),
  }
})

/** 不依赖底座的入口，任何探测结果下都应保持可跳转 */
const PLAIN_PATHS = ['/', '/flight-tasks', '/media', '/flight-areas', '/models']

/** 导航栏的完整渲染顺序（置灰项会被替换为 span，故需按序整体断言） */
const ALL_PATHS = [
  '/',
  '/live-stream',
  '/devices',
  '/cockpit',
  '/wayline',
  '/flight-tasks',
  '/media',
  '/flight-areas',
  '/models',
]

function mountTopNav() {
  return mount(AppTopNav, {
    global: {
      plugins: [createPinia()],
      // 未安装 vue-router，必须显式替换 router-link，否则渲染出未解析的组件
      stubs: { RouterLink: RouterLinkStub },
    },
  })
}

/** 收集当前渲染出的可跳转链接的 to 属性 */
function renderedLinkPaths(wrapper: ReturnType<typeof mountTopNav>): string[] {
  return wrapper.findAllComponents(RouterLinkStub).map((link) => String(link.props('to')))
}

/** 收集当前渲染为置灰 span 的导航项文字 */
function renderedDisabledLabels(wrapper: ReturnType<typeof mountTopNav>): string[] {
  return wrapper.findAll('.nav-item--disabled').map((item) => item.text())
}

describe('AppTopNav', () => {
  beforeEach(() => {
    probeState.chatAvailable = false
    probeState.chatProbed = false
    probeState.paasAvailable = false
    probeState.paasProbed = false
    probeState.paasReason = null
    probeState.paasAddress = ''
  })

  describe('AI 助手入口', () => {
    it('AppTopNav_shouldHideAiButton_whenProbeNotFinished', () => {
      const wrapper = mountTopNav()

      // 探测未完成就渲染会让关闭场景下按钮闪一下再消失
      expect(wrapper.find('.ai-btn').exists()).toBe(false)
    })

    it('AppTopNav_shouldHideAiButton_whenChatDisabled', () => {
      probeState.chatProbed = true
      probeState.chatAvailable = false
      const wrapper = mountTopNav()

      expect(wrapper.find('.ai-btn').exists()).toBe(false)
    })

    it('AppTopNav_shouldShowAiButton_whenChatEnabled', () => {
      probeState.chatProbed = true
      probeState.chatAvailable = true
      const wrapper = mountTopNav()

      expect(wrapper.find('.ai-btn').exists()).toBe(true)
    })

    it('AppTopNav_shouldToggleDrawer_whenAiButtonClicked', async () => {
      probeState.chatProbed = true
      probeState.chatAvailable = true
      const wrapper = mountTopNav()
      const chatStore = useChatStore()

      expect(chatStore.isOpen).toBe(false)
      await wrapper.find('.ai-btn').trigger('click')
      expect(chatStore.isOpen).toBe(true)
    })

    it('AppTopNav_shouldMarkAiButtonActive_whenDrawerOpen', async () => {
      probeState.chatProbed = true
      probeState.chatAvailable = true
      const wrapper = mountTopNav()

      const chatStore = useChatStore()
      chatStore.toggleDrawer()
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.ai-btn').classes()).toContain('ai-btn--active')
    })
  })

  describe('底座依赖入口', () => {
    it('AppTopNav_shouldRenderAllEntriesAsLinks_whenProbeNotFinished', () => {
      const wrapper = mountTopNav()

      // 探测完成前不置灰：否则 available 初始 false 会让入口先灰后亮
      expect(renderedDisabledLabels(wrapper)).toEqual([])
      expect(renderedLinkPaths(wrapper)).toEqual(ALL_PATHS)
    })

    it('AppTopNav_shouldRenderAllEntriesAsLinks_whenPaasAvailable', () => {
      probeState.paasProbed = true
      probeState.paasAvailable = true
      const wrapper = mountTopNav()

      expect(renderedDisabledLabels(wrapper)).toEqual([])
      expect(renderedLinkPaths(wrapper)).toEqual(ALL_PATHS)
    })

    it('AppTopNav_shouldDisablePaasEntries_whenPaasUnreachable', () => {
      probeState.paasProbed = true
      probeState.paasAvailable = false
      probeState.paasReason = 'unreachable'
      probeState.paasAddress = 'http://10.0.0.1'
      const wrapper = mountTopNav()

      expect(renderedDisabledLabels(wrapper)).toEqual(['直播大屏', '设备管理', '驾驶舱', '航线编辑器'])
      // 置灰项不能残留 router-link，否则键盘仍可聚焦跳转
      expect(renderedLinkPaths(wrapper)).toEqual(PLAIN_PATHS)
    })

    it('AppTopNav_shouldExplainHowToConfigure_whenPaasNotConfigured', () => {
      probeState.paasProbed = true
      probeState.paasAvailable = false
      probeState.paasReason = 'not-configured'
      const wrapper = mountTopNav()

      const firstDisabled = wrapper.find('.nav-item--disabled')
      expect(firstDisabled.attributes('title')).toBe('需要底座：尚未配置 FH2 上云 API 地址')
      expect(firstDisabled.attributes('aria-disabled')).toBe('true')
    })

    it('AppTopNav_shouldIncludeAddressInHint_whenPaasUnreachable', () => {
      probeState.paasProbed = true
      probeState.paasAvailable = false
      probeState.paasReason = 'unreachable'
      probeState.paasAddress = 'http://10.0.0.1'
      const wrapper = mountTopNav()

      expect(wrapper.find('.nav-item--disabled').attributes('title'))
        .toBe('需要底座：http://10.0.0.1 未响应')
    })
  })
})
