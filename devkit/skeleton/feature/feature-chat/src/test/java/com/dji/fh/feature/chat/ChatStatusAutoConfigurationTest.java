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

package com.dji.fh.feature.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.chat.controller.ChatStatusController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * {@link ChatStatusAutoConfiguration} 装配切片测试。
 *
 * <p>核心断言：无论 {@code fh.chat.enabled} 取何值，状态探测接口都必须注册成功。
 * 这是「关闭功能时探测仍返回 200」的装配层保证。
 *
 * @author DJI DevKit Team
 */
class ChatStatusAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ChatStatusAutoConfiguration.class));

    @Test
    void shouldRegisterStatusController_whenChatDisabled() {
        runner.withPropertyValues("fh.chat.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(ChatStatusController.class);
                    assertThat(ctx.getBean(ChatProperties.class).isEnabled()).isFalse();
                });
    }

    @Test
    void shouldRegisterStatusController_whenChatEnabled() {
        runner.withPropertyValues("fh.chat.enabled=true")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(ChatStatusController.class);
                    assertThat(ctx.getBean(ChatProperties.class).isEnabled()).isTrue();
                });
    }

    @Test
    void shouldDefaultToDisabled_whenPropertyAbsent() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ChatStatusController.class);
            assertThat(ctx.getBean(ChatProperties.class).isEnabled()).isFalse();
        });
    }

    @Test
    void shouldAllowIsvOverride_whenCustomControllerProvided() {
        ChatStatusController custom = new ChatStatusController(new ChatProperties());

        runner.withBean(ChatStatusController.class, () -> custom)
                .run(ctx -> assertThat(ctx.getBean(ChatStatusController.class)).isSameAs(custom));
    }
}
