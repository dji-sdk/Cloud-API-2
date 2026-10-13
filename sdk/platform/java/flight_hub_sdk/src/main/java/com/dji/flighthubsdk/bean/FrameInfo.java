// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 视频帧信息。 */
public final class FrameInfo {

    private boolean keyFrame;
    public FrameInfo() {
    }

/**
     * 构造 FrameInfo。
     *
     * @param keyFrame 是否关键帧
     */
    public FrameInfo(boolean keyFrame) {
        this.keyFrame = keyFrame;
    }

    /** 是否关键帧 */
    public boolean isKeyFrame() {
        return keyFrame;
    }

    /** 是否关键帧 */
    public void setKeyFrame(boolean keyFrame) {
        this.keyFrame = keyFrame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FrameInfo frameInfo = (FrameInfo) o;
        return keyFrame == frameInfo.keyFrame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyFrame);
    }

    @Override
    public String toString() {
        return "FrameInfo{keyFrame=" + keyFrame + '}';
    }
}
