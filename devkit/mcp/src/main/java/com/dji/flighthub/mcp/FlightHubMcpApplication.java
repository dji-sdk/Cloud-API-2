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

package com.dji.flighthub.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FlightHub MCP Server — 通过 MCP 协议对 DJI FlightHub PaaS SDK 进行运行时控制。
 *
 * <p>支持两种传输模式：
 * <ul>
 *   <li>stdio — 本地 IDE 集成（Claude Code / Cursor）</li>
 *   <li>SSE — 远程 AI Agent 平台</li>
 * </ul>
 *
 * <p>传输模式由环境变量 FH_TRANSPORT 决定（默认：stdio）。
 */
@SpringBootApplication
public class FlightHubMcpApplication {

    private static final Logger log = LoggerFactory.getLogger(FlightHubMcpApplication.class);

    public static void main(String[] args) {
        // 确保所有日志输出到 stderr，保持 stdout 仅用于 MCP JSON-RPC 通信
        String transport = System.getenv().getOrDefault("FH_TRANSPORT", "stdio");

        SpringApplication app = new SpringApplication(FlightHubMcpApplication.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.setLogStartupInfo(false);

        if ("stdio".equals(transport)) {
            app.setAdditionalProfiles("stdio");
            System.setProperty("spring.main.web-application-type", "none");
        } else {
            app.setAdditionalProfiles("sse");
        }

        app.run(args);
        System.err.println("FlightHub MCP Server started [transport=" + transport + "]");
    }
}
