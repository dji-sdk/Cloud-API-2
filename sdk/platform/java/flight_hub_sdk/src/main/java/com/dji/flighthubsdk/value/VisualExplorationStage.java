// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 视觉勘探任务阶段。 */
public enum VisualExplorationStage {
    /** 空闲 */
    IDLE(0),
    /** 起飞 */
    TAKE_OFF(1),
    /** 手动控制 */
    MANUAL(2),
    /** 转移至离场点 */
    SCOUT_TRANSFER(3),
    /** 离场点踏勘 */
    SCOUT(4),
    /** 转移至方形航线起点 */
    PATROL_TRANSFER(5),
    /** 执行方形航线 */
    PATROL(6),
    /** 转移至返航起点 */
    GO_HOME_TRANSFER(7),
    /** 返航 */
    GO_HOME(8),
    /** 生成进离场航线及离线地图数据中 */
    GENERATE(9),
    /** 进离场航线和离线地图下载成功 */
    DOWNLOAD_FINISH(10),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    VisualExplorationStage(int value) {
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
    public static VisualExplorationStage fromValue(int value) {
        for (VisualExplorationStage e : VisualExplorationStage.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
