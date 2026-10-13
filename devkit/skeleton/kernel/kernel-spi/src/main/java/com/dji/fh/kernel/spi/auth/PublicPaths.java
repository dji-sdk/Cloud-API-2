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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 公开路径白名单 — 无需登录即可匿名访问的端点。
 *
 * <p>唯一事实来源（single source of truth）：
 * 登录 / 注册入口对所有未配置或已配置鉴权的模式（NoOp / Authenticating）都应可达，
 * 因此常量下沉到 kernel-spi，feature 模块与 app 模块各自引用，避免出现两套
 * 不一致的路径清单（安全发布专项）。
 *
 * <p>匹配采用<b>精确 equals</b>，且先剥离 {@code context-path}，杜绝
 * {@code endsWith} 宽松匹配导致的越权风险：例如 {@code /api/auth/loginnn}
 * 这类「伪装路径」不得命中 {@code /api/auth/login}。
 *
 * @author DJI DevKit Team
 */
public final class PublicPaths {

    /**
     * 公开路径常量。
     */
    public static final List<String> PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    private PublicPaths() {
        // 工具类：禁止实例化
    }
    /**
     * 判断请求 URI（剥离 context-path 后）是否精确命中公开路径白名单。
     *
     * @param request HTTP 请求；context-path 可为 null（部分 Mock 场景）
     * @return 命中任一公开路径返回 true；否则 false
     */
    public static boolean isPublicPath(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return PATHS.contains(uri);
    }

    /**
     * 判断路径是否命中公开路径白名单（无 HttpServletRequest 时使用）。
     *
     * @param path 已剥离 context-path 的请求路径
     * @return 精确命中返回 true
     */
    public static boolean matches(String path) {
        return path != null && PATHS.contains(path);
    }

    /**
     * 供日志 / 测试展示白名单内容。
     *
     * @return 公开路径数组
     */
    public static String[] paths() {
        return PATHS.toArray(new String[0]);
    }
}
