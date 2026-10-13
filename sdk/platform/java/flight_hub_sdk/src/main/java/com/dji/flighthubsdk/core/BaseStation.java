// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.generated.IBaseStation;
import com.dji.flighthubsdk.generated.ILiveDeviceModule;
import com.dji.flighthubsdk.generated.INetworkTestModule;
import com.dji.flighthubsdk.generated.IRtkCalibrationModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 基站设备接口。
 * <p>LiveInfo.device_sn = 基站 SN（不是摄像头 SN）；payload_index = camera_index。</p>
 */
public class BaseStation {

    private final IBaseStation baseStation;
    private final String sn;
    private RtkCalibrationModule rtkCalibrationModule;
    private NetworkTestModule networkTestModule;
    private LiveDeviceModule liveDeviceModule;
    private final java.util.concurrent.ConcurrentHashMap<String, LiveModule> liveModuleRef =
            new java.util.concurrent.ConcurrentHashMap<>();

    public BaseStation(IBaseStation baseStation, String sn) {
        this.baseStation = baseStation;
        this.sn = sn;
        Logger.info("BaseStation", "[" + sn + "] BaseStation instance created");
    }

    /**
     * 获取设备序列号
     */
    public String getSn() {
        return sn;
    }

    /**
     * 获取直播模块（按 LiveInfo 区分不同摄像头/视频流）
     * 缓存 key = payload_index + ":" + video_id（多摄必须含 video_id）
     */
    public synchronized LiveModule getLiveStream(LiveInfo params) {
        if (params == null) {
            Logger.warn("BaseStation", "getLiveStream: params is null");
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo nativeInfo = null;
        try {
            nativeInfo = FlightHubSdkConvert.toNativeLiveInfo(params);
            String cacheKey = liveCacheKey(params);
            if (liveModuleRef.containsKey(cacheKey)) {
                return liveModuleRef.get(cacheKey);
            }
            LiveModule liveModule = new LiveModule(this.baseStation.getLiveStream(nativeInfo), this.sn);
            Logger.info("BaseStation", "[" + sn + "] GetLiveStream: created, key=" + cacheKey);
            liveModuleRef.put(cacheKey, liveModule);
            return liveModule;
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 获取设备级直播发现模块（可用直播源订阅）
     */
    public synchronized LiveDeviceModule getLiveDeviceModule() {
        if (liveDeviceModule == null) {
            ILiveDeviceModule module = baseStation.getLiveDeviceModule();
            if (module != null) {
                liveDeviceModule = new LiveDeviceModule(module, "BaseStation[" + sn + "]");
            }
        }
        return liveDeviceModule;
    }

    /**
     * 获取 RTK 标定模块
     */
    public synchronized RtkCalibrationModule getRtkCalibrationModule() {
        if (rtkCalibrationModule == null) {
            IRtkCalibrationModule module = baseStation.getRtkCalibrationModule();
            if (module != null) {
                rtkCalibrationModule = new RtkCalibrationModule(module);
            }
        }
        return rtkCalibrationModule;
    }

    /**
     * 获取网络测试模块
     */
    public synchronized NetworkTestModule getNetworkTestModule() {
        if (networkTestModule == null) {
            INetworkTestModule module = baseStation.getNetworkTestModule();
            if (module != null) {
                networkTestModule = new NetworkTestModule(module);
            }
        }
        return networkTestModule;
    }

    private static String liveCacheKey(LiveInfo params) {
        String payload = params.getPayloadIndex() != null ? params.getPayloadIndex() : "";
        String video = params.getVideoId() != null ? params.getVideoId() : "";
        return payload + ":" + video;
    }
}
