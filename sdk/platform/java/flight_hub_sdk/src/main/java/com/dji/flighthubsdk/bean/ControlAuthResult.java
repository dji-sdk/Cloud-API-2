// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** acquire/release 控制权结果。 */
public final class ControlAuthResult {
    /** 飞机 SN */
    private String droneSn = "";
    /** 手飞机型授权 bid */
    private String bid = "";
    /** 控制权列表 */
    private List<ControlAuthController> controls = new ArrayList<>();

    public ControlAuthResult() {
    }

    /** 飞机 SN */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 飞机 SN */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }

    /** 手飞机型授权 bid */
    public String getBid() { return bid; }
    /** @param bid 手飞机型授权 bid */
    public void setBid(String bid) { this.bid = bid != null ? bid : ""; }

    /** 控制权列表 */
    public List<ControlAuthController> getControls() { return controls; }
    /** @param controls 控制权列表 */
    public void setControls(List<ControlAuthController> controls) {
        this.controls = controls != null ? controls : new ArrayList<>();
    }
}
