// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 负载相机列表。
 */
public final class AircraftPayloadsCamerasList {

    private List<AircraftPayloadCameraInfo> camerasList;
    public AircraftPayloadsCamerasList() {
    }

/**
     * 构造 AircraftPayloadsCamerasList。
     *
     * @param camerasList 相机列表
     */
    public AircraftPayloadsCamerasList(List<AircraftPayloadCameraInfo> camerasList) {
        this.camerasList = camerasList != null ? camerasList : Collections.emptyList();
    }

    /** 相机列表 */
    public List<AircraftPayloadCameraInfo> getCamerasList() {
        return camerasList;
    }

    /** 相机列表 */
    public void setCamerasList(List<AircraftPayloadCameraInfo> camerasList) {
        this.camerasList = camerasList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadsCamerasList that = (AircraftPayloadsCamerasList) o;
        return Objects.equals(camerasList, that.camerasList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(camerasList);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsCamerasList{camerasList=" + camerasList + '}';
    }
}
