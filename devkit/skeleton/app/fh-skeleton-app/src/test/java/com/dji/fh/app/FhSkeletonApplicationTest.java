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

package com.dji.fh.app;

import com.dji.fh.app.security.SecurityFilterAutoConfiguration;
import com.dji.fh.kernel.impl.async.AsyncAutoConfiguration;
import com.dji.fh.kernel.impl.event.EventBusAutoConfiguration;
import com.dji.fh.kernel.mocksdk.MockSdkAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证默认 mock profile 下的全 AutoConfig 装配。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class FhSkeletonApplicationTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    void contextLoads_shouldHaveAllAutoConfigs_whenMockProfile() {
        assertThat(ctx).isNotNull();
        // A2 AutoConfig 都应在
        assertThat(ctx.getBean(AsyncAutoConfiguration.class)).isNotNull();
        assertThat(ctx.getBean(EventBusAutoConfiguration.class)).isNotNull();
        // A3 Mock + Security AutoConfig
        assertThat(ctx.getBean(MockSdkAutoConfiguration.class)).isNotNull();
        assertThat(ctx.getBean(SecurityFilterAutoConfiguration.class)).isNotNull();
    }
}
