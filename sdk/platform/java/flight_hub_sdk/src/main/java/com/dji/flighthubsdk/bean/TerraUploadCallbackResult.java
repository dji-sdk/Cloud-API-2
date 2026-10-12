// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 模型上传回调响应结果
 * 对应接口: POST /pf-terra/api/v1/models/files/upload-callback
 */
public final class TerraUploadCallbackResult {

    /** 新创建的 File 记录 ID */
    private long id;
    /** 新创建的 File 记录 UUID */
    private String uuid;
    /** 文件名（原样返回） */
    private String name;

    public TerraUploadCallbackResult() {
    }

    public TerraUploadCallbackResult(long id, String uuid, String name) {
        this.id = id;
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraUploadCallbackResult that = (TerraUploadCallbackResult) o;
        return id == that.id && Objects.equals(uuid, that.uuid) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(id, uuid, name); }

    @Override
    public String toString() {
        return "TerraUploadCallbackResult{id=" + id + ", uuid='" + uuid + "', name='" + name + "'}";
    }
}
