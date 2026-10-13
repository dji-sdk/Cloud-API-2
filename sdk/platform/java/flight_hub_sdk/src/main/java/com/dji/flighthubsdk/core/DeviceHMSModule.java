// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.IDeviceHMSModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备 HMS 实时推送模块（机场/飞机 WS 告警，统一 listenDeviceHMS）。
 */
public class DeviceHMSModule {

    private final IDeviceHMSModule deviceHmsModule;
    private final String logTag;

    public DeviceHMSModule(IDeviceHMSModule deviceHmsModule, String logTag) {
        this.deviceHmsModule = deviceHmsModule;
        this.logTag = logTag != null ? logTag : "DeviceHMSModule";
    }

    /**
     * 订阅设备 HMS 实时告警
     */
    public int listenDeviceHMS(FlightHubCallback.DeviceHmsCallback callback) {
        try {
            com.dji.flighthubsdk.generated.HmsDataCallback swigCallback =
                    new com.dji.flighthubsdk.generated.HmsDataCallback() {
                        @Override
                        public void onHmsData(com.dji.flighthubsdk.generated.HmsData hms_data) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toHmsData(hms_data));
                                }
                            } catch (Exception e) {
                                Logger.error(logTag, "Device HMS callback error: " + e.getMessage(), e);
                            }
                        }
                    };

            int listenId = deviceHmsModule.listenDeviceHMSWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenDeviceHMS error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toHmsData(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅设备 HMS 实时告警
     * @param listenId 监听 ID
     */
    public void cancelListenDeviceHMS(int listenId) {
        try {
            deviceHmsModule.cancelListenDeviceHMSWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenDeviceHMS error: " + e.getMessage(), e);
        }
    }
}
