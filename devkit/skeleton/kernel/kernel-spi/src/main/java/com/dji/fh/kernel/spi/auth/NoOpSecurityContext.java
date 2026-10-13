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

import java.util.Map;
import java.util.Set;

/**
 * NoOp 默认 SecurityContext —— 表示「未认证 / 匿名」。
 *
 * <p>使用单例避免重复分配；Kernel 在未配置任何 AuthenticationProvider 时
 * 一律返回本实例。
 *
 * @author DJI DevKit Team
 */
public final class NoOpSecurityContext implements SecurityContext {

    /**
     * 全局共享单例。
     */
    public static final NoOpSecurityContext INSTANCE = new NoOpSecurityContext();

    private NoOpSecurityContext() {
    }

    @Override
    public String principalId() {
        return SecurityContext.ANONYMOUS_PRINCIPAL;
    }

    @Override
    public Set<String> roles() {
        return Set.of();
    }

    @Override
    public Map<String, Object> attributes() {
        return Map.of();
    }
}
