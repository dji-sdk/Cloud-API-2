// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 机场关联的飞机信息 */
public class AIInsideAssociateDrone {
    /** 飞机 SN */
    private String deviceSn = "";
    /** 飞机型号信息 */
    private AIInsideDeviceModel deviceModel;
    /** 飞机是否在线 */
    private boolean deviceOnlineStatus;
    /** 飞机工作状态码，取值语义同 {@link com.dji.flighthubsdk.value.DroneModeCode}（设备物模型 mode_code）：0=待机, 1=起飞准备, 2=起飞准备完毕, 3=手动飞行, 4=自动起飞, 5=航线飞行, 6=全景拍照, 7=智能跟随, 8=ADS-B躲避, 9=自动返航, 10=自动降落, 11=强制降落, 12=三桨叶降落, 13=升级中, 14=未连接, 15=APAS避障, 16=虚拟摇杆, 17=指点飞行, 18=RTK收敛中, 19=机场选址中, 20=POI环绕, 21=离场航线, 22=中继选址中, 65535=未知 */
    private double deviceWorkingStatus;
    /** 飞机当前算法装载状态 */
    private AIInsideModelInfo aiModelInfo;

    public AIInsideAssociateDrone() {}

    /** 飞机 SN */
    public String getDeviceSn() { return deviceSn; }

    /** @param v 飞机 SN */
    public void setDeviceSn(String v) { this.deviceSn = v != null ? v : ""; }

    /** 飞机型号信息 */
    public AIInsideDeviceModel getDeviceModel() { return deviceModel; }

    /** @param v 飞机型号信息 */
    public void setDeviceModel(AIInsideDeviceModel v) { this.deviceModel = v; }

    /** 飞机是否在线 */
    public boolean isDeviceOnlineStatus() { return deviceOnlineStatus; }

    /** @param v 飞机是否在线 */
    public void setDeviceOnlineStatus(boolean v) { this.deviceOnlineStatus = v; }

    /** 飞机工作状态码，取值语义同 {@link com.dji.flighthubsdk.value.DroneModeCode}（设备物模型 mode_code）：0=待机, 1=起飞准备, 2=起飞准备完毕, 3=手动飞行, 4=自动起飞, 5=航线飞行, 6=全景拍照, 7=智能跟随, 8=ADS-B躲避, 9=自动返航, 10=自动降落, 11=强制降落, 12=三桨叶降落, 13=升级中, 14=未连接, 15=APAS避障, 16=虚拟摇杆, 17=指点飞行, 18=RTK收敛中, 19=机场选址中, 20=POI环绕, 21=离场航线, 22=中继选址中, 65535=未知 */
    public double getDeviceWorkingStatus() { return deviceWorkingStatus; }

    /** @param v 飞机工作状态码，取值语义同 {@link com.dji.flighthubsdk.value.DroneModeCode}（设备物模型 mode_code）：0=待机, 1=起飞准备, 2=起飞准备完毕, 3=手动飞行, 4=自动起飞, 5=航线飞行, 6=全景拍照, 7=智能跟随, 8=ADS-B躲避, 9=自动返航, 10=自动降落, 11=强制降落, 12=三桨叶降落, 13=升级中, 14=未连接, 15=APAS避障, 16=虚拟摇杆, 17=指点飞行, 18=RTK收敛中, 19=机场选址中, 20=POI环绕, 21=离场航线, 22=中继选址中, 65535=未知 */
    public void setDeviceWorkingStatus(double v) { this.deviceWorkingStatus = v; }

    /** 飞机当前算法装载状态 */
    public AIInsideModelInfo getAiModelInfo() { return aiModelInfo; }

    /** @param v 飞机当前算法装载状态 */
    public void setAiModelInfo(AIInsideModelInfo v) { this.aiModelInfo = v; }
}
