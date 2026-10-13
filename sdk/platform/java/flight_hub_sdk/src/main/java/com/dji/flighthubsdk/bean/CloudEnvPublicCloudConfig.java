// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 司空2 公有云配置（CloudEnvTargetConfig.type == 0 时使用）。 */
public class CloudEnvPublicCloudConfig {
    private String bindCode = "";
    private String orgId = "";
    private String zoneId = "";

    public CloudEnvPublicCloudConfig() {
    }

    /** 绑定码 */
    public String getBindCode() {
        return bindCode;
    }

    /** 绑定码 */
    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
    }

    /** 组织 ID */
    public String getOrgId() {
        return orgId;
    }

    /** 组织 ID */
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /** 区域 ID */
    public String getZoneId() {
        return zoneId;
    }

    /** 区域 ID */
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvPublicCloudConfig that = (CloudEnvPublicCloudConfig) o;
        return Objects.equals(bindCode, that.bindCode)
                && Objects.equals(orgId, that.orgId)
                && Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindCode, orgId, zoneId);
    }

    @Override
    public String toString() {
        return "CloudEnvPublicCloudConfig{bindCode='" + bindCode + '\'' + ", orgId='" + orgId + '\'' + ", zoneId='" + zoneId + '\'' + '}';
    }
}
