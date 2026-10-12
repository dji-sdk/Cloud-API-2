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

package com.dji.fh.kernel.spi.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记当前方法为公开端点（匿名可访问），覆盖类级 {@link RequireAuthenticated}。
 *
 * <p>使用场景：类级标注 {@code @RequireAuthenticated} 实现"默认登录"后，登录/注册等
 * 入口端点需显式声明公开，避免被 AOP 切面误拦截返回 403。
 *
 * <p>{@link AuthorizationAspect} 在解析到本注解时跳过 {@link RequireAuthenticated} 校验；
 * 但 {@link RequireRole} / {@link RequirePermission} 不受影响（公开端点一般也不叠加角色校验）。
 *
 * @author DJI DevKit Team
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {
}
