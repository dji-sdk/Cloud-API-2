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

package com.dji.fh.feature.chat.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link ProviderType} 枚举单元测试。
 *
 * @author DJI DevKit Team
 */
class ProviderTypeTest {

    @Test
    void values_shouldContainExpectedProviders() {
        assertThat(ProviderType.values()).containsExactly(
                ProviderType.ANTHROPIC,
                ProviderType.OPENAI,
                ProviderType.DASHSCOPE,
                ProviderType.DEEPSEEK);
    }
}
