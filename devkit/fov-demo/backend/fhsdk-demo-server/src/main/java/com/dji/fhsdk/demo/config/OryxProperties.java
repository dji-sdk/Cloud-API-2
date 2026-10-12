// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oryx")
public class OryxProperties {

    /** WHIP 推流 URL 模板，{streamKey} 占位 */
    private String whipUrlTemplate;
    /** WHEP 播放 URL 模板，{streamKey} 占位 */
    private String whepUrlTemplate;
    /** Oryx 全局推流密钥，WHIP URL 附加 secret=xxx */
    private String publishSecret;

    public String getWhipUrlTemplate() { return whipUrlTemplate; }
    public void setWhipUrlTemplate(String whipUrlTemplate) { this.whipUrlTemplate = whipUrlTemplate; }
    public String getWhepUrlTemplate() { return whepUrlTemplate; }
    public void setWhepUrlTemplate(String whepUrlTemplate) { this.whepUrlTemplate = whepUrlTemplate; }
    public String getPublishSecret() { return publishSecret; }
    public void setPublishSecret(String publishSecret) { this.publishSecret = publishSecret; }
}
