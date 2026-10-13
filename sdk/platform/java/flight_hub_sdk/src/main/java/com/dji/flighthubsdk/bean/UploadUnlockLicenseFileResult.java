// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 上传解锁证书文件结果。 */
public final class UploadUnlockLicenseFileResult {

    /** 存储目录，如 "unlock_license"，用于拼接 file_key 调用 checkUnlockLicense */
    private String fileStoreDir;
    public UploadUnlockLicenseFileResult() {
    }

/**
     * 构造 UploadUnlockLicenseFileResult。
     *
     * @param fileStoreDir 文件存储目录
     */
    public UploadUnlockLicenseFileResult(String fileStoreDir) {
        this.fileStoreDir = fileStoreDir != null ? fileStoreDir : "";
    }

    /** 存储目录，如 "unlock_license"，用于拼接 file_key 调用 checkUnlockLicense */
    public String getFileStoreDir() { return fileStoreDir; }

    /** @param fileStoreDir 存储目录，如 "unlock_license"，用于拼接 file_key 调用 checkUnlockLicense */
    public void setFileStoreDir(String fileStoreDir) {
        this.fileStoreDir = fileStoreDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadUnlockLicenseFileResult that = (UploadUnlockLicenseFileResult) o;
        return Objects.equals(fileStoreDir, that.fileStoreDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileStoreDir);
    }

    @Override
    public String toString() {
        return "UploadUnlockLicenseFileResult{fileStoreDir='" + fileStoreDir + '\'' + '}';
    }
}
