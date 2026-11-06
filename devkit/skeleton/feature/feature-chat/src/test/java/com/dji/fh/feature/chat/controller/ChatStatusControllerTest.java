/*
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

package com.dji.fh.feature.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dji.fh.feature.chat.ChatProperties;
import com.dji.fh.kernel.spi.auth.PublicEndpoint;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * {@link ChatStatusController} 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>功能关闭时返回 {@code enabled=false}（默认值）</li>
 *   <li>功能开启时返回 {@code enabled=true}</li>
 *   <li>真实 HTTP GET 返回 200 + {@code {"enabled":…}}（验证映射与序列化）</li>
 *   <li>类级 {@link RequireAuthenticated} + 方法级 {@link PublicEndpoint} 配对存在，
 *       保证 {@code AuthorizationAspect} 命中切点后能读到豁免</li>
 *   <li>状态端点映射路径为 {@code /status}（类级前缀 {@code /api/v1/chat}）</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class ChatStatusControllerTest {

    private ChatProperties properties;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        properties = new ChatProperties();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatStatusController(properties))
                .build();
    }

    @Test
    void getStatus_shouldReturnDisabled_whenChatDisabled() {
        ChatStatusController controller = new ChatStatusController(new ChatProperties());

        assertThat(controller.getStatus().enabled()).isFalse();
    }

    @Test
    void getStatus_shouldReturnEnabled_whenChatEnabled() {
        ChatProperties enabledProperties = new ChatProperties();
        enabledProperties.setEnabled(true);
        ChatStatusController controller = new ChatStatusController(enabledProperties);

        assertThat(controller.getStatus().enabled()).isTrue();
    }

    @Test
    void getStatus_shouldReturn200WithDisabledBody_whenChatDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void getStatus_shouldReturn200WithEnabledBody_whenChatEnabled() throws Exception {
        properties.setEnabled(true);

        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void getStatus_shouldPairPublicEndpointWithClassLevelAuth_whenInvokedAnonymously()
            throws NoSuchMethodException {
        Method method = ChatStatusController.class.getMethod("getStatus");

        // 方法级豁免必须真的存在，否则切面不会放行匿名请求
        assertThat(method.getAnnotation(PublicEndpoint.class)).isNotNull();
        // 类级鉴权必须存在，否则 AuthorizationAspect 的切点不命中，上面的豁免注解成为死注解
        assertThat(ChatStatusController.class.getAnnotation(RequireAuthenticated.class)).isNotNull();
    }

    @Test
    void getStatus_shouldMapToStatusPath_whenRequested() throws NoSuchMethodException {
        Method method = ChatStatusController.class.getMethod("getStatus");

        assertThat(method.getAnnotation(GetMapping.class).value()).containsExactly("/status");
    }
}
