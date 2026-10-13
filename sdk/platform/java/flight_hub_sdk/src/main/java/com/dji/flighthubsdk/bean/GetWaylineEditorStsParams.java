// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 获取航线编辑器 STS 凭证请求参数 */
public final class GetWaylineEditorStsParams {

    /** 对象存储路径前缀，必填 */
    private String objectKeyPrefix;
    /** 凭证有效期（秒），默认 3600（proto int64，实际值不超过 int 范围） */
    private int expire;

    public GetWaylineEditorStsParams() {
        this.expire = 3600;
    }

    /**
     * 构造 GetWaylineEditorStsParams。
     *
     * @param objectKeyPrefix 对象存储路径前缀，必填
     */
    public GetWaylineEditorStsParams(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix != null ? objectKeyPrefix : "";
        this.expire = 3600;
    }

    /** 对象存储路径前缀，必填 */
    public String getObjectKeyPrefix() { return objectKeyPrefix; }

    /** 对象存储路径前缀，必填 */
    public void setObjectKeyPrefix(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix;
    }

    /** 凭证有效期（秒），默认 3600（proto int64，实际值不超过 int 范围） */
    public int getExpire() { return expire; }

    /** @param expire 凭证有效期（秒），默认 3600（proto int64，实际值不超过 int 范围） */
    public void setExpire(int expire) {
        this.expire = expire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetWaylineEditorStsParams that = (GetWaylineEditorStsParams) o;
        return expire == that.expire && Objects.equals(objectKeyPrefix, that.objectKeyPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKeyPrefix, expire);
    }

    @Override
    public String toString() {
        return "GetWaylineEditorStsParams{"
                + "objectKeyPrefix='" + objectKeyPrefix + '\''
                + ", expire=" + expire
                + '}';
    }
}
