// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 星历下载与上传阶段。 */
public enum Stage {
    /** 空闲 */
    IDLE(0),
    /** 下载中 */
    DOWNLOADING(1),
    /** 下载成功 */
    DOWNLOAD_SUCCESSED(2),
    /** 下载失败 */
    DOWNLOAD_FAILED(3),
    /** 上传中 */
    UPLOADING(4),
    /** 上传成功 */
    UPLOAD_SUCCESSED(5),
    /** 上传失败 */
    UPLOAD_FAILED(6),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    Stage(int value) {
        this.value = value;
    }

    /** 获取枚举对应的整型值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 UNKNOWN。
     *
     * @param value 整型原始值
     * @return 对应枚举常量
     */
    public static Stage fromValue(int value) {
        for (Stage e : Stage.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
