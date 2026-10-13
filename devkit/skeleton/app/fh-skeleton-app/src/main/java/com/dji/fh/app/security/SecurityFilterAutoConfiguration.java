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

package com.dji.fh.app.security;

import com.dji.fh.kernel.spi.auth.PublicPaths;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * HTTP 安全过滤器的默认装配。
 *
 * <p>提供名为 {@code "securityFilter"} 的 {@link FilterRegistrationBean}，
 * 包装 {@link NoOpSecurityFilter} 拦截 {@code /*}，order = {@code HIGHEST_PRECEDENCE + 100}
 * 让 ISV 自家 filter 默认在其之后运行。
 *
 * <p>ISV 想换真鉴权 filter：定义自家 {@code @Bean(name="securityFilter") FilterRegistrationBean<MyFilter>}，
 * 本默认 bean 因 {@link ConditionalOnMissingBean} 而让位。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
public class SecurityFilterAutoConfiguration {
    /**
     * 无需登录即可匿名访问的公开路径 — 唯一事实来源为 kernel-spi
     * {@link PublicPaths}（安全发布专项）。
     *
     * <p>feature-user 禁用时本段随 NoOp filter 同步放行，保证 NoOp 模式下挂载的
     * 公开端点仍可达。保留本字段仅为向后兼容 ISV 对 {@code PUBLIC_PATHS} 的引用，
     * 内容与 {@link PublicPaths#paths()} 完全一致，不再单独维护第二套清单。
     */
    public static final String[] PUBLIC_PATHS = PublicPaths.paths();

    /**
     * 默认 filter 的 order，留 100 余量让 ISV filter 排在其之后。
     */
    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    /**
     * 装配默认 NoOpSecurityFilter，bean 名 "securityFilter"。
     *
     * @return FilterRegistrationBean 包装 NoOpSecurityFilter
     */
    @Bean(name = "securityFilter")
    @ConditionalOnMissingBean(name = "securityFilter")
    public FilterRegistrationBean<NoOpSecurityFilter> noOpSecurityFilter() {
        FilterRegistrationBean<NoOpSecurityFilter> reg =
                new FilterRegistrationBean<>(new NoOpSecurityFilter());
        reg.setOrder(DEFAULT_ORDER);
        reg.addUrlPatterns("/*");
        reg.setName("noOpSecurityFilter");
        return reg;
    }
}
