// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.fhsdk.TelemetryMapper;
import com.dji.fhsdk.demo.model.DeviceContext;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceSessionService {
    private static final Logger log = LoggerFactory.getLogger(DeviceSessionService.class);
    private final SessionRegistry registry;
    private final DeviceService deviceService;
    private final LiveService liveService;
    private final ControlService controlService;
    private final TelemetryPublisher publisher;
    private final PayloadService payloadService;

    public DeviceSessionService(SessionRegistry registry, DeviceService deviceService, LiveService liveService,
                                ControlService controlService, TelemetryPublisher publisher, PayloadService payloadService) {
        this.registry = registry;
        this.deviceService = deviceService;
        this.liveService = liveService;
        this.controlService = controlService;
        this.publisher = publisher;
        this.payloadService = payloadService;
    }

    public synchronized void select(String httpSessionId, String gatewaySn) {
        if (!registry.isReady(httpSessionId)) throw new IllegalStateException("session not initialized");
        deselect(httpSessionId);

        Optional<DeviceTopo> topoOpt = deviceService.findTopoByGatewaySn(gatewaySn);
        if (topoOpt.isEmpty()) throw new IllegalArgumentException("device not found: " + gatewaySn);
        DeviceTopo topo = topoOpt.get();
        Device main = topo.getDevice();
        if (main == null) throw new IllegalStateException("topology root missing for: " + gatewaySn);

        Device droneDevice = pickDroneDevice(topo)
                .orElseThrow(() -> new IllegalStateException("no drone found in selected topology: " + gatewaySn));
        if (!droneDevice.isOnline()) throw new IllegalStateException("drone offline: " + droneDevice.getDeviceSn());

        String droneSn = droneDevice.getDeviceSn();
        Drone drone = FlightHubSDK.getDroneInstance(droneSn);
        if (drone == null) throw new IllegalStateException("drone instance not found: " + droneSn);

        // Airport location comes from Dock (dock/airport), not base_station gateway SN.
        Device dockDevice = findDockDevice(topo).orElse(null);
        Dock dock = null;
        if (dockDevice != null) {
            dock = FlightHubSDK.getDockInstance(dockDevice.getDeviceSn());
            if (dock == null) {
                log.warn("Dock found sn={} class={} but getDockInstance returned null",
                        dockDevice.getDeviceSn(), dockDevice.getDeviceClass());
            }
        } else {
            log.warn("No dock/airport in topology gateway={} rootClass={}", gatewaySn, main.getDeviceClass());
        }

        DeviceContext ctx = new DeviceContext();
        ctx.setHttpSessionId(httpSessionId);
        ctx.setGatewaySn(gatewaySn);
        ctx.setDroneSn(droneSn);
        ctx.setDrone(drone);
        ctx.setDock(dock);
        registry.setDeviceContext(httpSessionId, ctx);

        subscribeSlowOsd(ctx);
        subscribeDockOsd(ctx);
        payloadService.startWatch(ctx);
        log.info("Selected topology={} rootClass={} drone={} dockSn={}",
                gatewaySn, main.getDeviceClass(), droneSn,
                dockDevice != null ? dockDevice.getDeviceSn() : null);
    }

    public synchronized void deselect(String httpSessionId) {
        DeviceContext ctx = registry.getDeviceContext(httpSessionId);
        if (ctx == null) return;
        try { controlService.disconnectDrc(httpSessionId); } catch (Exception e) { log.warn("disconnect DRC on deselect: {}", e.getMessage()); }
        unsubscribeSlowOsd(ctx);
        unsubscribeDockOsd(ctx);
        liveService.stopLive(ctx);
        payloadService.stopWatch(ctx);
        ctx.clear();
        registry.clearDeviceContext(httpSessionId);
        log.info("Deselected device session={}", httpSessionId);
    }

    private void subscribeSlowOsd(DeviceContext ctx) {
        String sid = ctx.getHttpSessionId();
        String gw = ctx.getGatewaySn();
        DroneOsdModule osd = ctx.getDrone().getDroneOsdModule();
        if (osd == null) throw new IllegalStateException("drone OSD module not available: " + ctx.getDroneSn());

        ctx.putListenId("location", osd.listenAircraftLocation(loc -> publishMqtt(sid, gw, "location", TelemetryMapper.location(loc))));
        ctx.putListenId("attitude", osd.listenAircraftAttitude(att -> publishMqtt(sid, gw, "attitude", TelemetryMapper.attitude(att))));
        ctx.putListenId("speed", osd.listenAircraftSpeed(spd -> publishMqtt(sid, gw, "speed", TelemetryMapper.speed(spd))));
        ctx.putListenId("battery", osd.listenAircraftBatteryInfo(bat -> publishMqtt(sid, gw, "battery", TelemetryMapper.battery(bat))));
        ctx.putListenId("wind", osd.listenAircraftWindSpeed(wind -> publishMqtt(sid, gw, "wind", TelemetryMapper.wind(wind))));
        ctx.putListenId("gimbal", osd.listenAircraftPayloadsGimbalAttitude(gimbal -> publishMqtt(sid, gw, "gimbalAttitude", TelemetryMapper.gimbalAttitude(gimbal))));
        ctx.putListenId("measure", osd.listenAircraftPayloadsOsdInfo(list -> {
            if (list.getPayloadsInfoList() == null) return;
            for (var osdInfo : list.getPayloadsInfoList()) {
                publishMqtt(sid, gw, "measureTarget", TelemetryMapper.measureTargetFromOsd(osdInfo));
            }
        }));
    }

    private void subscribeDockOsd(DeviceContext ctx) {
        Dock dock = ctx.getDock();
        if (dock == null) {
            log.warn("subscribeDockOsd skipped: dock is null gateway={}", ctx.getGatewaySn());
            return;
        }
        DockOsdModule dockOsdModule = dock.getDockOsdModule();
        if (dockOsdModule == null) {
            log.warn("subscribeDockOsd skipped: DockOsdModule null gateway={}", ctx.getGatewaySn());
            return;
        }
        String sid = ctx.getHttpSessionId();
        String gw = ctx.getGatewaySn();
        int listenId = dockOsdModule.listenDockLocation(loc ->
                publishMqtt(sid, gw, "dockLocation", TelemetryMapper.dockLocation(loc)));
        ctx.putListenId("dockLocation", listenId);
    }

    private void unsubscribeDockOsd(DeviceContext ctx) {
        Dock dock = ctx.getDock();
        if (dock == null) return;
        DockOsdModule dockOsdModule = dock.getDockOsdModule();
        Integer id = ctx.getListenIds().remove("dockLocation");
        if (id != null && dockOsdModule != null) {
            try { dockOsdModule.cancelListenDockLocation(id); } catch (Exception ignored) {}
        }
    }

    private void unsubscribeSlowOsd(DeviceContext ctx) {
        DroneOsdModule osd = ctx.getDrone() == null ? null : ctx.getDrone().getDroneOsdModule();
        if (osd == null) return;
        cancel(ctx, "location", osd::cancelListenAircraftLocation);
        cancel(ctx, "attitude", osd::cancelListenAircraftAttitude);
        cancel(ctx, "speed", osd::cancelListenAircraftSpeed);
        cancel(ctx, "battery", osd::cancelListenAircraftBatteryInfo);
        cancel(ctx, "wind", osd::cancelListenAircraftWindSpeed);
        cancel(ctx, "gimbal", osd::cancelListenAircraftPayloadsGimbalAttitude);
        cancel(ctx, "measure", osd::cancelListenAircraftPayloadsOsdInfo);
    }

    private void publishMqtt(String sid, String gw, String field, Map<String, Object> data) {
        publisher.publishMqttOsd(sid, gw, field, data);
    }

    private Optional<Device> pickDroneDevice(DeviceTopo topo) {
        List<Device> candidates = new ArrayList<>();
        if (topo.getDevice() != null) candidates.add(topo.getDevice());
        if (topo.getSubDevices() != null) candidates.addAll(topo.getSubDevices());
        return candidates.stream().filter(this::isDrone).findFirst();
    }

    /** Prefer Dock/airport class for airport location OSD. */
    private Optional<Device> findDockDevice(DeviceTopo topo) {
        List<Device> candidates = new ArrayList<>();
        if (topo.getDevice() != null) candidates.add(topo.getDevice());
        if (topo.getSubDevices() != null) candidates.addAll(topo.getSubDevices());
        return candidates.stream().filter(this::isDock).findFirst();
    }

    private boolean isDrone(Device d) {
        if (d == null || d.getDeviceClass() == null) return false;
        return "drone".equalsIgnoreCase(d.getDeviceClass());
    }

    private boolean isDock(Device d) {
        if (d == null || d.getDeviceClass() == null) return false;
        String cls = d.getDeviceClass().toLowerCase(Locale.ROOT);
        return "dock".equals(cls) || "airport".equals(cls);
    }

    private interface CancelOp { void cancel(int listenId); }

    private void cancel(DeviceContext ctx, String key, CancelOp op) {
        Integer id = ctx.getListenIds().remove(key);
        if (id != null) {
            try { op.cancel(id); } catch (Exception ignored) {}
        }
    }
}
