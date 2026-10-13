// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 文件上传成功返回结果 */
public final class UploadFileResult {
    private String objectUrl;
    private String etag;
    private String objectKey;
    public UploadFileResult() {
    }

/**
     * 构造 UploadFileResult。
     *
     * @param objectUrl 对象URL
     * @param etag ETag
     * @param objectKey 对象存储Key
     */
    public UploadFileResult(String objectUrl, String etag, String objectKey) {
        this.objectUrl = objectUrl != null ? objectUrl : "";
        this.etag = etag != null ? etag : "";
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** 对象URL */
    public String getObjectUrl() { return objectUrl; }
    /** ETag */
    public String getEtag() { return etag; }
    /** 对象存储Key */
    public String getObjectKey() { return objectKey; }

    /** 对象URL */
    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    /** ETag */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /** 对象存储Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadFileResult that = (UploadFileResult) o;
        return Objects.equals(objectUrl, that.objectUrl)
                && Objects.equals(etag, that.etag)
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectUrl, etag, objectKey);
    }

    @Override
    public String toString() {
        return "UploadFileResult{objectUrl='" + objectUrl + '\'' + ", etag='" + etag + '\'' + ", objectKey='" + objectKey + '\'' + '}';
    }
}
