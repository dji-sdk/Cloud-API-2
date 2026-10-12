// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.config.DemoProperties;
import com.dji.fhsdk.demo.model.DeviceContext;
import com.dji.fhsdk.demo.model.PayloadListData;
import com.dji.fhsdk.demo.model.PayloadVo;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PayloadService {
    private static final Logger log = LoggerFactory.getLogger(PayloadService.class);
    private final TelemetryPublisher publisher;
    private final DemoProperties demoProperties;

    public PayloadService(TelemetryPublisher publisher, DemoProperties demoProperties) {
        this.publisher = publisher;
        this.demoProperties = demoProperties;
    }

    public void startWatch(DeviceContext ctx) {
        stopWatch(ctx);
        Drone drone = ctx.getDrone();
        if (drone == null) return;
        LiveDeviceModule liveDeviceModule = drone.getLiveDeviceModule();
        if (liveDeviceModule == null) throw new IllegalStateException("live device module not available");

        ctx.setAvailablePayloads(new CopyOnWriteArrayList<>());
        ctx.setPayloadConfigured(false);
        ctx.setSelectedLiveInfo(null);
        ctx.setPayloadIndex(null);

        int listenId = liveDeviceModule.listenLiveAvailableRange(ranges -> {
            List<LiveInfo> filtered = filterDronePayloads(ranges, ctx.getDroneSn());
            ctx.setAvailablePayloads(new CopyOnWriteArrayList<>(filtered));
            for (LiveInfo info : filtered) {
                log.info("payload available gateway={} payload={} deviceSn={} videoId={}",
                        ctx.getGatewaySn(), info.getPayloadIndex(), info.getDeviceSn(), info.getVideoId());
            }
            publishState(ctx);
        });
        ctx.setPayloadRangeListenId(listenId);
        log.info("payload watch started gateway={} drone={}", ctx.getGatewaySn(), ctx.getDroneSn());
    }

    public void stopWatch(DeviceContext ctx) {
        if (ctx == null || ctx.getDrone() == null) return;
        if (ctx.getPayloadRangeListenId() >= 0) {
            try {
                LiveDeviceModule liveDeviceModule = ctx.getDrone().getLiveDeviceModule();
                if (liveDeviceModule != null) liveDeviceModule.cancelListenLiveAvailableRange(ctx.getPayloadRangeListenId());
            } catch (Exception ignored) {}
            ctx.setPayloadRangeListenId(-1);
        }
    }

    public PayloadListData listPayloads(DeviceContext ctx) {
        PayloadListData data = new PayloadListData();
        data.setPayloads(toVoList(ctx.getAvailablePayloads()));
        data.setSelectedPayloadIndex(ctx.getPayloadIndex());
        data.setConfigured(ctx.isPayloadConfigured());
        return data;
    }

    public void confirmPayload(DeviceContext ctx, String payloadIndex) {
        if (payloadIndex == null || payloadIndex.isBlank()) throw new IllegalArgumentException("payloadIndex is required");
        if (ctx.isDrcConnected()) throw new IllegalStateException("请先断开 DRC 后再更换 Payload");
        if (ctx.getLiveModule() != null) throw new IllegalStateException("请先停止直播后再更换 Payload");

        String target = payloadIndex.trim();
        LiveInfo found = ctx.getAvailablePayloads().stream()
                .filter(info -> target.equals(info.getPayloadIndex()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("payload 不在可用列表中: " + target));

        ctx.setSelectedLiveInfo(found);
        ctx.setPayloadIndex(found.getPayloadIndex());
        ctx.setPayloadConfigured(true);
        log.info("payload confirmed gateway={} payload={} videoId={}",
                ctx.getGatewaySn(), found.getPayloadIndex(), found.getVideoId());
        publishState(ctx);
    }

    public void requireConfigured(DeviceContext ctx) {
        if (!ctx.isPayloadConfigured() || ctx.getSelectedLiveInfo() == null) {
            throw new IllegalStateException("Payload 未配置，请先选择并确认 Payload");
        }
    }

    private void publishState(DeviceContext ctx) {
        publisher.publishPayloadRange(ctx.getHttpSessionId(), ctx.getGatewaySn(),
                toVoList(ctx.getAvailablePayloads()), ctx.getPayloadIndex(),
                ctx.isPayloadConfigured(), defaultPayloadHint());
    }

    private String defaultPayloadHint() {
        String configured = demoProperties.getDronePayloadIndex();
        return configured != null && !configured.isBlank() ? configured.trim() : null;
    }

    private static List<LiveInfo> filterDronePayloads(List<LiveInfo> ranges, String droneSn) {
        if (ranges == null || ranges.isEmpty()) return List.of();
        List<LiveInfo> matched = new ArrayList<>();
        for (LiveInfo info : ranges) {
            if (droneSn != null && droneSn.equals(info.getDeviceSn())) matched.add(info);
        }
        return matched.isEmpty() ? List.copyOf(ranges) : matched;
    }

    static List<PayloadVo> toVoList(List<LiveInfo> infos) {
        List<PayloadVo> vos = new ArrayList<>();
        if (infos == null) return vos;
        for (LiveInfo info : infos) {
            vos.add(new PayloadVo(info.getPayloadIndex(), info.getDeviceSn(), info.getVideoId()));
        }
        return vos;
    }
}
