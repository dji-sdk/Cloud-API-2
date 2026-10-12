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
 * 表示「当前请求是谁」。
 * 由 AuthenticationProvider 解析请求后填充，feature / ISV 通过
 * {@link SecurityContextHolder#current()} 读取。
 *
 * <p>字段最小化原则 —— ISV 想加部门 / 工号等扩展字段，请通过 {@link #attributes()}
 * 提供（key/value 形态，不进 Kernel SemVer 契约）。
 *
 * <p>注意：本接口<b>不含</b> tenantId —— Skeleton 明确不做多租户（见 spec §12）。
 *
 * @author DJI DevKit Team
 */
public interface SecurityContext {

    /**
     * 匿名 / 未认证用户的 principalId 常量。
     * NoOp 实现返回此值；isAuthenticated() 据此判断。
     */
    String ANONYMOUS_PRINCIPAL = "anonymous";

    /**
     * @return 用户唯一标识（user id / username / sub）；NoOp 模式返回 {@link #ANONYMOUS_PRINCIPAL}
     */
    String principalId();

    /**
     * @return 角色集（可空集）
     */
    Set<String> roles();

    /**
     * @return 扩展属性（建议只读；用于存 token 解析后的额外字段）
     */
    Map<String, Object> attributes();

    /**
     * 便捷判断方法：当前用户是否具有指定角色。
     *
     * <p>大小写不敏感匹配：角色配置来源多样
     * （JWT claims 为小写、ISV yml 可能写 "ADMIN"），一律忽略大小写比较，
     * 避免因大小写不一致导致误判越权或误拦截。
     *
     * @param role 待检查的角色
     * @return 存在任一忽略大小写相等的角色返回 true
     */
    default boolean hasRole(String role) {
        if (role == null) {
            return false;
        }
        for (String candidate : roles()) {
            if (role.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 便捷判断方法：当前用户是否已认证（非匿名）。
     *
     * @return principalId 不为 {@link #ANONYMOUS_PRINCIPAL} 返回 true
     */
    default boolean isAuthenticated() {
        return !ANONYMOUS_PRINCIPAL.equals(principalId());
    }
}
