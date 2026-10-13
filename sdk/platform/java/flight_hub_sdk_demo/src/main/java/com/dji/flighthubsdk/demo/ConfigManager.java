// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置管理器
 * 负责加载和管理SDK的配置参数
 */
public class ConfigManager {

    /** 与 new_sdk_demo 一致：config.auth.header_value 为空时回退此环境变量 */
    public static final String ENV_AUTH_VALUE = "FLIGHTHUB_AUTH_VALUE";
    
    private static DemoConfig config;
    
    /**
     * 演示配置类
     */
    public static class DemoConfig {
        public ServerConfig server = new ServerConfig();
        public SdkConfig sdk = new SdkConfig();
        public DeviceConfig devices = new DeviceConfig();
        public AppConfig app = new AppConfig();
        public RtmpConfig rtmp = new RtmpConfig();
        public AuthConfig auth = new AuthConfig();

        // Terra Build 测试配置（从 config.json 的 terra_build 节点读取，缺省则用空字符串/0）
        public String terraBuildFileUuid = "633c1d91-35bd-4670-aed2-6f440b48d8e4";
        public long terraBuildFileId = 441;
        public String terraBuildFolderUuid = "4865ce88-56a6-43c2-a776-7a87413e4e26";
        // 模型上传回调测试配置：直传 OSS 后填入实际的 object_key 和文件名
        public String terraObjectKey = "test_terra/JavaSDKTest.las";
        public String terraModelFileName = "JavaSDKTest.las";

        public String getTerraBuildFileUuid() { return terraBuildFileUuid != null ? terraBuildFileUuid : ""; }
        public long getTerraBuildFileId() { return terraBuildFileId; }
        public String getTerraBuildFolderUuid() { return terraBuildFolderUuid != null ? terraBuildFolderUuid : ""; }
        public String getTerraObjectKey() { return terraObjectKey != null ? terraObjectKey : ""; }
        public String getTerraModelFileName() { return terraModelFileName != null ? terraModelFileName : ""; }

        // 图片备注接口测试配置（直传 OSS 后填入 object_key，以及上传回调返回的 file_id）
        public String mediaObjectKey = "204-paas/E5997759-B0F2-D64B-A95E-EFA40FB14D16.png";
        public long mediaFileId = 875;

        public String getMediaObjectKey() { return mediaObjectKey != null ? mediaObjectKey : ""; }
        public long getMediaFileId() { return mediaFileId; }

        @Override
        public String toString() {
            return String.format("DemoConfig{server=%s, sdk=%s, devices=%s, app=%s, auth=%s}",
                server, sdk, devices, app, auth);
        }
    }

    /**
     * Gateway 鉴权头（HTTP + WebSocket 共用）
     */
    public static class AuthConfig {
        public String header_key = "";
        public String header_value = "";

        @Override
        public String toString() {
            return String.format("AuthConfig{header_key='%s', header_value='%s'}",
                header_key, maskAuthValue(header_value));
        }
    }
    
    /**
     * 服务器配置
     */
    public static class ServerConfig {
        public String address;
        public String port;
        public String notification_server;
        /** WSS/HTTPS 证书验证开关（安全默认开启）；私有化自签名部署须配 ssl_ca_path 指向 CA 文件；显式置 false 会产生 WARN 日志，仅限受控测试环境 */
        public boolean verify_ssl = true;
        /** 私有 CA 证书文件路径(PEM); 为空时使用系统 CA 信任库 */
        public String ssl_ca_path = "";

        @Override
        public String toString() {
            return String.format("ServerConfig{address='%s', port='%s', notification_server='%s', verify_ssl=%s, ssl_ca_path='%s'}",
                address, port, notification_server, verify_ssl, ssl_ca_path);
        }
    }
    
    /**
     * SDK配置
     */
    public static class SdkConfig {
        public String log_level;
        public boolean log_to_console = true;  // 默认值为 true
        public String log_file_path = "flight_hub_sdk.log";  // 默认日志文件路径
        public long log_max_file_size = 4 * 1024 * 1024;     // 默认4MB
        public int log_max_files = 1000;                     // 默认1000个文件
        /** DRTC Native 配置文件路径；也可由环境变量 FHSDK_DRTC_CONFIG 覆盖 */
        public String drtc_config_path = "";
        
        @Override
        public String toString() {
            return String.format(
                "SdkConfig{log_level='%s', log_to_console=%s, log_file_path='%s', log_max_file_size=%d, log_max_files=%d, drtc_config_path='%s'}",
                log_level, log_to_console, log_file_path, log_max_file_size, log_max_files, drtc_config_path);
        }
    }
    
    /**
     * 设备配置
     */
    public static class DeviceConfig {
        public List<String> drone_sns = new ArrayList<>();
        public List<String> dock_sns = new ArrayList<>();

        @Override
        public String toString() {
            return String.format("DeviceConfig{drone_sns=%s, dock_sns=%s}",
                drone_sns, dock_sns);
        }
    }
    
    /**
     * 应用配置
     */
    public static class AppConfig {
        public String encoding;
        public String working_dir;
        
        @Override
        public String toString() {
            return String.format("AppConfig{encoding='%s', working_dir='%s'}", 
                encoding, working_dir);
        }
    }
    
    /**
     * Demo 运行基准目录：可执行 jar 所在目录；IDE/gradle run 时回退到 user.dir。
     * 配置文件应放在 jar 同级（build 会拷贝 config*.json 与 drtc_config_node*.json）。
     */
    public static File getAppBaseDir() {
        try {
            CodeSource cs = ConfigManager.class.getProtectionDomain().getCodeSource();
            if (cs != null) {
                URL location = cs.getLocation();
                if (location != null) {
                    File loc = new File(location.toURI());
                    if (loc.isFile()) {
                        File parent = loc.getParentFile();
                        if (parent != null) {
                            return parent;
                        }
                    }
                }
            }
        } catch (URISyntaxException | SecurityException ignored) {
            // fall through
        }
        return new File(System.getProperty("user.dir", "."));
    }

    /**
     * 解析已存在的文件路径：先按原路径，再相对 CWD，再相对 jar 目录。
     * @return 绝对路径；找不到返回 null
     */
    public static String resolveExistingFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String trimmed = path.trim();
        File direct = new File(trimmed);
        if (direct.isFile()) {
            return direct.getAbsolutePath();
        }
        if (!direct.isAbsolute()) {
            File besideJar = new File(getAppBaseDir(), trimmed);
            if (besideJar.isFile()) {
                return besideJar.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * 初始化配置
     */
    public static void initConfig(String jsonConfigPath) {
        if (config == null) {
            config = new DemoConfig();
            
            // 如果提供了json配置文件路径，则加载json配置
            if (jsonConfigPath != null && !jsonConfigPath.trim().isEmpty()) {
                String resolved = resolveExistingFile(jsonConfigPath);
                if (resolved == null) {
                    throw new RuntimeException("配置文件不存在: " + jsonConfigPath
                            + "（已试 CWD 与 jar 同级目录 " + getAppBaseDir().getAbsolutePath() + "）");
                }
                loadConfigFromJsonFile(resolved);
            }
        }
    }
    /**
     * RTMP推流配置
     */
    public static class RtmpConfig {
        public String server_url;
        public String stream_key;
        public int frame_rate = 30;
        public int bitrate = 2000000; // 2Mbps
        public int width = 1920;
        public int height = 1080;
        public boolean enabled = false;
        
        @Override
        public String toString() {
            return String.format(
                "RtmpConfig{server_url='%s', stream_key='%s', frame_rate=%d, bitrate=%d, resolution=%dx%d, enabled=%s}",
                server_url, stream_key, frame_rate, bitrate, width, height, enabled
            );
        }
    }

    
    /**
     * 从JSON文件加载配置
     */
    private static void loadConfigFromJsonFile(String jsonConfigPath) {
        try (InputStream input = new FileInputStream(jsonConfigPath)) {
            byte[] bytes = new byte[input.available()];
            input.read(bytes);
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            
            // 解析JSON内容
            parseJsonConfig(jsonContent);
            
            System.out.println("JSON配置已从文件加载: " + jsonConfigPath);
            
        } catch (IOException e) {
            System.err.println("无法加载JSON配置文件: " + jsonConfigPath + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 解析JSON配置内容
     */
    private static void parseJsonConfig(String jsonContent) {
        // 解析服务器配置
        if (jsonContent.contains("\"server\"")) {
            // 提取server对象内容
            Pattern serverPattern = Pattern.compile("\"server\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher serverMatcher = serverPattern.matcher(jsonContent);
            if (serverMatcher.find()) {
                String serverContent = serverMatcher.group(1);
                
                // 解析address
                Pattern addressPattern = Pattern.compile("\"address\"\\s*:\\s*\"([^\"]+)\"");
                Matcher addressMatcher = addressPattern.matcher(serverContent);
                if (addressMatcher.find()) {
                    config.server.address = addressMatcher.group(1);
                }
                
                // 解析port
                Pattern portPattern = Pattern.compile("\"port\"\\s*:\\s*\"([^\"]+)\"");
                Matcher portMatcher = portPattern.matcher(serverContent);
                if (portMatcher.find()) {
                    config.server.port = portMatcher.group(1);
                }
                
                // 解析notification_server
                Pattern notificationServerPattern = Pattern.compile("\"notification_server\"\\s*:\\s*\"([^\"]+)\"");
                Matcher notificationServerMatcher = notificationServerPattern.matcher(serverContent);
                if (notificationServerMatcher.find()) {
                    config.server.notification_server = notificationServerMatcher.group(1);
                }

                // 解析verify_ssl
                Pattern verifySslPattern = Pattern.compile("\"verify_ssl\"\\s*:\\s*(true|false)");
                Matcher verifySslMatcher = verifySslPattern.matcher(serverContent);
                if (verifySslMatcher.find()) {
                    config.server.verify_ssl = Boolean.parseBoolean(verifySslMatcher.group(1));
                }

                // 解析ssl_ca_path
                Pattern sslCaPathPattern = Pattern.compile("\"ssl_ca_path\"\\s*:\\s*\"([^\"]*)\"");
                Matcher sslCaPathMatcher = sslCaPathPattern.matcher(serverContent);
                if (sslCaPathMatcher.find()) {
                    config.server.ssl_ca_path = sslCaPathMatcher.group(1);
                }
            }
        }
        
        // 解析SDK配置
        if (jsonContent.contains("\"sdk\"")) {
            Pattern sdkPattern = Pattern.compile("\"sdk\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher sdkMatcher = sdkPattern.matcher(jsonContent);
            if (sdkMatcher.find()) {
                String sdkContent = sdkMatcher.group(1);
                
                // 解析log_level
                Pattern logLevelPattern = Pattern.compile("\"log_level\"\\s*:\\s*\"([^\"]+)\"");
                Matcher logLevelMatcher = logLevelPattern.matcher(sdkContent);
                if (logLevelMatcher.find()) {
                    config.sdk.log_level = logLevelMatcher.group(1);
                }
                
                // 解析log_to_console
                Pattern logToConsolePattern = Pattern.compile("\"log_to_console\"\\s*:\\s*(true|false)");
                Matcher logToConsoleMatcher = logToConsolePattern.matcher(sdkContent);
                if (logToConsoleMatcher.find()) {
                    config.sdk.log_to_console = Boolean.parseBoolean(logToConsoleMatcher.group(1));
                }
                
                // 解析log_file_path
                Pattern logFilePathPattern = Pattern.compile("\"log_file_path\"\\s*:\\s*\"([^\"]+)\"");
                Matcher logFilePathMatcher = logFilePathPattern.matcher(sdkContent);
                if (logFilePathMatcher.find()) {
                    config.sdk.log_file_path = logFilePathMatcher.group(1);
                }
                
                // 解析log_max_file_size
                Pattern logMaxFileSizePattern = Pattern.compile("\"log_max_file_size\"\\s*:\\s*(\\d+)");
                Matcher logMaxFileSizeMatcher = logMaxFileSizePattern.matcher(sdkContent);
                if (logMaxFileSizeMatcher.find()) {
                    config.sdk.log_max_file_size = Long.parseLong(logMaxFileSizeMatcher.group(1));
                }
                
                // 解析log_max_files
                Pattern logMaxFilesPattern = Pattern.compile("\"log_max_files\"\\s*:\\s*(\\d+)");
                Matcher logMaxFilesMatcher = logMaxFilesPattern.matcher(sdkContent);
                if (logMaxFilesMatcher.find()) {
                    config.sdk.log_max_files = Integer.parseInt(logMaxFilesMatcher.group(1));
                }

                // 解析 drtc_config_path
                Pattern drtcConfigPathPattern = Pattern.compile("\"drtc_config_path\"\\s*:\\s*\"([^\"]+)\"");
                Matcher drtcConfigPathMatcher = drtcConfigPathPattern.matcher(sdkContent);
                if (drtcConfigPathMatcher.find()) {
                    config.sdk.drtc_config_path = drtcConfigPathMatcher.group(1);
                }
            }
        }
        
        // 解析设备配置
        if (jsonContent.contains("\"devices\"")) {
            Pattern devicesPattern = Pattern.compile("\"devices\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher devicesMatcher = devicesPattern.matcher(jsonContent);
            if (devicesMatcher.find()) {
                String devicesContent = devicesMatcher.group(1);
                
                // 解析drone_sns数组
                Pattern droneSnsPattern = Pattern.compile("\"drone_sns\"\\s*:\\s*\\[([^\\]]*)\\]");
                Matcher droneSnsMatcher = droneSnsPattern.matcher(devicesContent);
                if (droneSnsMatcher.find()) {
                    String droneSnsContent = droneSnsMatcher.group(1);
                    config.devices.drone_sns.clear();
                    Pattern snPattern = Pattern.compile("\"([^\"]+)\"");
                    Matcher snMatcher = snPattern.matcher(droneSnsContent);
                    while (snMatcher.find()) {
                        config.devices.drone_sns.add(snMatcher.group(1));
                    }
                }
                
                // 解析dock_sns数组
                Pattern dockSnsPattern = Pattern.compile("\"dock_sns\"\\s*:\\s*\\[([^\\]]*)\\]");
                Matcher dockSnsMatcher = dockSnsPattern.matcher(devicesContent);
                if (dockSnsMatcher.find()) {
                    String dockSnsContent = dockSnsMatcher.group(1);
                    config.devices.dock_sns.clear();
                    Pattern snPattern = Pattern.compile("\"([^\"]+)\"");
                    Matcher snMatcher = snPattern.matcher(dockSnsContent);
                    while (snMatcher.find()) {
                        config.devices.dock_sns.add(snMatcher.group(1));
                    }
                }
            }
        }
        
        // 解析应用配置
        if (jsonContent.contains("\"app\"")) {
            Pattern appPattern = Pattern.compile("\"app\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher appMatcher = appPattern.matcher(jsonContent);
            if (appMatcher.find()) {
                String appContent = appMatcher.group(1);
                
                // 解析encoding
                Pattern encodingPattern = Pattern.compile("\"encoding\"\\s*:\\s*\"([^\"]+)\"");
                Matcher encodingMatcher = encodingPattern.matcher(appContent);
                if (encodingMatcher.find()) {
                    config.app.encoding = encodingMatcher.group(1);
                }
                
                // 解析working_dir
                Pattern workingDirPattern = Pattern.compile("\"working_dir\"\\s*:\\s*\"([^\"]+)\"");
                Matcher workingDirMatcher = workingDirPattern.matcher(appContent);
                if (workingDirMatcher.find()) {
                    config.app.working_dir = workingDirMatcher.group(1);
                }
            }
        }

        // 解析RTMP配置
        if (jsonContent.contains("\"rtmp\"")) {
            Pattern rtmpPattern = Pattern.compile("\"rtmp\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher rtmpMatcher = rtmpPattern.matcher(jsonContent);
            if (rtmpMatcher.find()) {
                String rtmpContent = rtmpMatcher.group(1);

                Matcher m;

                m = Pattern.compile("\"server_url\"\\s*:\\s*\"([^\"]+)\"").matcher(rtmpContent);
                if (m.find()) config.rtmp.server_url = m.group(1);

                m = Pattern.compile("\"stream_key\"\\s*:\\s*\"([^\"]+)\"").matcher(rtmpContent);
                if (m.find()) config.rtmp.stream_key = m.group(1);

                m = Pattern.compile("\"frame_rate\"\\s*:\\s*(\\d+)").matcher(rtmpContent);
                if (m.find()) config.rtmp.frame_rate = Integer.parseInt(m.group(1));

                m = Pattern.compile("\"bitrate\"\\s*:\\s*(\\d+)").matcher(rtmpContent);
                if (m.find()) config.rtmp.bitrate = Integer.parseInt(m.group(1));

                m = Pattern.compile("\"width\"\\s*:\\s*(\\d+)").matcher(rtmpContent);
                if (m.find()) config.rtmp.width = Integer.parseInt(m.group(1));

                m = Pattern.compile("\"height\"\\s*:\\s*(\\d+)").matcher(rtmpContent);
                if (m.find()) config.rtmp.height = Integer.parseInt(m.group(1));

                m = Pattern.compile("\"enabled\"\\s*:\\s*(true|false)").matcher(rtmpContent);
                if (m.find()) config.rtmp.enabled = Boolean.parseBoolean(m.group(1));
            }
        }

        // 解析 auth（gateway header）；value 为空时由 resolveAuthHeaderValue() 回退环境变量
        if (jsonContent.contains("\"auth\"")) {
            Pattern authPattern = Pattern.compile("\"auth\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher authMatcher = authPattern.matcher(jsonContent);
            if (authMatcher.find()) {
                String authContent = authMatcher.group(1);
                Matcher m;
                m = Pattern.compile("\"header_key\"\\s*:\\s*\"([^\"]+)\"").matcher(authContent);
                if (m.find()) {
                    config.auth.header_key = m.group(1);
                }
                m = Pattern.compile("\"header_value\"\\s*:\\s*\"([^\"]*)\"").matcher(authContent);
                if (m.find()) {
                    config.auth.header_value = m.group(1);
                }
            }
        }
    }

    private static boolean isAuthPlaceholder(String value) {
        if (value == null) {
            return true;
        }
        String v = value.trim();
        return v.isEmpty()
                || "YOUR_AUTH_VALUE".equals(v)
                || "YOUR_AUTH_TOKEN".equals(v)
                || "XXXXXX".equals(v);
    }

    private static String maskAuthValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    /**
     * 解析鉴权头 key：config.auth.header_key，缺省空字符串。
     */
    public static String resolveAuthHeaderKey() {
        DemoConfig cfg = getConfig();
        if (cfg.auth != null && cfg.auth.header_key != null && !cfg.auth.header_key.trim().isEmpty()) {
            return cfg.auth.header_key.trim();
        }
        return "";
    }

    /**
     * 解析鉴权头 value：优先 config.auth.header_value，占位/空则回退 {@link #ENV_AUTH_VALUE}。
     * @return 非空鉴权值；都未配置时返回空串
     */
    public static String resolveAuthHeaderValue() {
        DemoConfig cfg = getConfig();
        if (cfg.auth != null && !isAuthPlaceholder(cfg.auth.header_value)) {
            return cfg.auth.header_value.trim();
        }
        String env = System.getenv(ENV_AUTH_VALUE);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return "";
    }

    public static boolean hasAuth() {
        return !resolveAuthHeaderValue().isEmpty();
    }
    
    /**
     * 获取配置
     */
    public static DemoConfig getConfig() {
        if (config == null) {
            initConfig(null);
        }
        return config;
    }
    
    /**
     * 打印配置信息
     */
    public static void printConfig() {
        DemoConfig cfg = getConfig();
        System.out.println("\n=== 配置信息 ===");
        System.out.println("服务器地址: " + cfg.server.address);
        System.out.println("服务器端口: " + cfg.server.port);
        System.out.println("通知服务器: " + cfg.server.notification_server);
        System.out.println("日志级别: " + cfg.sdk.log_level);
        System.out.println("终端日志: " + cfg.sdk.log_to_console);
        System.out.println("日志文件路径: " + cfg.sdk.log_file_path);
        System.out.println("日志文件最大大小: " + cfg.sdk.log_max_file_size + " bytes");
        System.out.println("最大日志文件数量: " + cfg.sdk.log_max_files);
        System.out.println("编码格式: " + cfg.app.encoding);
        System.out.println("工作目录: " + cfg.app.working_dir);
        System.out.println("飞机序列号: " + cfg.devices.drone_sns);
        System.out.println("机场序列号: " + cfg.devices.dock_sns);
        String authKey = resolveAuthHeaderKey();
        String authValue = resolveAuthHeaderValue();
        System.out.println("鉴权头: " + authKey + "=" + (authValue.isEmpty() ? "(未配置)" : maskAuthValue(authValue)));
        System.out.println("================\n");
    }
}