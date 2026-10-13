// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.fhsdk;

import com.dji.fhsdk.demo.config.FhsdkProperties;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * FHSDK 单例初始化（懒加载，首次 POST /session/init 时触发）。
 * 平台连接参数见 application.yml / 环境变量。
 */
@Component
public class FhsdkBootstrap {

    private static final Logger log = LoggerFactory.getLogger(FhsdkBootstrap.class);

    private final FhsdkProperties properties;
    private volatile boolean initialized;

    public FhsdkBootstrap(FhsdkProperties properties) {
        this.properties = properties;
    }

    public synchronized void ensureInitialized() {
        if (initialized && FlightHubSDK.isInitialized()) {
            return;
        }
        JniLibraryLoader.loadLibrary();

        FlightHubSdkConfig sdkConfig = new FlightHubSdkConfig();
        sdkConfig.setAddress(properties.getAddress());
        sdkConfig.setPort(properties.getPort());
        sdkConfig.setNotificationServerAddress(properties.getNotificationServer());
        sdkConfig.setLogLevel(properties.getLogLevel());
        sdkConfig.setLogToConsole(properties.isLogToConsole());
        sdkConfig.setLogFilePath(properties.getLogFilePath());
        sdkConfig.setLogMaxFileSize(4 * 1024 * 1024);
        sdkConfig.setLogMaxFiles(100);

        String drtcConfigPath = resolveDrtcConfigPath();
        if (drtcConfigPath != null) {
            sdkConfig.setDrtcConfigPath(drtcConfigPath);
            log.info("Using DRTC config path: {}", drtcConfigPath);
        } else {
            log.warn("DRTC config path not found (checked env FHSDK_DRTC_CONFIG, property fhsdk.drtc-config-path, and ./drtc_config_node.json). DRTC live/DRC may fail.");
        }

        List<HeaderKeyValue> headers = new ArrayList<>();
        String authKey = properties.getAuthHeaderKey();
        String authValue = properties.getAuthHeaderValue();
        if (authKey != null && !authKey.isBlank() && authValue != null && !authValue.isBlank()) {
            headers.add(new HeaderKeyValue(authKey.trim(), authValue.trim()));
            FlightHubSDK.setWebSocketAuthHeaders(headers);
            FlightHubSDK.setHttpHeaders(headers);
        } else {
            log.warn("fhsdk.auth-header-key/value not set; proceeding without custom auth headers");
        }

        FlightHubSdkErrorCode result = FlightHubSDK.initialize(sdkConfig);
        if (result != FlightHubSdkErrorCode.SUCCESS) {
            throw new IllegalStateException("FHSDK initialize failed: " + result);
        }
        initialized = true;
        log.info("FHSDK initialized, version={}", FlightHubSDK.getVersion());
    }

    public boolean isInitialized() {
        return initialized && FlightHubSDK.isInitialized();
    }

    private String resolveDrtcConfigPath() {
        String fromEnv = System.getenv("FHSDK_DRTC_CONFIG");
        if (isExistingFile(fromEnv)) return Paths.get(fromEnv).toAbsolutePath().normalize().toString();
        String fromProperty = properties.getDrtcConfigPath();
        if (isExistingFile(fromProperty)) return Paths.get(fromProperty).toAbsolutePath().normalize().toString();
        Path localDefault = Paths.get("drtc_config_node.json").toAbsolutePath().normalize();
        return Files.isRegularFile(localDefault) ? localDefault.toString() : null;
    }

    private boolean isExistingFile(String path) {
        if (path == null || path.isBlank()) return false;
        return Files.isRegularFile(Paths.get(path.trim()).toAbsolutePath().normalize());
    }
}
