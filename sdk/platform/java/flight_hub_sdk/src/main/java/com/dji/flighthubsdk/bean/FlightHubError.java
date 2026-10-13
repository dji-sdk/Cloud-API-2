// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

import java.util.Objects;
/**
 * FHSDK 错误信息（分层错误码）。
 *
 * <p>errorCode 表示 SDK 层错误；httpCode 为 HTTP 状态码（0 表示 curl 层失败）；
 * serverCode 和 serverMessage 为后端业务层错误码和描述。
 */
public final class FlightHubError {

    private FlightHubSdkErrorCode errorCode;
    private int httpCode;
    private int serverCode;
    private int deviceCode;
    private String serverMessage;
    public FlightHubError() {
    }

/**
     * 构造 FlightHubError。
     *
     * @param errorCode SDK 层错误码
     * @param httpCode HTTP 状态码（0 表示 curl 层失败）
     * @param serverCode 后端业务层错误码
     * @param deviceCode 设备错误码
     * @param serverMessage 后端业务层错误描述
     */
    public FlightHubError(FlightHubSdkErrorCode errorCode, int httpCode, int serverCode, int deviceCode, String serverMessage) {
        this.errorCode = errorCode;
        this.httpCode = httpCode;
        this.serverCode = serverCode;
        this.deviceCode = deviceCode;
        this.serverMessage = serverMessage != null ? serverMessage : "";
    }

    /**
     * 构造 FlightHubError。
     *
     * @param errorCode SDK 层错误码
     * @param httpCode HTTP 状态码（0 表示 curl 层失败）
     * @param serverCode 后端业务层错误码
     * @param serverMessage 后端业务层错误描述
     */
    public FlightHubError(FlightHubSdkErrorCode errorCode, int httpCode, int serverCode, String serverMessage) {
        this(errorCode, httpCode, serverCode, 0, serverMessage);
    }

    /**
     * 构造 FlightHubError。
     *
     * @param errorCode SDK 层错误码
     */
    public FlightHubError(FlightHubSdkErrorCode errorCode) {
        this(errorCode, 0, 0, 0, "");
    }

    /** SDK 层错误码 */
    public FlightHubSdkErrorCode getErrorCode() { return errorCode; }
    /** HTTP 状态码（0 表示 curl 层失败） */
    public int getHttpCode() { return httpCode; }
    /** 后端业务层错误码 */
    public int getServerCode() { return serverCode; }
    /** 设备错误码 */
    public int getDeviceCode() { return deviceCode; }
    /** 后端业务层错误描述 */
    public String getServerMessage() { return serverMessage; }
    /** 是否成功 */
    public boolean isSuccess() {
        return errorCode == FlightHubSdkErrorCode.SUCCESS;
    }

    /** SDK 层错误码 */
    public void setErrorCode(FlightHubSdkErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /** HTTP 状态码（0 表示 curl 层失败） */
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    /** 后端业务层错误码 */
    public void setServerCode(int serverCode) {
        this.serverCode = serverCode;
    }

    /** 设备错误码 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 后端业务层错误描述 */
    public void setServerMessage(String serverMessage) {
        this.serverMessage = serverMessage;
    }



    @Override
    public String toString() {
        return "FlightHubError{errorCode=" + errorCode
                + ", httpCode=" + httpCode
                + ", serverCode=" + serverCode
                + ", deviceCode=" + deviceCode
                + ", serverMessage='" + serverMessage + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightHubError that = (FlightHubError) o;
        return Objects.equals(errorCode, that.errorCode)
                && httpCode == that.httpCode
                && serverCode == that.serverCode
                && deviceCode == that.deviceCode
                && Objects.equals(serverMessage, that.serverMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                errorCode,
                httpCode,
                serverCode,
                deviceCode,
                serverMessage);
    }
}
