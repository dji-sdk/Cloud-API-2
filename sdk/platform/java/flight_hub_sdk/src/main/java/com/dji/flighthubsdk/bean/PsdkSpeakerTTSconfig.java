// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.MegaphoneTTSLanguageType;
import com.dji.flighthubsdk.value.MegaphoneTTSVoiceType;

import java.util.Objects;
/**
 * 喊话器 TTS 配置。
 */
public class PsdkSpeakerTTSconfig {

    private int volume;
    /** `MegaphoneTTSVoiceType`（`value.h`） */
    private MegaphoneTTSVoiceType type = MegaphoneTTSVoiceType.MAN;
    /** `MegaphoneTTSLanguageType` */
    private MegaphoneTTSLanguageType language = MegaphoneTTSLanguageType.CHINESE;
    private int speed;

    public PsdkSpeakerTTSconfig() {
    }


    /** 音量 */
    public int getVolume() {
        return volume;
    }

    /** @param volume 音量 */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /** `MegaphoneTTSVoiceType`（`value.h`） */
    public MegaphoneTTSVoiceType getType() {
        return type;
    }

    /** @param type `MegaphoneTTSVoiceType`（`value.h`） */
    public void setType(MegaphoneTTSVoiceType type) {
        this.type = type;
    }

    /** `MegaphoneTTSLanguageType` */
    public MegaphoneTTSLanguageType getLanguage() {
        return language;
    }

    /** @param language `MegaphoneTTSLanguageType` */
    public void setLanguage(MegaphoneTTSLanguageType language) {
        this.language = language;
    }

    /** TTS语速 */
    public int getSpeed() {
        return speed;
    }

    /** @param speed TTS语速 */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkSpeakerTTSconfig that = (PsdkSpeakerTTSconfig) o;
        return volume == that.volume
                && Objects.equals(type, that.type)
                && Objects.equals(language, that.language)
                && speed == that.speed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volume, type, language, speed);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerTTSconfig{"
                + "volume=" + volume
                + ", type=" + type
                + ", language=" + language
                + ", speed=" + speed
                + '}';
    }
}
