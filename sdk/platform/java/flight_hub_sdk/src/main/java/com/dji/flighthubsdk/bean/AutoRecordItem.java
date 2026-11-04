// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** PAAS 自动录制配置项（单个设备/相机的录制目标） */
public class AutoRecordItem {
    private String sn = "";
    private String cameraIndex = "";
    private String cameraSn = "";
    private int recordingStrategy = 0;

    public AutoRecordItem() {
    }

    /** 设备 SN（必填） */
    public String getSn() { return sn; }
    /** @param sn 设备 SN（必填） */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }
    /** 负载相机索引（形如 "39-0-7"），与 cameraSn 二选一 */
    public String getCameraIndex() { return cameraIndex; }
    /** @param cameraIndex 负载相机索引（形如 "39-0-7"），与 cameraSn 二选一 */
    public void setCameraIndex(String cameraIndex) { this.cameraIndex = cameraIndex != null ? cameraIndex : ""; }
    /** 第三方外接相机 SN，与 cameraIndex 二选一 */
    public String getCameraSn() { return cameraSn; }
    /** @param cameraSn 第三方外接相机 SN，与 cameraIndex 二选一 */
    public void setCameraSn(String cameraSn) { this.cameraSn = cameraSn != null ? cameraSn : ""; }
    /** 只读，服务端按设备类型计算：0=任务录制 1=持续录制 */
    public int getRecordingStrategy() { return recordingStrategy; }
    /** @param recordingStrategy 只读，服务端按设备类型计算：0=任务录制 1=持续录制 */
    public void setRecordingStrategy(int recordingStrategy) { this.recordingStrategy = recordingStrategy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AutoRecordItem that = (AutoRecordItem) o;
        return recordingStrategy == that.recordingStrategy
                && Objects.equals(sn, that.sn)
                && Objects.equals(cameraIndex, that.cameraIndex)
                && Objects.equals(cameraSn, that.cameraSn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, cameraIndex, cameraSn, recordingStrategy);
    }

    @Override
    public String toString() {
        return "AutoRecordItem{sn='" + sn + '\'' + ", cameraIndex='" + cameraIndex + '\''
                + ", cameraSn='" + cameraSn + '\'' + ", recordingStrategy=" + recordingStrategy + '}';
    }
}
