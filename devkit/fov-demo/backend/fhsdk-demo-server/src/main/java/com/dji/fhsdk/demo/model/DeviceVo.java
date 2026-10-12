// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

import java.util.ArrayList;
import java.util.List;

public class DeviceVo {

    private String gatewaySn;
    private String gatewayName;
    private boolean online;
    private String droneSn;
    private String droneName;
    private boolean droneOnline;

    public String getGatewaySn() { return gatewaySn; }
    public void setGatewaySn(String gatewaySn) { this.gatewaySn = gatewaySn; }
    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String droneSn) { this.droneSn = droneSn; }
    public String getDroneName() { return droneName; }
    public void setDroneName(String droneName) { this.droneName = droneName; }
    public boolean isDroneOnline() { return droneOnline; }
    public void setDroneOnline(boolean droneOnline) { this.droneOnline = droneOnline; }

    public static class ListData {
        private List<DeviceVo> devices = new ArrayList<>();

        public List<DeviceVo> getDevices() { return devices; }
        public void setDevices(List<DeviceVo> devices) { this.devices = devices; }
    }
}
