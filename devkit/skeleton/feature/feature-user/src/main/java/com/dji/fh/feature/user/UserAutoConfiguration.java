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

package com.dji.fh.feature.user;

import com.dji.fh.feature.user.controller.AuthController;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.feature.user.security.AuthenticatingSecurityFilter;
import com.dji.fh.feature.user.security.JwtAuthenticationProvider;
import com.dji.fh.feature.user.security.JwtTokenProvider;
import com.dji.fh.feature.user.service.UserService;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 用户模块自动装配。
 *
 * <p>默认启用。ISV 设 {@code fh.user.enabled=false} 可完全禁用。
 * 通过 {@code @AutoConfigureOrder} 确保在 kernel AuthAutoConfiguration 之前加载，
 * 使 JwtAuthenticationProvider 优先于 NoOpAuthenticationProvider 注册。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(name = "fh.user.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(UserProperties.class)
@EntityScan(basePackages = "com.dji.fh.feature.user.entity")
@EnableJpaRepositories(basePackages = "com.dji.fh.feature.user.repository")
public class UserAutoConfiguration {

    /**
     * 装配 JwtTokenProvider。
     *
     * @param props 用户模块配置
     * @return JwtTokenProvider 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(UserProperties props) {
        return new JwtTokenProvider(props);
    }

    /**
     * 装配 JwtAuthenticationProvider，优先级 100（高于 NoOp 的 MAX_VALUE）。
     *
     * @param tokenProvider  JWT token 签发/验证器
     * @param userRepository 用户数据访问（用于密码版本校验）
     * @return AuthenticationProvider 实例
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    public AuthenticationProvider jwtAuthenticationProvider(JwtTokenProvider tokenProvider,
                                                           UserRepository userRepository) {
        return new JwtAuthenticationProvider(tokenProvider, userRepository);
    }

    /**
     * 装配 UserService。
     *
     * @param userRepository 用户数据访问
     * @param tokenProvider  JWT token 签发器
     * @param props          用户模块配置
     * @return UserService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public UserService userService(UserRepository userRepository,
                                   JwtTokenProvider tokenProvider,
                                   UserProperties props) {
        return new UserService(userRepository, tokenProvider, props);
    }

    /**
     * 装配 AuthController。
     *
     * @param userService 用户业务逻辑
     * @return AuthController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(UserService userService) {
        return new AuthController(userService);
    }

    /**
     * 装配认证 Security Filter，替换 app 层的 NoOpSecurityFilter。
     *
     * <p>Bean 名为 "securityFilter"，触发 {@code SecurityFilterAutoConfiguration} 中
     * {@code @ConditionalOnMissingBean(name="securityFilter")} 的退让机制。
     *
     * @param authenticationProvider 认证 Provider（JwtAuthenticationProvider）
     * @return FilterRegistrationBean 包装认证 Filter
     */
    @Bean(name = "securityFilter")
    public FilterRegistrationBean<AuthenticatingSecurityFilter> authenticatingSecurityFilter(
            AuthenticationProvider authenticationProvider) {
        FilterRegistrationBean<AuthenticatingSecurityFilter> reg =
                new FilterRegistrationBean<>(new AuthenticatingSecurityFilter(authenticationProvider));
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        reg.addUrlPatterns("/*");
        reg.setName("authenticatingSecurityFilter");
        return reg;
    }
}
