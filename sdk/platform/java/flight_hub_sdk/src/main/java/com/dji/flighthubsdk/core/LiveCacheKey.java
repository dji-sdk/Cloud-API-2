// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.bean.LiveInfo;

/**
 * LiveModule / LiveRecordModule 缓存键：payload_index + ":" + video_id。
 * <p>同一 payloadIndex 下可能有多路 video（如 normal-0 / zoom-0），缓存必须按 video 区分。
 */
final class LiveCacheKey {

    private LiveCacheKey() {
    }

    static String of(LiveInfo params) {
        if (params == null) {
            return "";
        }
        String payload = params.getPayloadIndex() != null ? params.getPayloadIndex() : "";
        String video = params.getVideoId() != null ? params.getVideoId() : "";
        return payload + ":" + video;
    }
}
