// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 云环境切换回调请求参数（POST /device/api/v1/service/cloud_environment_switch/callback）。
 * 设备侧执行云环境切换后调用此接口上报切换结果（含一机一密鉴权信息）。
 */
public class CloudEnvironmentSwitchCallbackParam {
    private int code = 0;
    private String status = "";
    private DeviceAuthVerifyParam deviceAuthVerify = new DeviceAuthVerifyParam();
    private List<CloudEnvDeviceList> devicesList = new ArrayList<>();
    private CloudEnvTargetConfig targetConfig = new CloudEnvTargetConfig();

    public CloudEnvironmentSwitchCallbackParam() {
    }

    /** 0=success, 非0=failed */
    public int getCode() {
        return code;
    }

    /** 0=success, 非0=failed */
    public void setCode(int code) {
        this.code = code;
    }

    /** ok/failed */
    public String getStatus() {
        return status;
    }

    /** ok/failed */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 设备一机一密鉴权信息 */
    public DeviceAuthVerifyParam getDeviceAuthVerify() {
        return deviceAuthVerify;
    }

    /** @param deviceAuthVerify 设备一机一密鉴权信息 */
    public void setDeviceAuthVerify(DeviceAuthVerifyParam deviceAuthVerify) {
        this.deviceAuthVerify = deviceAuthVerify != null ? deviceAuthVerify : new DeviceAuthVerifyParam();
    }

    /** 切换的设备列表 */
    public List<CloudEnvDeviceList> getDevicesList() {
        return devicesList;
    }

    /** @param devicesList 切换的设备列表 */
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
        CloudEnvironmentSwitchCallbackParam that = (CloudEnvironmentSwitchCallbackParam) o;
        return code == that.code
                && Objects.equals(status, that.status)
                && Objects.equals(deviceAuthVerify, that.deviceAuthVerify)
                && Objects.equals(devicesList, that.devicesList)
                && Objects.equals(targetConfig, that.targetConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, status, deviceAuthVerify, devicesList, targetConfig);
    }

    @Override
    public String toString() {
        return "CloudEnvironmentSwitchCallbackParam{code=" + code + ", status='" + status + '\''
                + ", deviceAuthVerify=" + deviceAuthVerify + ", devicesList=" + devicesList
                + ", targetConfig=" + targetConfig + '}';
    }
}
