// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/**
 * 媒体文件类型（后端 file_type 枚举，多接口共用）。
 * <p>
 * 不同接口实际返回的取值范围不同，具体参见各接口字段注释。
 */
public enum MediaType {

    /** 未定义类型 */
    NO_TYPE(0),
    /** 文件夹 */
    DIR(1),
    /** 图片 */
    IMAGE(2),
    /** 视频 */
    VIDEO(3),
    /** 模型（未知/不支持格式的默认值） */
    MODEL(4),
    /** 航线文件 */
    WAY_LINE(5),
    /** 任务航线 */
    TASK_WAYLINE(6),
    /** 压缩包 */
    ZIP(7),
    /** 2D 模型 */
    MODEL_2D(8),
    /** 3D 模型 */
    MODEL_3D(9),
    /** 用户自定义云媒体 */
    CUSTOM_CLOUD(10),
    /** 后重建中间状态文件夹 */
    MODEL_GATHER_DIR(11),
    /** PPK 文件 */
    PPK(12),
    /** XMP 文件 */
    XMP(20),
    /** 开放模型 */
    OPEN_MODEL(21),
    /** 开放媒体 */
    OPEN_MEDIA(22);

    private final int value;

    MediaType(int value) {
        this.value = value;
    }

    /** 获取枚举对应的整型值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #NO_TYPE}。
     *
     * @param value 整型原始值
     * @return 对应枚举常量
     */
    public static MediaType fromValue(int value) {
        for (MediaType e : MediaType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return NO_TYPE;
    }
}
