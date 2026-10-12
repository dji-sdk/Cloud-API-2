// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.fhsdk.FhsdkBootstrap;
import org.springframework.stereotype.Service;

/** 浏览器 HttpSession 与 FHSDK 就绪标记（每个标签页一次 init） */
@Service
public class SessionService {

    private final FhsdkBootstrap bootstrap;
    private final SessionRegistry registry;

    public SessionService(FhsdkBootstrap bootstrap, SessionRegistry registry) {
        this.bootstrap = bootstrap;
        this.registry = registry;
    }

    public void initSession(String httpSessionId) {
        bootstrap.ensureInitialized();
        registry.markReady(httpSessionId);
    }

    public boolean isReady(String httpSessionId) {
        return registry.isReady(httpSessionId);
    }
}
