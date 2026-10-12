// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK 运行时门面：封装「初始化 → 注入鉴权头 → 获取 Workspace → shutdown」全生命周期。
 *
 * <p>本身实现 {@link AutoCloseable}, 配合 try-with-resources 保证 {@link FlightHubSDK#shutdown()}
 * 一定被调用, 避免散落且易漏的手动 shutdown。
 *
 * <p>鉴权采用「gateway header 注入」模型(本 SDK 版本无 setToken) ：
 * 把 {@code auth.auth_key_values} 解析出的一组 key/value 同时注入 HTTP 与 WebSocket 头。
 *
 * <pre>{@code
 * try (SdkRuntime rt = SdkRuntime.start(DemoConfig.load("config.json"))) {
 *     Workspace ws = rt.workspace();
 *     ...
 * } // 自动 shutdown
 * }</pre>
 */
public final class SdkRuntime implements AutoCloseable {

    private static final String TAG = "SdkRuntime";

    /**
     * {@link FlightHubSDK#shutdown()} 的看门狗超时(ms)。用过 RTC 直播后, native shutdown 可能
     * 阻塞/死锁在 RTC 链路 teardown;超过此时长仍未返回, 则放弃等待(由入口兜底强制退出进程),
     * 避免用户请求退出后整个进程「卡住无反应」。
     */
    private static final long SHUTDOWN_TIMEOUT_MS = 5000L;

    private final DemoConfig config;
    /**
     * volatile：demo 自身在主线程串行使用(close 由入口 try-with-resources 调用), 但本类是参考实现,
     * 集成方可能从关闭线程/shutdown 钩子调 {@link #close()} 而工作线程仍在读 {@link #ensureStarted()},
     * volatile 保证这种跨线程使用下的可见性(防御性加固)。
     */
    private volatile boolean started;

    private SdkRuntime(DemoConfig config) {
        this.config = config;
    }

    /** 初始化 SDK 并完成鉴权头注入。失败抛 {@link FlightHubException}。 */
    public static SdkRuntime start(DemoConfig config) {
        SdkRuntime rt = new SdkRuntime(config);
        rt.doStart();
        return rt;
    }

    private void doStart() {
        // JNI 必须先显式加载：下面的 setHttpHeaders/setWebSocketAuthHeaders 会走 native,
        // 而 native 未加载时二者只会返回 ERROR_NOT_INITIALIZED(见 FlightHubSdkManager)。
        // JNI 只在 initialize() 内部才自动加载, 但鉴权头要在 initialize 之前注入, 故此处先加载。
        // loadLibrary() 幂等(内部 libraryLoaded 标志), initialize() 内会再次校验, 不会重复加载。
        loadNativeLibrary();

        FlightHubSdkConfig cfg = new FlightHubSdkConfig();
        cfg.setAddress(config.address());
        cfg.setPort(config.port());
        cfg.setNotificationServerAddress(config.notificationServer());
        // TLS 透传: 私有化自签名部署经 server.ssl_ca_path 提供私有 CA,
        // 否则 WSS/HTTPS 证书链校验失败会触发「连接错误回调 + shutdown」竞态崩溃路径。
        cfg.setVerifySsl(config.verifySsl());
        cfg.setSslCaPath(config.sslCaPath());
        cfg.setDrtcConfigPath(config.drtcConfigPath());
        cfg.setLogLevel(config.logLevel());
        cfg.setLogToConsole(config.logToConsole());
        cfg.setLogFilePath(config.logFilePath());
        cfg.setLogMaxFileSize(config.logMaxFileSize());
        cfg.setLogMaxFiles(config.logMaxFiles());

        // 鉴权头必须在 initialize 之前注入(HTTP + WebSocket 两个平面) 。
        if (config.hasAuth()) {
            List<HeaderKeyValue> headers = new ArrayList<HeaderKeyValue>();
            StringBuilder keys = new StringBuilder();
            for (DemoConfig.AuthHeader h : config.authHeaders()) {
                headers.add(new HeaderKeyValue(h.key(), h.value()));
                if (keys.length() > 0) {
                    keys.append(',');
                }
                keys.append(h.key());
            }
            check(FlightHubSDK.setHttpHeaders(headers), "setHttpHeaders");
            check(FlightHubSDK.setWebSocketAuthHeaders(headers), "setWebSocketAuthHeaders");
            DemoLog.info(TAG, "已注入鉴权头(" + headers.size() + " 个): " + keys + "=*******");
        } else {
            // 空鉴权直接拒绝启动: 未鉴权即 initialize 会让后续接口以匿名身份触达网关,
            // 属于 P1 级安全风险(SDK-P1-11)。生产环境必须通过 auth.auth_key_values 或
            // 环境变量 FLIGHTHUB_AUTH_VALUE 注入鉴权令牌。
            throw new IllegalStateException(
                "拒绝在无鉴权状态下启动 SDK: auth.auth_key_values 未配置任何有效键值对, 且环境变量 "
                + DemoConfig.ENV_AUTH_VALUE
                + " 均为空。请在 config.json 中配置 auth.auth_key_values, 或设置环境变量后重试。"
            );
        }

        FlightHubSdkErrorCode rc = FlightHubSDK.initialize(cfg);
        check(rc, "initialize");
        started = true;
        DemoLog.info(
            TAG, "SDK 初始化成功: " + config.address() + ":" + config.port()
            + " version=" + safeVersion()
        );
    }

    /** 获取全局 Workspace 实例。 */
    public Workspace workspace() {
        ensureStarted();
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new FlightHubException("获取 Workspace 实例失败(返回 null)");
        }
        return ws;
    }

    public boolean isStarted() {
        return started;
    }

    public DemoConfig config() {
        return config;
    }

    @Override
    public void close() {
        if (!started) {
            return;
        }
        started = false;
        // 用过 RTC 直播(LiveModule)后, FlightHubSDK.shutdown() 的 native 实现可能阻塞/死锁在
        // RTC 链路 teardown, 直接同步调用会把退出流程整个卡死(表现为退出后输入 q「无反应」)。
        // 故放到守护线程里带超时执行：正常路径它很快返回并记日志;若超时(native 卡住)则记录并返回,
        // 让 close() 不再无限阻塞, 由入口(DemoEntry.main 末尾)兜底强制结束进程。
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FlightHubSdkErrorCode rc = FlightHubSDK.shutdown();
                    DemoLog.info(TAG, "SDK 已 shutdown: " + rc);
                } catch (Throwable t) {
                    DemoLog.warn(TAG, "shutdown 异常: " + Utils.describe(t));
                }
            }
        }, "sdk-shutdown");
        worker.setDaemon(true);
        worker.start();
        try {
            worker.join(SHUTDOWN_TIMEOUT_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        if (worker.isAlive()) {
            DemoLog.warn(
                TAG, "SDK shutdown 超时(" + SHUTDOWN_TIMEOUT_MS
                + "ms, native 可能阻塞在 RTC teardown), 放弃等待, 由入口强制退出进程"
            );
        }
    }

    private void ensureStarted() {
        if (!started) {
            throw new FlightHubException("SDK 未初始化");
        }
    }

    /**
     * 显式加载 JNI 原生库, 把各类加载失败统一收敛成 {@link FlightHubException}。
     *
     * <p>{@link JniLibraryLoader#loadLibrary()} 的失败面较杂：平台不支持抛
     * {@code UnsupportedOperationException}、依赖库/IO 失败包成 {@code RuntimeException},
     * 而主库那句 {@code System.load(...)} 抛的是 {@code UnsatisfiedLinkError}(属 {@code Error},
     * 不是 {@code Exception}, 上层 {@code catch(Exception)} 接不住会直接崩)。故这里连同
     * {@code LinkageError} 一并捕获, 转成本类统一风格的 {@link FlightHubException}(属非受检运行时异常,
     * 仍可被上层按需 catch 捕获, 不吞 OOM/StackOverflow 等真正致命的 {@code Error})。
     */
    private static void loadNativeLibrary() {
        try {
            JniLibraryLoader.loadLibrary();
        } catch (Exception | LinkageError t) {
            // 附上当前平台与已内置原生库的平台矩阵：SDK jar 官方交付只内置 linux-x86_64 的原生库,
            // 其它平台(Windows / macOS / linux-aarch64)会在此加载失败。给出可读提示, 而非裸的
            // UnsatisfiedLinkError, 让集成者一眼定位是"平台不支持"而非配置错误。
            String platform = System.getProperty("os.name") + "/" + System.getProperty("os.arch");
            throw new FlightHubException(
                "加载 JNI 原生库失败: " + Utils.describe(t)
                + " [当前平台=" + platform
                + "; 本 SDK 官方交付包仅内置原生库: linux-x86_64]", t);
        }
    }

    private static void check(FlightHubSdkErrorCode rc, String op) {
        if (rc != FlightHubSdkErrorCode.SUCCESS) {
            throw new FlightHubException(op + " 失败: " + rc);
        }
    }

    private static String safeVersion() {
        try {
            return FlightHubSDK.getVersion();
        } catch (Throwable t) {
            return "unknown";
        }
    }
}
