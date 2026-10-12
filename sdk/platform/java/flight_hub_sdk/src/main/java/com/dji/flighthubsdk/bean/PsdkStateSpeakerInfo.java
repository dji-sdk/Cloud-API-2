// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * PSDK 喊话器状态。
 */
public final class PsdkStateSpeakerInfo {

    /** `MegaphoneWorkMode`（`value.h`） */
    private int workMode;
    /** `MegaphonePlayMode` */
    private int playMode;
    /** `PSDKMegaphoneSystemState` */
    private int systemState;
    private int playVolume;
    private String playFileName;
    private String playFileMd5;
    private int ttsVolume;
    /** 0：男生 1：女生 */
    private int ttsType;
    /** 0：中文 1：英文 */
    private int ttsLanguage;
    private int ttsSpeed;
    public PsdkStateSpeakerInfo() {
    }

/**
     * 构造 PsdkStateSpeakerInfo。
     *
     * @param workMode 工作模式
     * @param playMode 播放模式
     * @param systemState 系统状态
     * @param playVolume 播放音量
     * @param playFileName 播放文件名
     * @param playFileMd5 播放文件MD5
     * @param ttsVolume TTS音量
     * @param ttsType TTS类型，0：男声 1：女声
     * @param ttsLanguage TTS语言，0：中文 1：英文
     * @param ttsSpeed TTS语速
     */
    public PsdkStateSpeakerInfo(
            int workMode,
            int playMode,
            int systemState,
            int playVolume,
            String playFileName,
            String playFileMd5,
            int ttsVolume,
            int ttsType,
            int ttsLanguage,
            int ttsSpeed) {
        this.workMode = workMode;
        this.playMode = playMode;
        this.systemState = systemState;
        this.playVolume = playVolume;
        this.playFileName = playFileName != null ? playFileName : "";
        this.playFileMd5 = playFileMd5 != null ? playFileMd5 : "";
        this.ttsVolume = ttsVolume;
        this.ttsType = ttsType;
        this.ttsLanguage = ttsLanguage;
        this.ttsSpeed = ttsSpeed;
    }

    /** `MegaphoneWorkMode`（`value.h`） */
    public int getWorkMode() { return workMode; }
    /** `MegaphonePlayMode` */
    public int getPlayMode() { return playMode; }
    /** `PSDKMegaphoneSystemState` */
    public int getSystemState() { return systemState; }
    /** 播放音量 */
    public int getPlayVolume() { return playVolume; }
    /** 播放文件名 */
    public String getPlayFileName() { return playFileName; }
    /** 播放文件MD5 */
    public String getPlayFileMd5() { return playFileMd5; }
    /** TTS音量 */
    public int getTtsVolume() { return ttsVolume; }
    /** 0：男生 1：女生 */
    public int getTtsType() { return ttsType; }
    /** 0：中文 1：英文 */
    public int getTtsLanguage() { return ttsLanguage; }
    /** TTS语速 */
    public int getTtsSpeed() { return ttsSpeed; }

    /** @param workMode `MegaphoneWorkMode`（`value.h`） */
    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    /** @param playMode `MegaphonePlayMode` */
    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    /** @param systemState `PSDKMegaphoneSystemState` */
    public void setSystemState(int systemState) {
        this.systemState = systemState;
    }

    /** 播放音量 */
    public void setPlayVolume(int playVolume) {
        this.playVolume = playVolume;
    }

    /** 播放文件名 */
    public void setPlayFileName(String playFileName) {
        this.playFileName = playFileName;
    }

    /** 播放文件MD5 */
    public void setPlayFileMd5(String playFileMd5) {
        this.playFileMd5 = playFileMd5;
    }

    /** TTS音量 */
    public void setTtsVolume(int ttsVolume) {
        this.ttsVolume = ttsVolume;
    }

    /** @param ttsType 0：男生 1：女生 */
    public void setTtsType(int ttsType) {
        this.ttsType = ttsType;
    }

    /** @param ttsLanguage 0：中文 1：英文 */
    public void setTtsLanguage(int ttsLanguage) {
        this.ttsLanguage = ttsLanguage;
    }

    /** TTS语速 */
    public void setTtsSpeed(int ttsSpeed) {
        this.ttsSpeed = ttsSpeed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkStateSpeakerInfo that = (PsdkStateSpeakerInfo) o;
        return workMode == that.workMode
                && playMode == that.playMode
                && systemState == that.systemState
                && playVolume == that.playVolume
                && ttsVolume == that.ttsVolume
                && ttsType == that.ttsType
                && ttsLanguage == that.ttsLanguage
                && ttsSpeed == that.ttsSpeed
                && Objects.equals(playFileName, that.playFileName)
                && Objects.equals(playFileMd5, that.playFileMd5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                workMode,
                playMode,
                systemState,
                playVolume,
                playFileName,
                playFileMd5,
                ttsVolume,
                ttsType,
                ttsLanguage,
                ttsSpeed);
    }

    @Override
    public String toString() {
        return "PsdkStateSpeakerInfo{"
                + "workMode=" + workMode
                + ", playMode=" + playMode
                + ", systemState=" + systemState
                + ", playVolume=" + playVolume
                + ", playFileName='" + playFileName + '\''
                + ", playFileMd5='" + playFileMd5 + '\''
                + ", ttsVolume=" + ttsVolume
                + ", ttsType=" + ttsType
                + ", ttsLanguage=" + ttsLanguage
                + ", ttsSpeed=" + ttsSpeed
                + '}';
    }
}
