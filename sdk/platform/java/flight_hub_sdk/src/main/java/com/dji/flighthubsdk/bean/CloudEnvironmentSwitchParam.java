// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 云环境切换下发请求参数（POST /device/api/v1/service/cloud-environment-switch）。
 * 设备完成云环境切换后调用此接口通知服务端，携带切换结果及设备一机一密鉴权信息。
 */
public class CloudEnvironmentSwitchParam {
    private List<CloudEnvDeviceList> devicesList = new ArrayList<>();
    private CloudEnvTargetConfig targetConfig = new CloudEnvTargetConfig();

    public CloudEnvironmentSwitchParam() {
    }

    /** 设备列表，每个元素代表一组拓扑设备 */
    public List<CloudEnvDeviceList> getDevicesList() {
        return devicesList;
    }

    /** @param devicesList 设备列表，每个元素代表一组拓扑设备 */
    public void setDevicesList(List<CloudEnvDeviceList> devicesList) {
        this.devicesList = devicesList != null ? devicesList : new ArrayList<>();
    }

    /** 目标云环境配置 */
    public CloudEnvTargetConfig getTargetConfig() {
        return targetConfig;
    }

    /** @param targetConfig 目标云环境配置 */
    public void setTargetConfig(CloudEnvTargetConfig targetConfig) {
        this.targetConfig = targetConfig != null ? targetConfig : new CloudEnvTargetConfig();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvironmentSwitchParam that = (CloudEnvironmentSwitchParam) o;
        return Objects.equals(devicesList, that.devicesList)
                && Objects.equals(targetConfig, that.targetConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devicesList, targetConfig);
    }

    @Override
    public String toString() {
        return "CloudEnvironmentSwitchParam{devicesList=" + devicesList + ", targetConfig=" + targetConfig + '}';
    }
}
