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

package com.dji.fh.feature.chat.controller;

import com.dji.fh.feature.chat.ChatProperties;
import com.dji.fh.kernel.spi.auth.PublicEndpoint;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 助手可用性探测接口。
 *
 * <p>与 {@link ChatController} 的关键差别：{@code fh.chat.enabled=false} 时本接口依然可达。
 * 因此它由 {@code ChatStatusAutoConfiguration} 无条件注册，不参与
 * {@code ChatAutoConfiguration} 的开关门禁。前端据此决定是否渲染「AI 助手」入口——
 * 若探测本身也 404，前端就无法区分「功能未启用」与「服务异常」。
 *
 * <p>匿名可访问：只返回一个布尔值，不含任何业务数据；且探测可能发生在登录之前，
 * 若要求登录反而会让未登录用户被跳转到登录页。
 *
 * <p><b>类级 {@link RequireAuthenticated} + 方法级 {@link PublicEndpoint} 是刻意的配对。</b>
 * {@code AuthorizationAspect} 的切点只匹配 {@code @RequireAuthenticated} 等注解，类上不声明
 * {@code @RequireAuthenticated} 时切面根本不执行，方法上的 {@code @PublicEndpoint} 也就成了
 * 不会被读取的装饰。声明类级 {@code @RequireAuthenticated} 后，切面会命中并读到
 * {@code @PublicEndpoint}，从而为 {@code /status} 放行、为后续新增的其它方法保持鉴权。
 * 改动这两个注解中的任何一个都会让端点行为悄然反转。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequireAuthenticated
public class ChatStatusController {

    private final ChatProperties properties;

    /**
     * @param properties Chat 配置属性，用于读取 {@code fh.chat.enabled}
     */
    public ChatStatusController(ChatProperties properties) {
        this.properties = properties;
    }

    /**
     * 查询 AI 助手功能是否启用。
     *
     * @return 200 + {@code {"enabled": bool}}
     */
    @GetMapping("/status")
    @PublicEndpoint
    public ChatStatus getStatus() {
        return new ChatStatus(properties.isEnabled());
    }

    /**
     * 探测结果。
     *
     * @param enabled 是否启用 AI 助手
     */
    public record ChatStatus(boolean enabled) {
    }
}
