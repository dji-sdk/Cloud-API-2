// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.controller;

import com.dji.fhsdk.demo.model.*;
import com.dji.fhsdk.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

/**
 * Demo REST API（/api/v1）。
 * <p>
 * 典型流程：session/init → devices/select → devices/payload → live/start → control/drc-link。
 * 遥测经 WebSocket {@code /api/v1/ws/telemetry} 下行，与 HttpSession 绑定。
 */
@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final SessionService sessionService;
    private final DeviceService deviceService;
    private final DeviceSessionService deviceSessionService;
    private final ControlService controlService;
    private final LiveService liveService;
    private final PayloadService payloadService;
    private final SessionRegistry registry;

    public ApiController(SessionService sessionService,
                         DeviceService deviceService,
                         DeviceSessionService deviceSessionService,
                         ControlService controlService,
                         LiveService liveService,
                         PayloadService payloadService,
                         SessionRegistry registry) {
        this.sessionService = sessionService;
        this.deviceService = deviceService;
        this.deviceSessionService = deviceSessionService;
        this.controlService = controlService;
        this.liveService = liveService;
        this.payloadService = payloadService;
        this.registry = registry;
    }

    @PostMapping("/session/init")
    public ApiResponse<Void> initSession(HttpSession session) {
        sessionService.initSession(session.getId());
        return ApiResponse.ok();
    }

    @GetMapping("/devices")
    public ApiResponse<DeviceVo.ListData> listDevices(HttpSession session) {
        requireReady(session);
        DeviceVo.ListData data = new DeviceVo.ListData();
        data.setDevices(deviceService.listDevices());
        return ApiResponse.ok(data);
    }

    @PostMapping("/devices/select")
    public ApiResponse<Void> selectDevice(@RequestBody SelectDeviceRequest req, HttpSession session) {
        requireReady(session);
        deviceSessionService.select(session.getId(), req.getGatewaySn());
        return ApiResponse.ok();
    }

    @DeleteMapping("/devices/select")
    public ApiResponse<Void> deselectDevice(HttpSession session) {
        deviceSessionService.deselect(session.getId());
        return ApiResponse.ok();
    }

    @GetMapping("/devices/payloads")
    public ApiResponse<PayloadListData> listPayloads(HttpSession session) {
        DeviceContext ctx = requireDevice(session);
        return ApiResponse.ok(payloadService.listPayloads(ctx));
    }

    @PostMapping("/devices/payload")
    public ApiResponse<PayloadListData> confirmPayload(@RequestBody SelectPayloadRequest req, HttpSession session) {
        DeviceContext ctx = requireDevice(session);
        payloadService.confirmPayload(ctx, req.getPayloadIndex());
        return ApiResponse.ok(payloadService.listPayloads(ctx));
    }

    @PostMapping("/control/drc-link")
    public ApiResponse<Void> connectDrc(HttpSession session) {
        controlService.connectDrc(session.getId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/control/drc-link")
    public ApiResponse<Void> disconnectDrc(HttpSession session) {
        controlService.disconnectDrc(session.getId());
        return ApiResponse.ok();
    }

    @PostMapping("/control/stick")
    public ApiResponse<Void> stick(@RequestBody StickControlRequest req, HttpSession session) {
        controlService.sendStick(session.getId(), req);
        return ApiResponse.ok();
    }

    @PostMapping("/control/gimbal-reset")
    public ApiResponse<Void> gimbalReset(@RequestBody GimbalResetRequest req, HttpSession session) {
        controlService.resetGimbal(session.getId(), req);
        return ApiResponse.ok();
    }

    @GetMapping("/live/stream-url")
    public ApiResponse<StreamUrlResponse> streamUrl(HttpSession session) {
        DeviceContext ctx = requireDevice(session);
        return ApiResponse.ok(liveService.getStreamUrl(ctx));
    }

    @PostMapping("/live/start")
    public ApiResponse<StreamUrlResponse> startLive(HttpSession session) {
        requireReady(session);
        DeviceContext ctx = requireDevice(session);
        liveService.startLive(ctx);
        return ApiResponse.ok(liveService.getStreamUrl(ctx));
    }

    @DeleteMapping("/live/stop")
    public ApiResponse<Void> stopLive(HttpSession session) {
        DeviceContext ctx = registry.getDeviceContext(session.getId());
        if (ctx != null) {
            liveService.stopLive(ctx);
        }
        return ApiResponse.ok();
    }

    private DeviceContext requireDevice(HttpSession session) {
        DeviceContext ctx = registry.getDeviceContext(session.getId());
        if (ctx == null) {
            throw new IllegalStateException("no device selected");
        }
        return ctx;
    }

    private void requireReady(HttpSession session) {
        if (!sessionService.isReady(session.getId())) {
            throw new IllegalStateException("session not initialized, call POST /session/init first");
        }
    }
}
