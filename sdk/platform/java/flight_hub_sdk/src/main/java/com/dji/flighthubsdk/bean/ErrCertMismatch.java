// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 解锁证书不匹配信息。 */
public final class ErrCertMismatch {

    /** 机场 SN */
    private String airportSn = "";
    /** 飞机 SN */
    private String aircraftSn = "";
    /** 证书文件名 */
    private String fileName = "";

    public ErrCertMismatch() {
    }

    /** @param airportSn 机场 SN */
    public String getAirportSn() { return airportSn; }

    /** @param airportSn 机场 SN */
    public void setAirportSn(String airportSn) { this.airportSn = airportSn != null ? airportSn : ""; }

    /** @param aircraftSn 飞机 SN */
    public String getAircraftSn() { return aircraftSn; }

    /** @param aircraftSn 飞机 SN */
    public void setAircraftSn(String aircraftSn) { this.aircraftSn = aircraftSn != null ? aircraftSn : ""; }

    /** @param fileName 证书文件名 */
    public String getFileName() { return fileName; }

    /** @param fileName 证书文件名 */
    public void setFileName(String fileName) { this.fileName = fileName != null ? fileName : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrCertMismatch that = (ErrCertMismatch) o;
        return Objects.equals(airportSn, that.airportSn)
                && Objects.equals(aircraftSn, that.aircraftSn)
                && Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airportSn, aircraftSn, fileName);
    }

    @Override
    public String toString() {
        return "ErrCertMismatch{airportSn='" + airportSn + '\''
                + ", aircraftSn='" + aircraftSn + '\''
                + ", fileName='" + fileName + '\'' + '}';
    }
}
