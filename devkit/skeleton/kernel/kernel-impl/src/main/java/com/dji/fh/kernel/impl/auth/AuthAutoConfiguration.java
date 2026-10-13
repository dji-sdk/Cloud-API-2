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

package com.dji.fh.kernel.impl.auth;

import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.PermissionEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auth 子系统的默认装配。
 *
 * <p>提供：
 * <ul>
 *   <li>{@link AuthenticationProvider} = {@link NoOpAuthenticationProvider}（兜底匿名）</li>
 *   <li>{@link PermissionEvaluator} = {@link DefaultPermissionEvaluator}（读 attributes["permissions"]）</li>
 *   <li>{@link AuthorizationAspect}（拦截 3 个注解）</li>
 *   <li>{@link GlobalExceptionHandler}（AccessDeniedException → HTTP 401）</li>
 * </ul>
 *
 * <p>{@code @EnableAspectJAutoProxy} 启用 Spring AOP，让 Aspect 生效。
 *
 * <p>注：{@link AuthorizationAspect} 不加 {@code @ConditionalOnMissingBean} —— 它是基础设施 bean，
 * ISV 想换实现请用 {@code @Primary} 或排除本 AutoConfiguration。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class AuthAutoConfiguration {

    /**
     * 装配默认 AuthenticationProvider（NoOp）。已存在则让位。
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationProvider noOpAuthenticationProvider() {
        return new NoOpAuthenticationProvider();
    }

    /**
     * 装配默认 PermissionEvaluator。已存在则让位。
     */
    @Bean
    @ConditionalOnMissingBean
    public PermissionEvaluator permissionEvaluator() {
        return new DefaultPermissionEvaluator();
    }

    /**
     * 装配 AuthorizationAspect。不加 ConditionalOnMissingBean（基础设施 bean）。
     */
    @Bean
    public AuthorizationAspect authorizationAspect(PermissionEvaluator evaluator) {
        return new AuthorizationAspect(evaluator);
    }

    /**
     * 装配全局异常处理器。仅在 Web 应用中激活。
     *
     * <p>ISV 覆盖方式（任选其一）：
     * <ul>
     *   <li>声明 {@code @RestControllerAdvice @Order(Ordered.HIGHEST_PRECEDENCE)} 更高优先级拦截</li>
     *   <li>排除本 AutoConfiguration：{@code @SpringBootApplication(exclude = AuthAutoConfiguration.class)}</li>
     *   <li>声明同类型 bean 替代：{@code @Bean GlobalExceptionHandler globalExceptionHandler() {...}}</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    @ConditionalOnWebApplication
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
