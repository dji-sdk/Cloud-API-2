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

package com.dji.fh.feature.wayline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class WaylineAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WaylineAutoConfiguration.class));

    @Test
    void autoConfig_shouldRegisterBeans_byDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(WaylineService.class);
            assertThat(ctx).hasSingleBean(WaylineRestController.class);
        });
    }

    @Test
    void autoConfig_shouldBeDisabled_whenPropertyFalse() {
        runner.withPropertyValues("fh.skeleton.feature.wayline.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(WaylineService.class);
                    assertThat(ctx).doesNotHaveBean(WaylineRestController.class);
                });
    }

    @Test
    void autoConfig_shouldBackOff_whenCustomBeanPresent() {
        runner.withBean(WaylineService.class, () -> new WaylineService(new WaylineProperties(10, 50, true)))
                .run(ctx -> assertThat(ctx).hasSingleBean(WaylineService.class));
    }
}
