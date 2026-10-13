// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 模型文件下载 URL 返回结果 */
public final class TerraBuildModelFileUrlResult {
    private String url;
    public TerraBuildModelFileUrlResult() {
    }

/**
     * 构造 TerraBuildModelFileUrlResult。
     *
     * @param url 文件下载地址
     */
    public TerraBuildModelFileUrlResult(String url) {
        this.url = url != null ? url : "";
    }

    /** 文件下载地址 */
    public String getUrl() { return url; }

    /** 文件下载地址 */
    public void setUrl(String url) {
        this.url = url;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(url, ((TerraBuildModelFileUrlResult) o).url);
    }
    @Override public int hashCode() { return Objects.hash(url); }

    @Override
    public String toString() {
        return "TerraBuildModelFileUrlResult{url='" + url + '\'' + '}';
    }
}
