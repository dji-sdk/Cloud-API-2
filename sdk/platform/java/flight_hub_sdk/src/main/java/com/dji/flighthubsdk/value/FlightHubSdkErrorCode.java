// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/**
 * Flight Hub SDK 层错误码。
 *
 * <p>表示初始化、配置、调用顺序等场景下的结果；各常量的整型取值与 SDK 实际返回的编码一致。
 * 若在其它 API 中见到同名错误码类型，其整数含义与本枚举 {@link #getValue()} 对齐。</p>
 *
 * <p>建议在业务与文档中优先以本枚举理解含义；需要与底层返回的整型互转时，可使用 {@link #fromValue(int)} 与 {@link #getValue()}。</p>
 *
 * @see com.dji.flighthubsdk.FlightHubSDK
 * @see com.dji.flighthubsdk.core.FlightHubSdkManager
 */
public enum FlightHubSdkErrorCode {

    /** 成功 */
    SUCCESS(0),
    /** 初始化失败。如直播通道建立失败、拉流初始化失败 */
    ERROR_INIT_FAILED(-1),
    /** 重复初始化。SDK 已 initialize，再次调用 initialize/setWorkspace/setUser 时返回 */
    ERROR_ALREADY_INITIALIZED(-2),
    /** 未初始化。SDK 尚未 initialize 即调用业务接口时返回 */
    ERROR_NOT_INITIALIZED(-3),
    /** 参数校验失败。必填参数为空、格式不合法等 */
    ERROR_INVALID_PARAM(-4),
    /** 操作失败。HTTP 请求异常、DRC 指令执行异常等通用失败，具体原因见 server_code / server_message */
    ERROR_OPERATION_FAILED(-5),
    /** 状态不允许。如载荷管理器未就绪、DRC 通道未建立时调用设备控制接口 */
    ERROR_INVALID_STATE(-6),
    /** 文件未找到。上传航线/日志/解禁证书时本地文件不存在 */
    FILE_NOT_FOUND(-7),
    /** 文件超限。上传文件大小超过允许上限 */
    FILE_TOO_LARGE(-8),
    /** 不支持的存储提供商。上传文件时对象存储类型不在支持范围内 */
    UNSUPPORTED_PROVIDER(-9),
    /** 上传失败。文件已找到但上传过程出错 */
    UPLOAD_FAILED(-10),
    /** 航线 ID 未找到。HTTP 响应中未携带预期的航线 ID */
    WAYLINE_ID_NOT_FOUND(-11),
    /** DRC 链路异常。DRC 通道不存在或数据传输失败，常见于指点飞行、设备控制、PSDK 控制等场景 */
    ERROR_DRC_LINK(-12),
    /** 未知或未在表中定义的取值 */
    UNKNOWN(65535);

    private final int value;

    FlightHubSdkErrorCode(int value) {
        this.value = value;
    }

    /** 与 SDK 返回的错误码整型编码一致。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #UNKNOWN}。
     *
     * @param value 与 SDK 返回的错误码整型编码一致的原始值
     * @return 对应枚举常量，未知时 {@link #UNKNOWN}
     */
    public static FlightHubSdkErrorCode fromValue(int value) {
        for (FlightHubSdkErrorCode c : FlightHubSdkErrorCode.values()) {
            if (c.value == value) {
                return c;
            }
        }
        return UNKNOWN;
    }
}
