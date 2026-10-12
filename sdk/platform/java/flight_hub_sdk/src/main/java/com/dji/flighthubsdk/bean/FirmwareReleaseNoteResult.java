// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件发布说明条目 */
public final class FirmwareReleaseNoteResult {

    private String deviceModelKey;
    private String lang;
    private String productVersion;
    private String releaseNote;
    private String releasedTime;
    public FirmwareReleaseNoteResult() {
    }

/**
     * 构造 FirmwareReleaseNoteResult。
     *
     * @param deviceModelKey 设备型号标识
     * @param lang lang
     * @param productVersion 产品版本
     * @param releaseNote 发布说明
     * @param releasedTime 发布时间
     */
    public FirmwareReleaseNoteResult(
            String deviceModelKey, String lang, String productVersion, String releaseNote, String releasedTime) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.lang = lang != null ? lang : "";
        this.productVersion = productVersion != null ? productVersion : "";
        this.releaseNote = releaseNote != null ? releaseNote : "";
        this.releasedTime = releasedTime != null ? releasedTime : "";
    }

    /** 设备型号标识 */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** 设备型号标识 */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** lang */
    public String getLang() {
        return lang;
    }

    /** lang */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /** 产品版本 */
    public String getProductVersion() {
        return productVersion;
    }

    /** 产品版本 */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /** 发布说明 */
    public String getReleaseNote() {
        return releaseNote;
    }

    /** 发布说明 */
    public void setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
    }

    /** 发布时间 */
    public String getReleasedTime() {
        return releasedTime;
    }

    /** 发布时间 */
    public void setReleasedTime(String releasedTime) {
        this.releasedTime = releasedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareReleaseNoteResult that = (FirmwareReleaseNoteResult) o;
        return Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(lang, that.lang)
                && Objects.equals(productVersion, that.productVersion)
                && Objects.equals(releaseNote, that.releaseNote)
                && Objects.equals(releasedTime, that.releasedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceModelKey,
                lang,
                productVersion,
                releaseNote,
                releasedTime);
    }

    @Override
    public String toString() {
        return "FirmwareReleaseNoteResult{"
                + "deviceModelKey='" + deviceModelKey + '\''
                + ", lang='" + lang + '\''
                + ", productVersion='" + productVersion + '\''
                + ", releaseNote='" + releaseNote + '\''
                + ", releasedTime='" + releasedTime + '\''
                + '}';
    }
}
