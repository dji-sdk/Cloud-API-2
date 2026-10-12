// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/**
 * 相机模式。
 * <p>对外接口中 {@code camera_mode} / {@code getCameraMode()} 等字段以 {@code int} 传递，
 * 数值须与本枚举 {@link #getValue()} 一致。</p>
 * <ul>
 *   <li>{@link #PHOTO} = 0 — 拍照</li>
 *   <li>{@link #VIDEO} = 1 — 录像</li>
 *   <li>{@link #SMART_PHOTO} = 2 — 智能低光拍照</li>
 *   <li>{@link #PANO_PHOTO} = 3 — 全景拍照</li>
 *   <li>{@link #INTERVAL_PHOTO} = 4 — 定时拍照</li>
 *   <li>{@link #UNKNOWN} = 65535 — 未知或未在表中定义的取值</li>
 * </ul>
 */
public enum CameraMode {

    /** 拍照（0） */
    PHOTO(0),
    /** 录像（1） */
    VIDEO(1),
    /** 智能低光拍照（2） */
    SMART_PHOTO(2),
    /** 全景拍照（3） */
    PANO_PHOTO(3),
    /** 定时拍照（4） */
    INTERVAL_PHOTO(4),
    /** 未知或未在表中定义的取值（65535） */
    UNKNOWN(65535);

    private final int value;

    CameraMode(int value) {
        this.value = value;
    }

    /** 整型取值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #UNKNOWN}。
     *
     * @param value 与 {@code CameraState::camera_mode} 等一致的原始值
     */
    public static CameraMode fromValue(int value) {
        for (CameraMode m : CameraMode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
