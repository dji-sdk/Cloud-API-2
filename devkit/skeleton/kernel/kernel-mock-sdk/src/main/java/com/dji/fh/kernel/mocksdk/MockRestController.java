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

package com.dji.fh.kernel.mocksdk;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * Mock SDK 手动触发 REST 端点。
 *
 * <p>所有端点 {@code POST /mock/devices/{sn}/<action>}，body 视 action 而定。
 *
 * <p>语义：fire-and-forget。事件触发被异步派发到 SDK callback；HTTP 响应只表示"已接受"，
 * 故全部返回 {@code 202 Accepted}（失败为 {@code 404 Not Found}）。
 *
 * <p><b>安全</b>：本 controller 仅在 {@code MockSdkAutoConfiguration} 装配时存在，
 * 即仅在 {@code fh.skeleton.sdk.mode=mock} 时启用。生产 profile 0 泄漏面。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/mock/devices")
public class MockRestController {

    private final MockEventTrigger trigger;

    /**
     * @param trigger 共享触发 API
     */
    public MockRestController(MockEventTrigger trigger) {
        this.trigger = Objects.requireNonNull(trigger, "trigger 不能为空");
    }

    /**
     * 触发设备上线。
     *
     * @param sn 设备 SN（必须已 register）
     * @return 202 {"status":"fired"} 或 404 {"error":"..."}
     */
    @PostMapping("/{sn}/online")
    public ResponseEntity<Map<String, String>> online(@PathVariable("sn") String sn) {
        try {
            trigger.fireDeviceOnline(sn);
            return ResponseEntity.accepted().body(Map.of("status", "fired"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 触发设备下线。
     *
     * @param sn     设备 SN
     * @param reason 下线原因（可选 query param）
     * @return 202 / 404
     */
    @PostMapping("/{sn}/offline")
    public ResponseEntity<Map<String, String>> offline(@PathVariable("sn") String sn,
                                                       @RequestParam(name = "reason", required = false) String reason) {
        try {
            trigger.fireDeviceOffline(sn, reason);
            return ResponseEntity.accepted().body(Map.of("status", "fired"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 手动触发一次 OSD 推送。
     *
     * @param sn   设备 SN
     * @param data OSD payload
     * @return 202
     */
    @PostMapping("/{sn}/osd")
    public ResponseEntity<Map<String, String>> osd(@PathVariable("sn") String sn,
                                                   @RequestBody Map<String, Object> data) {
        trigger.fireOsd(sn, data);
        return ResponseEntity.accepted().body(Map.of("status", "fired"));
    }

    /**
     * 触发飞行任务进度事件。
     *
     * @param sn      设备 SN
     * @param payload 含 taskId / status / progress
     * @return 202
     */
    @PostMapping("/{sn}/flight-task")
    public ResponseEntity<Map<String, String>> flightTask(@PathVariable("sn") String sn,
                                                          @RequestBody Map<String, Object> payload) {
        String taskId = (String) payload.getOrDefault("taskId", "manual-task");
        String status = (String) payload.getOrDefault("status", "RUNNING");
        int progress = ((Number) payload.getOrDefault("progress", 0)).intValue();
        trigger.fireFlightTaskProgress(sn, taskId, status, progress);
        return ResponseEntity.accepted().body(Map.of("status", "fired"));
    }

    /**
     * 触发 HMS 告警。
     *
     * @param sn      设备 SN
     * @param payload 含 alertCode / level / message
     * @return 202
     */
    @PostMapping("/{sn}/hms")
    public ResponseEntity<Map<String, String>> hms(@PathVariable("sn") String sn,
                                                   @RequestBody Map<String, Object> payload) {
        String code = (String) payload.getOrDefault("alertCode", "MANUAL-HMS");
        String level = (String) payload.getOrDefault("level", "WARNING");
        String message = (String) payload.getOrDefault("message", "");
        trigger.fireHmsAlert(sn, code, level, message);
        return ResponseEntity.accepted().body(Map.of("status", "fired"));
    }
}
