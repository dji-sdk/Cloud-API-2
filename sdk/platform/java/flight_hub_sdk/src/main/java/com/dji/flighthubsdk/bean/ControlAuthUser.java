// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 控制权用户信息。 */
public final class ControlAuthUser {
    /** 控制者呼号 */
    private String callSign = "";
    /** 控制用户 ID */
    private String userId = "";
    /** 控制用户类型：cloud / rc */
    private String type = "";

    public ControlAuthUser() {
    }

    /** 控制者呼号 */
    public String getCallSign() { return callSign; }
    /** @param callSign 控制者呼号 */
    public void setCallSign(String callSign) { this.callSign = callSign != null ? callSign : ""; }

    /** 控制用户 ID */
    public String getUserId() { return userId; }
    /** @param userId 控制用户 ID */
    public void setUserId(String userId) { this.userId = userId != null ? userId : ""; }

    /** 控制用户类型：cloud / rc */
    public String getType() { return type; }
    /** @param type 控制用户类型：cloud / rc */
    public void setType(String type) { this.type = type != null ? type : ""; }
}
