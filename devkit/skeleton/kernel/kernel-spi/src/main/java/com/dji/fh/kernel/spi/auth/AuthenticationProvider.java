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
import java.util.Optional;

/**
 * 校验请求 → 解析出 SecurityContext。ISV 实现这个 SPI 接入自家身份系统。
 *
 * <p>Kernel 默认提供 NoOp 实现；
 * 官方 starter 提供 JWT / OAuth2 实现；
 * ISV 想接公司 SSO / LDAP / 自家 token 系统 → 实现此接口 + 注册为 Spring Bean 即可。
 *
 * <p>多个 Provider 可同时存在，按 {@link #order()} 链式尝试，第一个成功的赢。
 *
 * <p><b>v0.1 已知约束</b>：本接口绑定 Servlet {@link HttpServletRequest}，
 * 仅支持 HTTP 请求场景。WebSocket upgrade / gRPC metadata / 定时任务 / 异步 event handler
 * 等非 HTTP 身份来源在 v0.1 不被本接口表达，需走 ISV 自行 {@link SecurityContextHolder#set}
 * 注入。v0.2 计划抽出 request-agnostic 的 {@code AuthenticationContext} 载体；届时本签名为
 * breaking change，ISV 实现需要适配。
 *
 * @author DJI DevKit Team
 */
public interface AuthenticationProvider {

    /**
     * 尝试从请求中解析出 SecurityContext。
     *
     * <p><b>语义约定</b>：
     * <ul>
     *   <li>解析成功 → 返回 {@code Optional.of(context)}</li>
     *   <li>本 Provider 不处理此请求 → 返回 {@code Optional.empty()}（继续下一个 Provider）</li>
     *   <li>本 Provider 应处理但 token 无效 → 抛 {@link AuthenticationException}</li>
     * </ul>
     *
     * @param request HTTP 请求
     * @return 解析结果
     * @throws AuthenticationException 校验失败
     */
    Optional<SecurityContext> authenticate(HttpServletRequest request) throws AuthenticationException;

    /**
     * Provider 优先级。数值小先尝试。
     *
     * <p>NoOp = {@link Integer#MAX_VALUE}（永远兜底）；
     * 自定义 Provider 推荐 100~1000；
     * 官方 starter Provider 推荐 500。
     *
     * @return order 值
     */
    default int order() {
        return 500;
    }
}
