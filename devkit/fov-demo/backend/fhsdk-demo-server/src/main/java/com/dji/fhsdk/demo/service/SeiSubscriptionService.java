// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.model.DeviceContext;
import com.dji.flighthubsdk.core.LiveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 直播码流 SEI 订阅（AR + FOV）。与 DRC 无关，开流后即可订阅。
 */
@Service
public class SeiSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SeiSubscriptionService.class);

    private final TelemetryPublisher publisher;

    public SeiSubscriptionService(TelemetryPublisher publisher) {
        this.publisher = publisher;
    }

    /** 在 {@code LiveModule.start} 成功后调用；已订阅则跳过。 */
    public void subscribe(DeviceContext ctx) {
        LiveModule live = ctx.getLiveModule();
        if (live == null) {
            return;
        }
        if (ctx.getArSeiListenId() >= 0 && ctx.getFovSeiListenId() >= 0) {
            return;
        }
        String sid = ctx.getHttpSessionId();
        String gw = ctx.getGatewaySn();

        if (ctx.getArSeiListenId() < 0) {
            int arId = live.subscribeArSeiInfo(ar -> {
                Map<String, Object> arMap = Map.of(
                        "cameraYaw", ar.getCameraYaw(),
                        "cameraPitch", ar.getCameraPitch(),
                        "cameraRoll", ar.getCameraRoll(),
                        "cameraPosX", ar.getCameraPosX(),
                        "cameraPosY", ar.getCameraPosY(),
                        "cameraPosZ", ar.getCameraPosZ(),
                        "latitude", ar.getLatitude(),
                        "longitude", ar.getLongitude(),
                        "altitude", ar.getAltitude()
                );
                publisher.publishSeiAr(sid, gw, arMap);
            });
            ctx.setArSeiListenId(arId);
        }

        if (ctx.getFovSeiListenId() < 0) {
            int fovId = live.subscribeFovSeiInfo(fov -> {
                Map<String, Object> fovMap = Map.of(
                        "fovHorizontal", fov.getFovHorizontal(),
                        "fovVertical", fov.getFovVertical(),
                        "curActualFocalLength", fov.getCurActualFocalLength()
                );
                publisher.publishSeiFov(sid, gw, fovMap);
            });
            ctx.setFovSeiListenId(fovId);
        }
        log.info("SEI subscribed gateway={}", gw);
    }

    public void unsubscribe(DeviceContext ctx) {
        LiveModule live = ctx.getLiveModule();
        if (live == null) {
            ctx.setArSeiListenId(-1);
            ctx.setFovSeiListenId(-1);
            return;
        }
        if (ctx.getArSeiListenId() >= 0) {
            try {
                live.unSubscribeArSeiInfo(ctx.getArSeiListenId());
            } catch (Exception ignored) {
            }
            ctx.setArSeiListenId(-1);
        }
        if (ctx.getFovSeiListenId() >= 0) {
            try {
                live.unSubscribeFovSeiInfo(ctx.getFovSeiListenId());
            } catch (Exception ignored) {
            }
            ctx.setFovSeiListenId(-1);
        }
    }
}
