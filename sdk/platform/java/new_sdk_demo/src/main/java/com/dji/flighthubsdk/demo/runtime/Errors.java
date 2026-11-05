// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.bean.FlightHubError;

/**
 * {@link FlightHubError} 的<b>唯一</b>格式化与校验实现。
 *
 * <p>SDK 的错误对象有四个诊断字段 ({@code errorCode} 传输层 / {@code serverCode} 服务端业务码 /
 * {@code deviceCode} 设备固件码 / {@code serverMessage} 服务端文案), 漏掉任一个都会让排查退化成
 * 「不透明失败」——这正是旧 demo 把设备错误码压平成布尔值的老问题。因此字段拼装只允许有一份实现。
 *
 * <p>本类出现前, 同一段字段拼接在 {@code ModuleDemo.requireSuccess}、{@code ModuleDemo.describeError}、
 * {@code Precondition.requireSuccess}、{@code Devices.describe} 里各写了一份, 且已经漂移:
 * {@code Devices.describe} 不做 null 判空 (其调用点 {@code logDeviceList} 恰恰是「回调错误」路径,
 * 传 null 会以 NPE 掩盖真实错误), {@code Precondition} 的副本靠一行注释声称「与 ModuleDemo 对齐」——
 * 注释保证不了对齐。现在四处全部委托到这里。
 *
 * <p>保留两种输出口径 (差异是刻意的, 不是漂移):
 * <ul>
 *   <li>{@link #describe} 空格分隔、只有错误字段——供<b>异常消息</b>拼接, 调用点已给出「谁失败了」的上下文;
 *   <li>{@link #detail} 逗号分隔、带 {@code success=}、{@code msg} 加引号——供<b>回调日志</b>「不论成败
 *       都原样落一行」使用, 逗号与引号让含空格的服务端文案边界一眼可辨。
 * </ul>
 * 两者的字段名 (code/serverCode/deviceCode/msg)保持一致, 以便按同一套字段名跨日志检索。
 */
public final class Errors {

    private Errors() {}

    /**
     * 错误详情 (空格分隔, null 安全): {@code code=.. serverCode=.. deviceCode=.. msg=..}。
     *
     * <p>供异常消息与错误日志拼接; {@code null} 返回 {@code "null error"} 而不是抛 NPE——
     * 格式化错误信息的过程本身绝不该再制造一个新异常, 把原始故障现场盖掉。
     */
    public static String describe(FlightHubError e) {
        if (e == null) {
            return "null error";
        }
        return "code=" + e.getErrorCode()
                + " serverCode=" + e.getServerCode()
                + " deviceCode=" + e.getDeviceCode()
                + " msg=" + e.getServerMessage();
    }

    /**
     * 完整状态 (逗号分隔, 含 {@code success=}, {@code msg} 加引号, null 安全):
     * {@code success=.., code=.., serverCode=.., deviceCode=.., msg=".."}。
     *
     * <p>供回调处「不论成败都落一行」使用; {@code null} 返回 {@code "null"}。
     */
    public static String detail(FlightHubError e) {
        if (e == null) {
            return "null";
        }
        return "success=" + e.isSuccess()
                + ", code=" + e.getErrorCode()
                + ", serverCode=" + e.getServerCode()
                + ", deviceCode=" + e.getDeviceCode()
                + ", msg=" + "\"" + e.getServerMessage() + "\"";
    }

    /** 错误对象是否为「已到达且成功」(null 视为不成功——回调没给 error 不能当成功)。 */
    public static boolean isSuccess(FlightHubError e) {
        return e != null && e.isSuccess();
    }

    /**
     * 校验错误码, 失败即抛 {@link FlightHubException}。
     *
     * <p>区分两类失败: {@code null}(回调没给 error, 多为 SDK/回调契约问题)与业务失败 (错误码非成功),
     * 消息分开写, 避免排查时把两者混为一谈。
     */
    public static void require(String label, FlightHubError e) {
        if (e == null) {
            throw new FlightHubException(label + " 回调返回 null error");
        }
        if (!e.isSuccess()) {
            throw new FlightHubException(label + " 失败: " + describe(e));
        }
    }
}
