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

import com.dji.fh.kernel.spi.auth.PermissionEvaluator;
import com.dji.fh.kernel.spi.auth.SecurityContext;

import java.util.Collection;

/**
 * PermissionEvaluator 默认实现 —— 从 {@link SecurityContext#attributes()} 的
 * {@code "permissions"} key 读 {@link Collection}，{@code contains} 即视为有权限。
 *
 * <p>ISV 想接 Casbin / OPA / 公司权限引擎 → 实现 {@link PermissionEvaluator} 覆盖此 bean。
 *
 * <p>容错：若 {@code attributes["permissions"]} 不是 Collection 类型，返回 {@code false}
 * 而不抛异常（防止配置/数据错误导致的认证大面积失败）。
 *
 * @author DJI DevKit Team
 */
public class DefaultPermissionEvaluator implements PermissionEvaluator {

    /**
     * attributes 中权限列表的固定 key。
     */
    public static final String PERMISSIONS_ATTRIBUTE = "permissions";

    @Override
    public boolean hasPermission(SecurityContext context, String permission) {
        if (context == null || permission == null) {
            return false;
        }
        Object raw = context.attributes().get(PERMISSIONS_ATTRIBUTE);
        if (!(raw instanceof Collection<?> permissions)) {
            return false;
        }
        return permissions.contains(permission);
    }
}
