// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 云环境切换结果（topic_cloud_environment_switch 推送 / HTTP 触发回调结果）。 */
public final class CloudEnvironmentSwitchResult {
    private String message;
    private String bid;
    private int progress;

    public CloudEnvironmentSwitchResult() {
    }

    /**
     * 构造 CloudEnvironmentSwitchResult。
     *
     * @param message 结果消息
     * @param bid 业务 ID
     * @param progress 进度
     */
    public CloudEnvironmentSwitchResult(String message, String bid, int progress) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
        this.progress = progress;
    }

    /** 结果消息 */
    public String getMessage() {
        return message;
    }

    /** 结果消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务 ID */
    public String getBid() {
        return bid;
    }

    /** 业务 ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 进度 */
    public int getProgress() {
        return progress;
    }

    /** 进度 */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvironmentSwitchResult that = (CloudEnvironmentSwitchResult) o;
        return progress == that.progress
                && Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid, progress);
    }

    @Override
    public String toString() {
        return "CloudEnvironmentSwitchResult{message='" + message + '\'' + ", bid='" + bid + '\'' + ", progress=" + progress + '}';
    }
}
