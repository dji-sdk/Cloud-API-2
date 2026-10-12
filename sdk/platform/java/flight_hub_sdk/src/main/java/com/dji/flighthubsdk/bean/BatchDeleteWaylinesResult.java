// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 批量删除航线返回结果 */
public final class BatchDeleteWaylinesResult {

    private int serverCode;
    private String serverMessage;
    public BatchDeleteWaylinesResult() {
    }

/**
     * 构造 BatchDeleteWaylinesResult。
     *
     * @param serverCode 服务端返回码
     * @param serverMessage 服务端返回消息
     */
    public BatchDeleteWaylinesResult(int serverCode, String serverMessage) {
        this.serverCode = serverCode;
        this.serverMessage = serverMessage != null ? serverMessage : "";
    }

    /** 服务端返回码 */
    public int getServerCode() {
        return serverCode;
    }

    /** 服务端返回码 */
    public void setServerCode(int serverCode) {
        this.serverCode = serverCode;
    }

    /** 服务端返回消息 */
    public String getServerMessage() {
        return serverMessage;
    }

    /** 服务端返回消息 */
    public void setServerMessage(String serverMessage) {
        this.serverMessage = serverMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchDeleteWaylinesResult that = (BatchDeleteWaylinesResult) o;
        return serverCode == that.serverCode
                && Objects.equals(serverMessage, that.serverMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverCode, serverMessage);
    }

    @Override
    public String toString() {
        return "BatchDeleteWaylinesResult{serverCode=" + serverCode + ", serverMessage='" + serverMessage + '\'' + '}';
    }
}
