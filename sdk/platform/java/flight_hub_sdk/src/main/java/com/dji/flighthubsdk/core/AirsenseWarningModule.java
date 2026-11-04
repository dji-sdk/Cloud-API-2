// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.IAirsenseWarningModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * AirSense（ADS-B）民航飞机告警实时推送模块。
 */
public class AirsenseWarningModule {

    private final IAirsenseWarningModule airsenseWarningModule;
    private final String logTag;

    public AirsenseWarningModule(IAirsenseWarningModule airsenseWarningModule, String logTag) {
        this.airsenseWarningModule = airsenseWarningModule;
        this.logTag = logTag != null ? logTag : "AirsenseWarningModule";
    }

    /**
     * 订阅 AirSense 民航飞机告警
     * @param callback 告警回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_airsense_warning
     */
    public int listenAirsenseWarning(FlightHubCallback.AirsenseWarningCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AirsenseWarningCallback swigCallback =
                    new com.dji.flighthubsdk.generated.AirsenseWarningCallback() {
                        @Override
                        public void onAirsenseWarning(com.dji.flighthubsdk.generated.AirsenseWarning msg) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toAirsenseWarning(msg));
                                }
                            } catch (Exception e) {
                                Logger.error(logTag, "Airsense warning callback error: " + e.getMessage(), e);
                            }
                        }
                    };

            int listenId = airsenseWarningModule.listenAirsenseWarningWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenAirsenseWarning error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 AirSense 民航飞机告警
     * @param listenId 监听 ID
     */
    public void cancelListenAirsenseWarning(int listenId) {
        try {
            airsenseWarningModule.cancelListenAirsenseWarningWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenAirsenseWarning error: " + e.getMessage(), e);
        }
    }
}
