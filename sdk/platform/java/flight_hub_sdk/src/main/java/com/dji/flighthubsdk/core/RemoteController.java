// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.IRemoteController;
import com.dji.flighthubsdk.util.Logger;

/**
 * 遥控器设备接口（Phase 1：身份壳，Module 后续挂载）
 * <p>通过 {@code FlightHubSDK.getRemoteControllerInstance(sn)} 获取实例。</p>
 */
public class RemoteController {

    private final IRemoteController remoteController;
    private final String sn;

    public RemoteController(IRemoteController remoteController, String sn) {
        this.remoteController = remoteController;
        this.sn = sn;
        Logger.info("RemoteController", "[" + sn + "] RemoteController instance created");
    }

    /**
     * 获取设备序列号
     */
    public String getSn() {
        return sn;
    }
}
