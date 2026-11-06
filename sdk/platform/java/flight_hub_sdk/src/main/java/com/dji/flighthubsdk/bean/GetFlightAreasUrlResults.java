// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 获取飞行区文件下载地址结果。 */
public final class GetFlightAreasUrlResults {
    private String name;
    private String url;
    private String checksum;
    private long size;
    public GetFlightAreasUrlResults() {
    }

/**
     * 构造 GetFlightAreasUrlResults。
     *
     * @param name 文件名
     * @param url 签名下载地址（有效期 3600 秒）
     * @param checksum 文件内容 SHA-256，十六进制
     * @param size 文件大小（字节）
     */
    public GetFlightAreasUrlResults(String name, String url, String checksum, long size) {
        this.name = name;
        this.url = url;
        this.checksum = checksum;
        this.size = size;
    }

    /** 文件名 */
    public String getName() { return name; }
    /** 签名下载地址（有效期 3600 秒） */
    public String getUrl() { return url; }
    /** 文件内容 SHA-256，十六进制 */
    public String getChecksum() { return checksum; }
    /** 文件大小（字节） */
    public long getSize() { return size; }

    /** 文件名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 签名下载地址（有效期 3600 秒） */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 文件内容 SHA-256，十六进制 */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /** 文件大小（字节） */
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetFlightAreasUrlResults that = (GetFlightAreasUrlResults) o;
        return size == that.size
                && Objects.equals(name, that.name)
                && Objects.equals(url, that.url)
                && Objects.equals(checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, checksum, size);
    }

    @Override
    public String toString() {
        return "GetFlightAreasUrlResults{name=" + name + ", url=" + url
                + ", checksum=" + checksum + ", size=" + size + '}';
    }
}
