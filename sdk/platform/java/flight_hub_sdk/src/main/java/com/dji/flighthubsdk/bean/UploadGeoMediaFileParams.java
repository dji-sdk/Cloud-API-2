// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 上传地理标注媒体资源（图片备注）请求参数 */
public final class UploadGeoMediaFileParams {

    /** 文件名称，必填 */
    private String name;
    /** 媒体资源在 OSS 中的 key（通过 STS 直传后获得），必填 */
    private String objectKey;
    /** 媒体资源指纹（md5），可选 */
    private String fingerprint;
    /** 媒体来源，可选 */
    private int mediaSource;

    public UploadGeoMediaFileParams() {
    }

    /**
     * 构造 UploadGeoMediaFileParams。
     *
     * @param name      文件名称，必填
     * @param objectKey 媒体资源在 OSS 中的 key（通过 STS 直传后获得），必填
     */
    public UploadGeoMediaFileParams(String name, String objectKey) {
        this.name = name != null ? name : "";
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** 文件名称，必填 */
    public String getName() { return name; }

    /** 文件名称，必填 */
    public void setName(String name) {
        this.name = name;
    }

    /** 媒体资源在 OSS 中的 key（通过 STS 直传后获得），必填 */
    public String getObjectKey() { return objectKey; }

    /** @param objectKey 媒体资源在 OSS 中的 key（通过 STS 直传后获得），必填 */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 媒体资源指纹（md5），可选 */
    public String getFingerprint() { return fingerprint; }

    /** 媒体资源指纹（md5），可选 */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /** 媒体来源，可选 */
    public int getMediaSource() { return mediaSource; }

    /** 媒体来源，可选 */
    public void setMediaSource(int mediaSource) {
        this.mediaSource = mediaSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadGeoMediaFileParams that = (UploadGeoMediaFileParams) o;
        return Objects.equals(name, that.name) && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, objectKey);
    }

    @Override
    public String toString() {
        return "UploadGeoMediaFileParams{"
                + "name='" + name + '\''
                + ", objectKey='" + objectKey + '\''
                + '}';
    }
}
