// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.IRelay;
import com.dji.flighthubsdk.generated.IRelayControlModule;
import com.dji.flighthubsdk.generated.IRtkCalibrationModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 中继设备接口
 * <p>通过 {@code FlightHubSDK.getRelayInstance(sn)} 获取实例。</p>
 */
public class Relay {

    private final IRelay relay;
    private final String sn;
    private RelayControlModule relayControlModule;
    private RtkCalibrationModule rtkCalibrationModule;

    public Relay(IRelay relay, String sn) {
        this.relay = relay;
        this.sn = sn;
        Logger.info("Relay", "[" + sn + "] Relay instance created");
    }

    /**
     * 获取设备序列号
     */
    public String getSn() {
        return sn;
    }

    /**
     * 获取中继控制模块
     */
    public synchronized RelayControlModule getRelayControlModule() {
        if (relayControlModule == null) {
            IRelayControlModule module = relay.getRelayControlModule();
            if (module != null) {
                relayControlModule = new RelayControlModule(module);
            }
        }
        return relayControlModule;
    }

    /**
     * 获取 RTK 标定模块
     */
    public synchronized RtkCalibrationModule getRtkCalibrationModule() {
        if (rtkCalibrationModule == null) {
            IRtkCalibrationModule module = relay.getRtkCalibrationModule();
            if (module != null) {
                rtkCalibrationModule = new RtkCalibrationModule(module);
            }
        }
        return rtkCalibrationModule;
    }
}
