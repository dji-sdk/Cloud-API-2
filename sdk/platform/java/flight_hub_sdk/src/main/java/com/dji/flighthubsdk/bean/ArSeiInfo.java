// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** AR SEI 信息。 */
public final class ArSeiInfo {

    /** 相机在 NED 坐标系下的偏航角，单位：度 */
    private double cameraYaw;
    /** 相机在 NED 坐标系下的俯仰角，单位：度 */
    private double cameraPitch;
    /** 相机在 NED 坐标系下的横滚角，单位：度 */
    private double cameraRoll;
    /** 相机在 GND 坐标系下的 X 位置，单位：mm */
    private int cameraPosX;
    /** 相机在 GND 坐标系下的 Y 位置，单位：mm */
    private int cameraPosY;
    /** 相机在 GND 坐标系下的 Z 位置，单位：mm */
    private int cameraPosZ;
    /** GND 坐标系原点纬度，单位：1/2^32*180° */
    private int latitude;
    /** GND 坐标系原点经度，单位：1/2^32*360° */
    private int longitude;
    /** GND 坐标系原点 Z 轴高度，单位：mm，向下为正 */
    private int gndOriginZ;
    /** 相对起飞点高度，单位：mm，向上为正 */
    private int altitude;
    public ArSeiInfo() {
    }

/**
     * 构造 ArSeiInfo。
     *
     * @param cameraYaw 相机在 NED 坐标系下的偏航角，单位度
     * @param cameraPitch 相机在 NED 坐标系下的俯仰角，单位度
     * @param cameraRoll 相机在 NED 坐标系下的横滚角，单位度
     * @param cameraPosX 相机在 GND 坐标系下的 X 位置，单位 mm
     * @param cameraPosY 相机在 GND 坐标系下的 Y 位置，单位 mm
     * @param cameraPosZ 相机在 GND 坐标系下的 Z 位置，单位 mm
     * @param latitude 纬度，单位 1/2^32*180度
     * @param longitude 经度，单位 1/2^32*360度
     * @param gndOriginZ GND 坐标系原点 Z 轴，单位 mm，向下为正
     * @param altitude 相对起飞点高度，单位 mm，向上为正
     */
    public ArSeiInfo(
            double cameraYaw,
            double cameraPitch,
            double cameraRoll,
            int cameraPosX,
            int cameraPosY,
            int cameraPosZ,
            int latitude,
            int longitude,
            int gndOriginZ,
            int altitude) {
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
        this.cameraRoll = cameraRoll;
        this.cameraPosX = cameraPosX;
        this.cameraPosY = cameraPosY;
        this.cameraPosZ = cameraPosZ;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gndOriginZ = gndOriginZ;
        this.altitude = altitude;
    }

    /** 相机在 NED 坐标系下的偏航角，单位：度 */
    public double getCameraYaw() {
        return cameraYaw;
    }

    /** @param cameraYaw 相机在 NED 坐标系下的偏航角，单位：度 */
    public void setCameraYaw(double cameraYaw) {
        this.cameraYaw = cameraYaw;
    }

    /** 相机在 NED 坐标系下的俯仰角，单位：度 */
    public double getCameraPitch() {
        return cameraPitch;
    }

    /** @param cameraPitch 相机在 NED 坐标系下的俯仰角，单位：度 */
    public void setCameraPitch(double cameraPitch) {
        this.cameraPitch = cameraPitch;
    }

    /** 相机在 NED 坐标系下的横滚角，单位：度 */
    public double getCameraRoll() {
        return cameraRoll;
    }

    /** @param cameraRoll 相机在 NED 坐标系下的横滚角，单位：度 */
    public void setCameraRoll(double cameraRoll) {
        this.cameraRoll = cameraRoll;
    }

    /** 相机在 GND 坐标系下的 X 位置，单位：mm */
    public int getCameraPosX() {
        return cameraPosX;
    }

    /** @param cameraPosX 相机在 GND 坐标系下的 X 位置，单位：mm */
    public void setCameraPosX(int cameraPosX) {
        this.cameraPosX = cameraPosX;
    }

    /** 相机在 GND 坐标系下的 Y 位置，单位：mm */
    public int getCameraPosY() {
        return cameraPosY;
    }

    /** @param cameraPosY 相机在 GND 坐标系下的 Y 位置，单位：mm */
    public void setCameraPosY(int cameraPosY) {
        this.cameraPosY = cameraPosY;
    }

    /** 相机在 GND 坐标系下的 Z 位置，单位：mm */
    public int getCameraPosZ() {
        return cameraPosZ;
    }

    /** @param cameraPosZ 相机在 GND 坐标系下的 Z 位置，单位：mm */
    public void setCameraPosZ(int cameraPosZ) {
        this.cameraPosZ = cameraPosZ;
    }

    /** GND 坐标系原点纬度，单位：1/2^32*180° */
    public int getLatitude() {
        return latitude;
    }

    /** @param latitude GND 坐标系原点纬度，单位：1/2^32*180° */
    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    /** GND 坐标系原点经度，单位：1/2^32*360° */
    public int getLongitude() {
        return longitude;
    }

    /** @param longitude GND 坐标系原点经度，单位：1/2^32*360° */
    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    /** GND 坐标系原点 Z 轴高度，单位：mm，向下为正 */
    public int getGndOriginZ() {
        return gndOriginZ;
    }

    /** @param gndOriginZ GND 坐标系原点 Z 轴高度，单位：mm，向下为正 */
    public void setGndOriginZ(int gndOriginZ) {
        this.gndOriginZ = gndOriginZ;
    }

    /** 相对起飞点高度，单位：mm，向上为正 */
    public int getAltitude() {
        return altitude;
    }

    /** @param altitude 相对起飞点高度，单位：mm，向上为正 */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArSeiInfo arSeiInfo = (ArSeiInfo) o;
        return Double.compare(arSeiInfo.cameraYaw, cameraYaw) == 0
                && Double.compare(arSeiInfo.cameraPitch, cameraPitch) == 0
                && Double.compare(arSeiInfo.cameraRoll, cameraRoll) == 0
                && cameraPosX == arSeiInfo.cameraPosX
                && cameraPosY == arSeiInfo.cameraPosY
                && cameraPosZ == arSeiInfo.cameraPosZ
                && latitude == arSeiInfo.latitude
                && longitude == arSeiInfo.longitude
                && gndOriginZ == arSeiInfo.gndOriginZ
                && altitude == arSeiInfo.altitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cameraYaw,
                cameraPitch,
                cameraRoll,
                cameraPosX,
                cameraPosY,
                cameraPosZ,
                latitude,
                longitude,
                gndOriginZ,
                altitude);
    }

    @Override
    public String toString() {
        return "ArSeiInfo{"
                + "cameraYaw=" + cameraYaw
                + ", cameraPitch=" + cameraPitch
                + ", cameraRoll=" + cameraRoll
                + ", cameraPosX=" + cameraPosX
                + ", cameraPosY=" + cameraPosY
                + ", cameraPosZ=" + cameraPosZ
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", gndOriginZ=" + gndOriginZ
                + ", altitude=" + altitude
                + '}';
    }
}
