// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** POI 任务状态。 */
public enum POITaskStatus {

    /** 待开始 */
    WAITING(0),
    /** 启动失败 */
    STARTING_FAILURE(1),
    /** 执行中（飞行） */
    EXECUTING(2),
    /** 已暂停 */
    PAUSED(3),
    /** 终止 */
    TERMINATED(4),
    /** 成功 */
    SUCCESS(5),
    /** 挂起 */
    SUSPENDED(6),
    /** 超时 */
    TIMEOUT(7),
    /** 部分执行状态 */
    PARTIALLY_DONE(8),
    /** 准备中的任务 */
    PREPARING(9),
    /** 起飞排队中 */
    QUEUE_FOR_TAKEOFF(10),
    /** 未知或未在表中定义的取值 */
    UNKNOWN(65535);

    private final int value;

    POITaskStatus(int value) {
        this.value = value;
    }

    /** 整型取值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #UNKNOWN}。
     *
     * @param value 与 {@code POITaskInfo::status} 一致的原始值
     * @return 对应枚举常量，未知时 {@link #UNKNOWN}
     */
    public static POITaskStatus fromValue(int value) {
        for (POITaskStatus m : POITaskStatus.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
