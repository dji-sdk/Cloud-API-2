// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 跨节点通道消息事件（对齐 v1.1 §跨节点通道消息 topic {@code topic_cascade_notify}）。
 *
 * <p>对端节点通过 {@code POST /cascade/api/notify/{dstNode}} 发过来的原始 payload；
 * SDK 不解析（任意字节透传，与 {@code postToNode} 的 body 语义对称）。
 * 是否 base64 / 是否携带元信息由业务方约定。
 */
public final class CascadeNotifyEvent {
    /** 事件 topic 常量。 */
    public static final String TOPIC = "topic_cascade_notify";

    private String payload;

    public CascadeNotifyEvent() {
        this("");
    }

    /**
     * 构造 CascadeNotifyEvent。
     *
     * @param payload 发送侧 postToNode 时透传的原始 body（SDK 不解析）
     */
    public CascadeNotifyEvent(String payload) {
        this.payload = payload != null ? payload : "";
    }

    /** 原始 payload（与 postToNode 的 body 语义对称） */
    public String getPayload() { return payload; }
    /** 原始 payload（与 postToNode 的 body 语义对称） */
    public void setPayload(String payload) { this.payload = payload != null ? payload : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeNotifyEvent that = (CascadeNotifyEvent) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    @Override
    public String toString() {
        return "CascadeNotifyEvent{payload_len=" + payload.length() + "}";
    }
}