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

package com.dji.fh.feature.user.dto;

/**
 * 认证响应 DTO。
 *
 * <p>注册场景下，普通用户 token 为 null（待审核），message 携带提示信息。
 *
 * @param token   JWT token（审核通过后才签发，pending 状态为 null）
 * @param user    用户信息
 * @param message 提示信息（如"注册成功，请等待管理员审核"）
 * @author DJI DevKit Team
 */
public record AuthResponse(
        String token,
        UserInfo user,
        String message
) {

    /**
     * 便捷构造：无提示信息（登录成功场景）。
     *
     * @param token JWT token
     * @param user  用户信息
     */
    public AuthResponse(String token, UserInfo user) {
        this(token, user, null);
    }
}
