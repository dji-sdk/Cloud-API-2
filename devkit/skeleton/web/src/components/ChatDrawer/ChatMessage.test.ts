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

import { it, expect, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DOMPurify from 'dompurify'
import ChatMessage from './ChatMessage.vue'

const base = { id: '1', role: 'assistant' as const, timestamp: 0 }
const render = (content: string) =>
  mount(ChatMessage, { props: { message: { ...base, content } } })

afterEach(() => {
  // 断言失败时也能恢复 spy，避免污染后续用例
  vi.restoreAllMocks()
})

it('shouldEscapeRawHtml_whenAssistantContentContainsTags', () => {
  const wrapper = render('<img src=x onerror="alert(1)">\n\n<svg onload=alert(1)></svg>')
  expect(wrapper.find('.chat-message__markdown').exists()).toBe(true)
  expect(wrapper.find('img').exists()).toBe(false)
  expect(wrapper.find('svg').exists()).toBe(false)
})

it('shouldNotCreateLink_whenMarkdownLinkUsesJavascriptScheme', () => {
  expect(render('[x](javascript:alert(1))').find('a').exists()).toBe(false)
})

it('shouldKeepHttpsLink_whenMarkdownLinkIsValid', () => {
  expect(render('[ok](https://example.com)').find('a').attributes('href'))
    .toBe('https://example.com')
})

it('shouldKeepMarkdownStructure_whenRenderingTableCodeAndImage', () => {
  const w = render('|a|b|\n|-|-|\n|1|2|\n\n```js\nconst a=1\n```\n\n![i](https://example.com/a.png)')
  expect(w.find('table').exists()).toBe(true)
  expect(w.find('pre code.language-js').exists()).toBe(true)
  expect(w.find('img').attributes('src')).toBe('https://example.com/a.png')
})

it('shouldRenderUserMessageAsPlainText_whenRoleIsUser', () => {
  const w = mount(ChatMessage, {
    props: { message: { ...base, role: 'user' as const, content: '**b** <b>x</b>' } },
  })
  expect(w.find('.chat-message__markdown').exists()).toBe(false)
  expect(w.find('.chat-message__bubble--user').text()).toBe('**b** <b>x</b>')
})

it('shouldInvokeDOMPurifyWithForbidConfig_whenRenderingAssistantContent', () => {
  const spy = vi.spyOn(DOMPurify, 'sanitize')
  render('**x**')
  expect(spy).toHaveBeenCalledTimes(1)
  // 断言配置实际传入，防止未来误删 FORBID_* 兜底
  expect(spy.mock.calls[0][1]).toMatchObject({
    USE_PROFILES: { html: true },
    FORBID_ATTR: ['style'],
    FORBID_TAGS: expect.arrayContaining([
      'style', 'form', 'input', 'button', 'select', 'textarea',
      'iframe', 'object', 'embed', 'link', 'meta', 'base',
    ]),
  })
})
