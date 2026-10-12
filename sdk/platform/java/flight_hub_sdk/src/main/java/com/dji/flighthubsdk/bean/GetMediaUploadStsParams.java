// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 获取媒体上传 STS 凭证的请求参数。
 */
public final class GetMediaUploadStsParams {
    private String filePath;
    private int expire;
    private String sn;
    private int module;
    public GetMediaUploadStsParams() {
    }

/**
     * 构造 GetMediaUploadStsParams。
     *
     * @param filePath 文件路径
     * @param expire 凭证有效期（秒）
     * @param sn 设备序列号
     * @param module 业务模块类型
     */
    public GetMediaUploadStsParams(String filePath, int expire, String sn, int module) {
        this.filePath = filePath != null ? filePath : "";
        this.expire = expire;
        this.sn = sn != null ? sn : "";
        this.module = module;
    }

    /** 文件路径 */
    public String getFilePath() { return filePath; }

    /** 凭证有效期（秒） */
    public int getExpire() { return expire; }

    /** 设备序列号 */
    public String getSn() { return sn; }

    /** 业务模块类型。枚举：2=飞行记录STS, 2001=SN加密, 其他(含0)=默认设备STS */
    public int getModule() { return module; }

    /** 文件路径 */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /** 凭证有效期（秒） */
    public void setExpire(int expire) {
        this.expire = expire;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 业务模块类型。枚举：2=飞行记录STS, 2001=SN加密, 其他(含0)=默认设备STS */
    public void setModule(int module) {
        this.module = module;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetMediaUploadStsParams that = (GetMediaUploadStsParams) o;
        return expire == that.expire
                && module == that.module
                && Objects.equals(filePath, that.filePath)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, expire, sn, module);
    }

    @Override
    public String toString() {
        return "GetMediaUploadStsParams{"
                + "filePath='" + filePath + '\''
                + ", expire=" + expire
                + ", sn='" + sn + '\''
                + ", module=" + module
                + '}';
    }
}
