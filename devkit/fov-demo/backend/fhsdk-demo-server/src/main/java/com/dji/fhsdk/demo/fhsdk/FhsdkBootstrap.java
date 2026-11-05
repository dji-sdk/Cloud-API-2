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

    /** SDK 日志单文件上限（字节） */
    private static final long LOG_MAX_FILE_SIZE_BYTES = 4L * 1024 * 1024;

    /** SDK 日志文件轮转保留个数 */
    private static final int LOG_MAX_FILES = 100;

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

        FlightHubSdkConfig sdkConfig = buildSdkConfig();

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

    /**
     * 构建 SDK 初始化配置（含 WSS 证书校验透传）。
     *
     * <p>包级可见，便于单元测试断言配置内容，避免触发 JNI 加载。
     *
     * @return SDK 初始化配置
     */
    FlightHubSdkConfig buildSdkConfig() {
        FlightHubSdkConfig sdkConfig = new FlightHubSdkConfig();
        sdkConfig.setAddress(properties.getAddress());
        sdkConfig.setPort(properties.getPort());
        sdkConfig.setNotificationServerAddress(properties.getNotificationServer());
        sdkConfig.setLogLevel(properties.getLogLevel());
        sdkConfig.setLogToConsole(properties.isLogToConsole());
        sdkConfig.setLogFilePath(properties.getLogFilePath());
        sdkConfig.setLogMaxFileSize(LOG_MAX_FILE_SIZE_BYTES);
        sdkConfig.setLogMaxFiles(LOG_MAX_FILES);
        configureTls(sdkConfig);

        String drtcConfigPath = resolveDrtcConfigPath();
        if (drtcConfigPath != null) {
            sdkConfig.setDrtcConfigPath(drtcConfigPath);
            log.info("Using DRTC config path: {}", drtcConfigPath);
        } else {
            log.warn("DRTC config path not found (checked env FHSDK_DRTC_CONFIG, property fhsdk.drtc-config-path, and ./drtc_config_node.json). DRTC live/DRC may fail.");
        }
        return sdkConfig;
    }

    /**
     * 透传 WSS 证书校验配置到 SDK。
     *
     * <p>与 skeleton {@code RealSdkStarter#configureTls} 语义一致：{@code verify-ssl}
     * 未配置时按 {@code true} 处理（安全默认），{@code ssl-ca-path} 为空则不设置。
     *
     * @param sdkConfig SDK 配置
     */
    private void configureTls(FlightHubSdkConfig sdkConfig) {
        Boolean configured = properties.getVerifySsl();
        boolean verifySsl = !Boolean.FALSE.equals(configured);
        String caPath = properties.getSslCaPath();
        boolean hasCaPath = caPath != null && !caPath.isBlank();

        sdkConfig.setVerifySsl(verifySsl);
        if (hasCaPath) {
            sdkConfig.setSslCaPath(caPath);
        }

        if (!verifySsl) {
            log.warn("SDK WSS 证书校验：已关闭（verify-ssl=false），仅建议用于内网私有化自签名部署{}",
                    hasCaPath ? "；ssl-ca-path 在校验关闭时不生效" : "");
        } else if (hasCaPath) {
            log.info("SDK WSS 证书校验：开启，CA 路径={}", caPath);
        } else {
            log.info("SDK WSS 证书校验：开启，使用 SDK 默认信任库");
        }
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
