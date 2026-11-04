// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 控制权扩展信息。 */
public final class ControlAuthSpec {
    /** 是否锁定 */
    private boolean isLocked;
    /** 负载索引 */
    private String payloadIndex = "";
    /** 是否云控授权 */
    private boolean isCloudControlAuth;
    /** 等待授权用户 */
    private String waitingForAuthUser = "";

    public ControlAuthSpec() {
    }

    /** 是否锁定 */
    public boolean isLocked() { return isLocked; }
    /** @param isLocked 是否锁定 */
    public void setIsLocked(boolean isLocked) { this.isLocked = isLocked; }

    /** 负载索引 */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引 */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }

    /** 是否云控授权 */
    public boolean isCloudControlAuth() { return isCloudControlAuth; }
    /** @param isCloudControlAuth 是否云控授权 */
    public void setIsCloudControlAuth(boolean isCloudControlAuth) { this.isCloudControlAuth = isCloudControlAuth; }

    /** 等待授权用户 */
    public String getWaitingForAuthUser() { return waitingForAuthUser; }
    /** @param waitingForAuthUser 等待授权用户 */
    public void setWaitingForAuthUser(String waitingForAuthUser) {
        this.waitingForAuthUser = waitingForAuthUser != null ? waitingForAuthUser : "";
    }
}
