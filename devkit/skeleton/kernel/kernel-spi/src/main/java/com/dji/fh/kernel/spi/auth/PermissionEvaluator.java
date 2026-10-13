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

/**
 * 细粒度权限判断器（可选 SPI）。
 *
 * <p>{@code @RequirePermission} 注解触发时调用本接口。
 * Kernel 默认提供基于 {@code SecurityContext.attributes()["permissions"]} 的实现；
 * ISV 想接自家权限引擎（如 Casbin / OPA）→ 实现此接口覆盖默认 Bean。
 *
 * <p>与 RBAC（{@code @RequireRole}）解耦 —— RBAC 走 {@code roles}，
 * 细粒度权限走 attributes / 自家引擎。
 *
 * @author DJI DevKit Team
 */
public interface PermissionEvaluator {

    /**
     * @param context    当前 SecurityContext
     * @param permission 待校验的权限标识
     * @return 拥有该权限返回 true
     */
    boolean hasPermission(SecurityContext context, String permission);
}
