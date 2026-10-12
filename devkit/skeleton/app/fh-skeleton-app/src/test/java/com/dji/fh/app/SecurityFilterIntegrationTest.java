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

import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证默认 NoOpSecurityFilter 已注册且 actuator 端点可访问（说明 filter 不会误拦截）。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class SecurityFilterIntegrationTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void securityFilter_shouldBeRegistered_whenApplicationStarts() {
        FilterRegistrationBean<?> reg = ctx.getBean("securityFilter", FilterRegistrationBean.class);
        assertThat(reg).isNotNull();
        assertThat(reg.getUrlPatterns()).contains("/*");
    }

    @Test
    void healthEndpoint_shouldRespond_whenFilterDoesNotBlock() {
        // TestRestTemplate 在 RANDOM_PORT 下会自动拼接 server.servlet.context-path，
        // 故此处传相对路径，实际 wire 请求为 /pf-devkit/ai-plugin/skeleton/actuator/health
        ResponseEntity<String> resp = rest.getForEntity("/actuator/health", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("UP");
        // filter 自己 clear 后，main thread 看到的应是 NoOp
        assertThat(SecurityContextHolder.current()).isSameAs(NoOpSecurityContext.INSTANCE);
    }
}
