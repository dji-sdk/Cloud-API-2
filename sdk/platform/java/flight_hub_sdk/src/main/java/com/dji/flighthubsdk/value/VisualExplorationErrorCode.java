// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 视觉勘探/踏勘任务错误码。 */
public enum VisualExplorationErrorCode {
    /** 命令操作成功（包含：开始、终止任务） */
    CMD_SUCCESS(0),
    /** 未知指令 */
    UNKNOW_TASK_CMD(1),
    /** 注册回调失败 */
    APP_REGISTER_CALLBACK_ERROR(2),
    /** 抽象层数据异常 */
    ABSTRACT_DATD_ERROR(3),
    /** 飞机不在空中 */
    DRONE_IS_NOT_IN_AIR(4),
    /** 参数读取异常 */
    PARAM_READ_ERROR(5),
    /** 星历不存在 */
    EPHEMERIS_NOT_EXIT(6),
    /** 风速过大 */
    WIND_SPD_BIG(7),
    /** 用户暂停任务 */
    USER_REQUEST_PAUSE(8),
    /** 用户切换档位 */
    USER_CHANGE_MODE(9),
    /** 环境光过暗，无法执行踏勘选址任务 */
    RUNNING_AT_NIGHT(10),
    /** 靠近限高 */
    HEIGHT_LIMIT(11),
    /** 靠近禁飞区边界 30m */
    BOUNDARY_LIMIT(12),
    /** OA 避障 */
    OA_LIMIT(13),
    /** dbus 订阅异常 */
    REG_DBUS_EROOR(14),
    /** 失控（EA230 开始新增离场点版本） */
    RC_LOST_VE(15),
    /** 任务列表已满 */
    START_FAIL_TASK_NUM_FULL(16),
    /** 开始失败，当前任务正在执行 */
    START_FAIL_FOR_TASK_IS_RUNNING(17),
    /** 开始失败，heading_level 没有收敛到 6 */
    START_FAIL_FOR_HEADING_LEVEL(18),
    /** 开始失败，没有安装 RTK */
    START_FAIL_FOR_OSDK_MO_DULE_TYPE(19),
    /** 开始失败，GNSS 初始化失败 */
    START_FAIL_FOR_GNSS_INIT_FAIL(20),
    /** 开始失败，GPS 信号弱 */
    START_FAIL_FOR_GNSS_LEVEL_LOW(21),
    /** 开始失败，飞机定位异常 */
    START_FAIL_FOR_POS_INVALID(22),
    /** 开始失败，地图异常 */
    START_FAIL_FOR_MAP_INVALID(23),
    /** 终止失败，当前任务没有执行 */
    STOP_FAIL_FOR_TASK_IS_NOT_RUNING(32),
    /** 终止失败，未知指令 / ID 号不匹配 */
    STOP_FAIL_FOR_UNKNOW_TASK_CMD(33),
    /** 终止天空分析或搜星任务成功 */
    STOP_SKY_PARSING_SUCCESS(34),
    /** 终止搜星任务成功 */
    STOP_GNSS_ACCESS_SUCCESS(35),
    /** 终止搜星任务失败 */
    STOP_GNSS_ACCESS_FAIL(36),
    /** 踏勘任务获取星历成功 */
    EPHEMERIS_UPLOAD_SUCCESS(64),
    /** 踏勘任务获取星历失败 */
    EPHEMERIS_GET_FAILED(65),
    /** 踏勘任务解压星历失败 */
    EPHEMERIS_UNZIP_FAILED(66),
    /** 旋转超时 */
    ROTATE_TIMEOUT(80),
    /** 四周遮挡严重 */
    ACCLUSION_HIGH(81),
    /** 获取影响因子超时 */
    GET_SCALE_TIMEOUT(82),
    /** 发现障碍物 */
    FOUND_OBSTACLE(83),
    /** 获取天空图失败 */
    PARSING_FAIL(84),
    /** 获取星轨结果失败 */
    GET_SCALE_ERROR(85),
    /** 搜星失败导致任务退出 */
    TOP_TASK_FOR_GNSS_FAILE(98),
    /** 选址无搜星数据 */
    GNSS_DATA_UNAVAILABLE(99),
    /** 单点定位可用率低 */
    SPP_LOW_POS_RATE(100),
    /** 单点定位内符合精度低 */
    SPP_LOW_POS_ACCURACY(101),
    /** 单点定位参与定位卫星少 */
    SPP_POS_SAT_INSUFFICIENT(102),
    /** 单点定位 PDOP 高 */
    SPP_POS_HIGH_PDOP(103),
    /** RTK 定向固定率低 */
    HEADING_LOW_FIXRATE(104),
    /** RTK 定向切浮点解次数多 */
    HEADING_SWITCH_TO_FLOAT(105),
    /** RTK 定向精度低（包括航向和俯仰） */
    HEADING_LOW_ACCURACY(112),
    /** RTK 定向参与定位卫星少 */
    HEADING_SAT_INSUFFICIENT(113),
    /** RTK 定位结果无可用数据 */
    RTK_POS_NO_VALID_DATA(114),
    /** RTK 定位结果出现多类异常 */
    RTK_POS_TOO_MANY_ERROR_PERIODS(115),
    /** 无可用事后星历 */
    NO_EPHEMERIS(116),
    /** 星历文件无效 */
    EPHEMERIS_NOT_VALID(117),
    /** 观测数据卫星 SNR 低 */
    OBS_SAT_SNR_LOW(118),
    /** 观测数据周跳多 */
    OBS_SAT_FREQUENT_CYCLE_SLIP(119),
    /** 观测数据卫星少 */
    OBS_SAT_NUM_INSUFFICIENT(120),
    /** 时段内最少搜星少于 16 颗（开始测试 3 分钟后） */
    OBS_MIN_SAT_NUM_LESS(121),
    /** RTK 单点定位不可用 */
    RTK_POS_TYPE_INVALID(122),
    /** 搜星初始化异常 */
    GNSS_INIT_ERROR(123),
    /** 搜星结果获取异常 */
    GNSS_GET_RESULT_ERROR(124),
    /** 等待搜星结果超时 */
    GNSS_TIMEOUT(125),
    /** 爬升高度参数出错 */
    TRANSFER_HEIGHT_ERROR(128),
    /** 爬升到最高高度（60m）找不到离场点 */
    TRANSFER_HEIGHT_LIMIT(129),
    /** 爬升超时 */
    TRANSFER_TIMEOUT(130),
    /** 爬升等待 GNSS 搜星结果超时 */
    TRANSFER_GNSS_TIME_OUT(131),
    /** 踏勘任务被内部或外部终止 */
    ASSESS_RESULT_UNAVAILABLE_VE(153),
    /** mission 异常切换 */
    MISSION_SWITCH_ERROR_VE(155),
    /** 离场航线执行失败 */
    DEPARTURE_FAIL_VE(160),
    /** 离场航线初始化失败 */
    DEPARTURE_INIT_FAIL_VE(161),
    /** 离场航线不可用 */
    DEPARTURE_INVALID_VE(162),
    /** 离场点不可达（可能存在禁飞区或障碍物） */
    DEPARTURE_TARGET_NOT_REACHABLE_VE(163),
    /** 离场航线规划失败 */
    DEPARTURE_PLANNING_FAIL_VE(164),
    /** 离场点垂直位置错误 */
    DEPARTURE_POS_VERT_ERROR_VE(165),
    /** 离场点水平位置错误 */
    DEPARTURE_POS_HORIZ_ERROR_VE(166),
    /** 非 P 档不能执行离场航线 */
    DEPARTURE_RC_MODE_ERROR_VE(167),
    /** 靠近限远 */
    RADIUS_LIMIT_VE(176),
    /** 靠近自定义飞行区 */
    VFENCE_LIMIT_VE(177),
    /** Home 点未刷新 */
    HOME_NOT_SET_VE(178),
    /** 限低 */
    LOW_LIMIT_VE(179),
    /** 姿态模式 */
    ATTI_MODE_VE(180),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    VisualExplorationErrorCode(int value) {
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
    public static VisualExplorationErrorCode fromValue(int value) {
        for (VisualExplorationErrorCode e : VisualExplorationErrorCode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
