// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.dji.fhsdk.demo.config.DemoProperties;
import com.dji.fhsdk.demo.config.FhsdkProperties;
import com.dji.fhsdk.demo.config.OryxProperties;
import com.dji.fhsdk.demo.config.LiveProperties;

@SpringBootApplication
@EnableConfigurationProperties({FhsdkProperties.class, OryxProperties.class, DemoProperties.class, LiveProperties.class})
public class FhsdkDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhsdkDemoApplication.class, args);
    }
}
