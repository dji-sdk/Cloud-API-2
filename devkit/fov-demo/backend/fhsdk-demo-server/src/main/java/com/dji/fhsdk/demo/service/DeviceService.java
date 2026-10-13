// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.fhsdk.FhsdkBootstrap;
import com.dji.fhsdk.demo.model.DeviceVo;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.DeviceManageModule;
import com.dji.flighthubsdk.core.Workspace;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DeviceService {
    private final FhsdkBootstrap bootstrap;

    public DeviceService(FhsdkBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public List<DeviceVo> listDevices() {
        bootstrap.ensureInitialized();
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        DeviceManageModule deviceManageModule = workspace.getDeviceManageModule();
        if (deviceManageModule == null) throw new IllegalStateException("device manage module not available");

        AtomicReference<List<DeviceTopo>> result = new AtomicReference<>(List.of());
        AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        deviceManageModule.getDeviceList((devices, error) -> {
            if (error != null && error.isSuccess()) result.set(devices);
            else errorRef.set(error);
            latch.countDown();
        });

        try {
            if (!latch.await(30, TimeUnit.SECONDS)) throw new IllegalStateException("getDeviceList timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("getDeviceList interrupted", e);
        }

        if (errorRef.get() != null) throw new IllegalStateException("getDeviceList failed: " + errorRef.get());

        List<DeviceVo> vos = new ArrayList<>();
        for (DeviceTopo topo : result.get()) {
            Device main = topo.getDevice();
            if (main == null) continue;
            DeviceVo vo = new DeviceVo();
            vo.setGatewaySn(main.getDeviceSn());
            vo.setGatewayName(firstNonBlank(main.getNickname(), main.getDeviceName(), main.getDeviceSn()));
            vo.setOnline(main.isOnline());
            List<Device> subs = topo.getSubDevices();
            if (subs != null && !subs.isEmpty()) {
                Device sub = subs.stream().filter(s -> "drone".equalsIgnoreCase(s.getDeviceClass())).findFirst().orElse(subs.get(0));
                vo.setDroneSn(sub.getDeviceSn());
                vo.setDroneName(firstNonBlank(sub.getNickname(), sub.getDeviceName(), sub.getDeviceSn()));
                vo.setDroneOnline(sub.isOnline());
            }
            vos.add(vo);
        }
        return vos;
    }

    public Optional<DeviceTopo> findTopoByGatewaySn(String gatewaySn) {
        return findTopoRaw(gatewaySn);
    }

    private Optional<DeviceTopo> findTopoRaw(String gatewaySn) {
        bootstrap.ensureInitialized();
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        DeviceManageModule deviceManageModule = workspace.getDeviceManageModule();
        if (deviceManageModule == null) return Optional.empty();

        AtomicReference<DeviceTopo> found = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        deviceManageModule.getDeviceList((devices, error) -> {
            if (error != null && error.isSuccess()) {
                for (DeviceTopo topo : devices) {
                    if (matchesTopo(topo, gatewaySn)) { found.set(topo); break; }
                }
            }
            latch.countDown();
        });
        try { latch.await(30, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return Optional.ofNullable(found.get());
    }

    private boolean matchesTopo(DeviceTopo topo, String gatewaySn) {
        if (topo == null || gatewaySn == null || gatewaySn.isBlank()) return false;
        Device root = topo.getDevice();
        if (root != null && gatewaySn.equals(root.getDeviceSn())) return true;
        List<Device> subs = topo.getSubDevices();
        if (subs == null) return false;
        for (Device sub : subs) if (sub != null && gatewaySn.equals(sub.getDeviceSn())) return true;
        return false;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }
}
