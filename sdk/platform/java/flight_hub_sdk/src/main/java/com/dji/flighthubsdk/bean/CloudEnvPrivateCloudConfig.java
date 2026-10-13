// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 司空2 私有云配置（CloudEnvTargetConfig.type == 2 时使用）。 */
public class CloudEnvPrivateCloudConfig {
    private String bindCode = "";
    private String orgId = "";
    private String url = "";

    public CloudEnvPrivateCloudConfig() {
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

    /** 私有云服务 URL */
    public String getUrl() {
        return url;
    }

    /** 私有云服务 URL */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvPrivateCloudConfig that = (CloudEnvPrivateCloudConfig) o;
        return Objects.equals(bindCode, that.bindCode)
                && Objects.equals(orgId, that.orgId)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindCode, orgId, url);
    }

    @Override
    public String toString() {
        return "CloudEnvPrivateCloudConfig{bindCode='" + bindCode + '\'' + ", orgId='" + orgId + '\'' + ", url='" + url + '\'' + '}';
    }
}
