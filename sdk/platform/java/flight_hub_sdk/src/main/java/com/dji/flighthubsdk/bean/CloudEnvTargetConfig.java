// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 目标云环境配置。
 * type: 云环境类型，0=司空2 公有云, 1=三方云(API1), 2=司空2 私有云, 3=三方云(API2)
 */
public class CloudEnvTargetConfig {
    private int type = 0;
    private CloudEnvPublicCloudConfig publicCloudConfig = new CloudEnvPublicCloudConfig();
    private CloudEnvPrivateCloudConfig privateCloudConfig = new CloudEnvPrivateCloudConfig();
    private CloudEnvThirdCloudConfig thirdCloudConfig = new CloudEnvThirdCloudConfig();

    public CloudEnvTargetConfig() {
    }

    /** 云环境类型，0=司空2 公有云, 1=三方云(API1), 2=司空2 私有云, 3=三方云(API2) */
    public int getType() {
        return type;
    }

    /** 云环境类型，0=司空2 公有云, 1=三方云(API1), 2=司空2 私有云, 3=三方云(API2) */
    public void setType(int type) {
        this.type = type;
    }

    /** 司空2 公有云配置（type == 0 时使用） */
    public CloudEnvPublicCloudConfig getPublicCloudConfig() {
        return publicCloudConfig;
    }

    /** @param publicCloudConfig 司空2 公有云配置（type == 0 时使用） */
    public void setPublicCloudConfig(CloudEnvPublicCloudConfig publicCloudConfig) {
        this.publicCloudConfig = publicCloudConfig != null ? publicCloudConfig : new CloudEnvPublicCloudConfig();
    }

    /** 司空2 私有云配置（type == 2 时使用） */
    public CloudEnvPrivateCloudConfig getPrivateCloudConfig() {
        return privateCloudConfig;
    }

    /** @param privateCloudConfig 司空2 私有云配置（type == 2 时使用） */
    public void setPrivateCloudConfig(CloudEnvPrivateCloudConfig privateCloudConfig) {
        this.privateCloudConfig = privateCloudConfig != null ? privateCloudConfig : new CloudEnvPrivateCloudConfig();
    }

    /** 三方云(API1) 配置（type == 1 时使用） */
    public CloudEnvThirdCloudConfig getThirdCloudConfig() {
        return thirdCloudConfig;
    }

    /** @param thirdCloudConfig 三方云(API1) 配置（type == 1 时使用） */
    public void setThirdCloudConfig(CloudEnvThirdCloudConfig thirdCloudConfig) {
        this.thirdCloudConfig = thirdCloudConfig != null ? thirdCloudConfig : new CloudEnvThirdCloudConfig();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvTargetConfig that = (CloudEnvTargetConfig) o;
        return type == that.type
                && Objects.equals(publicCloudConfig, that.publicCloudConfig)
                && Objects.equals(privateCloudConfig, that.privateCloudConfig)
                && Objects.equals(thirdCloudConfig, that.thirdCloudConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, publicCloudConfig, privateCloudConfig, thirdCloudConfig);
    }

    @Override
    public String toString() {
        return "CloudEnvTargetConfig{type=" + type + ", publicCloudConfig=" + publicCloudConfig
                + ", privateCloudConfig=" + privateCloudConfig + ", thirdCloudConfig=" + thirdCloudConfig + '}';
    }
}
