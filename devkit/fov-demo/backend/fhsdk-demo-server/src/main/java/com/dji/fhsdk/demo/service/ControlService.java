// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.fhsdk.TelemetryMapper;
import com.dji.fhsdk.demo.model.DeviceContext;
import com.dji.fhsdk.demo.model.GimbalResetRequest;
import com.dji.fhsdk.demo.model.StickControlRequest;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ControlService {
    private static final Logger log = LoggerFactory.getLogger(ControlService.class);
    private final SessionRegistry registry;
    private final TelemetryPublisher publisher;
    private final PayloadService payloadService;

    public ControlService(SessionRegistry registry, TelemetryPublisher publisher, PayloadService payloadService) {
        this.registry = registry;
        this.publisher = publisher;
        this.payloadService = payloadService;
    }

    public void connectDrc(String httpSessionId) {
        DeviceContext ctx = requireCtx(httpSessionId);
        if (ctx.isDrcConnected()) return;
        payloadService.requireConfigured(ctx);

        Drone drone = ctx.getDrone();
        DrcModule drcModule = drone.getDrcModule();
        if (drcModule == null) throw new IllegalStateException("DRC module not available");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
        drcModule.connectDrcLink(error -> { errorRef.set(error); latch.countDown(); });
        await(latch, "connectDrcLink");
        if (errorRef.get() == null || !errorRef.get().isSuccess()) {
            throw new IllegalStateException("connectDrcLink failed: " + errorRef.get());
        }
        ctx.setDrcConnected(true);
        acquirePayloadAuthority(ctx);
        subscribeDrcChannels(ctx);
        log.info("DRC connected gateway={}", ctx.getGatewaySn());
    }

    private void acquirePayloadAuthority(DeviceContext ctx) {
        String payloadIndex = resolvePayloadIndex(ctx, null);
        ControlAuthModule controlAuthModule = ctx.getDrone().getControlAuthModule();
        if (controlAuthModule == null) throw new IllegalStateException("control auth module not available");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
        controlAuthModule.acquirePayloadControlAuthority(payloadIndex, error -> { errorRef.set(error); latch.countDown(); });
        await(latch, "acquirePayloadControlAuthority");
        if (errorRef.get() == null || !errorRef.get().isSuccess()) {
            log.warn("acquirePayloadControlAuthority failed payload={} err={}", payloadIndex, errorRef.get());
        } else {
            log.info("payload control acquired payload={}", payloadIndex);
        }
    }

    public void disconnectDrc(String httpSessionId) {
        DeviceContext ctx = registry.getDeviceContext(httpSessionId);
        if (ctx == null || !ctx.isDrcConnected()) return;
        unsubscribeDrcChannels(ctx);

        DrcModule drcModule = ctx.getDrone().getDrcModule();
        if (drcModule == null) throw new IllegalStateException("DRC module not available");

        CountDownLatch latch = new CountDownLatch(1);
        drcModule.disconnectDrcLink(error -> latch.countDown());
        await(latch, "disconnectDrcLink");
        ctx.setDrcConnected(false);
    }

    public void sendStick(String httpSessionId, StickControlRequest req) {
        DeviceContext ctx = requireCtx(httpSessionId);
        if (!ctx.isDrcConnected()) throw new IllegalStateException("DRC not connected");

        StickControlData data = new StickControlData();
        data.setRoll(req.getRoll());
        data.setPitch(req.getPitch());
        data.setThrottle(req.getThrottle());
        data.setYaw(req.getYaw());
        data.setGimbalPitch(req.getGimbalPitch());

        FlightControlModule flightControlModule = ctx.getDrone().getFlightControlModule();
        if (flightControlModule == null) throw new IllegalStateException("flight control module not available");
        flightControlModule.drcSendStickControl(data);
    }

    public void resetGimbal(String httpSessionId, GimbalResetRequest req) {
        DeviceContext ctx = requireCtx(httpSessionId);
        if (!ctx.isDrcConnected()) throw new IllegalStateException("DRC not connected");

        int mode = req.getResetMode();
        if (mode < 0 || mode > 3) throw new IllegalArgumentException("resetMode must be 0-3");

        String payloadIndex = resolvePayloadIndex(ctx, req.getPayloadIndex());
        CameraControlModule cameraControlModule = ctx.getDrone().getCameraControlModule();
        if (cameraControlModule == null) throw new IllegalStateException("camera control module not available");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
        cameraControlModule.drcResetGimbal(payloadIndex, mode, error -> { errorRef.set(error); latch.countDown(); });
        await(latch, "resetGimbal");
        if (errorRef.get() == null || !errorRef.get().isSuccess()) {
            throw new IllegalStateException("resetGimbal failed: " + errorRef.get());
        }
        log.info("resetGimbal ok payload={} mode={}", payloadIndex, mode);
    }

    private String resolvePayloadIndex(DeviceContext ctx, String requestPayload) {
        if (requestPayload != null && !requestPayload.isBlank()) return requestPayload.trim();
        payloadService.requireConfigured(ctx);
        return ctx.getPayloadIndex().trim();
    }

    private void subscribeDrcChannels(DeviceContext ctx) {
        String sid = ctx.getHttpSessionId();
        String gw = ctx.getGatewaySn();
        String payload = resolvePayloadIndex(ctx, null);

        CameraControlModule cameraControlModule = ctx.getDrone().getCameraControlModule();
        if (cameraControlModule == null) throw new IllegalStateException("camera control module not available");

        int fastId = cameraControlModule.listenDrcCameraMeasureTarget(payload,
                target -> publisher.publishDrcCamera(sid, gw, TelemetryMapper.measureTarget(target)));
        ctx.putListenId("drc_measure", fastId);
    }

    private void unsubscribeDrcChannels(DeviceContext ctx) {
        Integer fastId = ctx.getListenIds().remove("drc_measure");
        if (fastId != null) {
            try {
                CameraControlModule cameraControlModule = ctx.getDrone().getCameraControlModule();
                if (cameraControlModule != null) {
                    cameraControlModule.cancelListenDrcCameraMeasureTarget(resolvePayloadIndex(ctx, null), fastId);
                }
            } catch (Exception ignored) {}
        }
    }

    private DeviceContext requireCtx(String httpSessionId) {
        DeviceContext ctx = registry.getDeviceContext(httpSessionId);
        if (ctx == null || ctx.getDrone() == null) throw new IllegalStateException("no device selected");
        return ctx;
    }

    private void await(CountDownLatch latch, String op) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) throw new IllegalStateException(op + " timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(op + " interrupted", e);
        }
    }
}
