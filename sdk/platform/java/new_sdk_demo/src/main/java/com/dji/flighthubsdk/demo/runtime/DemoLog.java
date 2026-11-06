// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 统一日志出口: 自封装的滚动文件日志, <b>不依赖</b> {@code com.dji.flighthubsdk.util.Logger}。
 *
 * <p>写文件方案:
 * <ul>
 *   <li>行格式: {@code [时间] LEVEL [线程id:SDKDemo] [SDKDemo::tag] message}; </li>
 *   <li>目录: {@code demo_logs_dir/yyyy-MM-dd/}(不存在则创建); </li>
 *   <li>文件名: {@code sdk_demo_log_yyyy_MM_dd_index.log}, index 从 1 递增; </li>
 *   <li>单文件超过配置的大小切下一个 index; 跨天进入新日期目录并重置 index=1; </li>
 *   <li>写文件失败回退 {@code System.out}, 保证日志不丢。</li>
 * </ul>
 *
 * <p>日志根目录、单文件上限与<b>最低落盘级别</b>由 {@link #configure(String, int, String)} 在入口处显式注入
 * (前两者来自 demo_params.json 的 {@code logging} 节, 级别来自 config.json 的 {@code sdk.log_level});
 * 在 configure 之前 (引导期)使用默认值且<b>不做级别过滤</b>。tag 作为模块/场景名 (日志行格式中的 method 段)。
 * 线程安全: 所有写入与配置在 {@link #logLock} 上串行, 级别读取走 {@code volatile}。
 *
 * <p><b>编码</b>: 日志行在写入时先编码为 UTF-8 字节数组, 随即写入 {@link BufferedOutputStream},
 * 编码只发生一次 (原先使用 {@code Writer} 链时, {@code getBytes} 计长 + {@code OutputStreamWriter}
 * 写入各做一次编码, 共两次)。
 */
public final class DemoLog {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy_MM_dd");

    /** 日志级别序数, 与 {@link #log(String, String, String)} 的级别字面量一一对应。 */
    private static final int LV_DEBUG = 0;
    private static final int LV_INFO = 1;
    private static final int LV_WARN = 2;
    private static final int LV_ERROR = 3;
    private static final String[] LEVEL_NAMES = {"DEBUG", "INFO", "WARN", "ERROR"};

    /** 默认日志根目录 (configure 前的引导期使用)。 */
    private static final String DEFAULT_LOG_DIR = "demo_logs";
    /** 默认单文件上限 30 MB。 */
    private static final long DEFAULT_MAX_FILE_SIZE = 30L * 1024 * 1024;

    private static final Object logLock = new Object();

    /** 日志根目录 (可配置)。 */
    private static String logRootDir = DEFAULT_LOG_DIR;
    /** 单文件上限字节数 (可配置)。 */
    private static long maxFileSize = DEFAULT_MAX_FILE_SIZE;

    /**
     * 低于该级别的日志不落盘, 由 {@link #configure} 从 {@code config.json} 的 {@code sdk.log_level} 注入。
     *
     * <p><b>引导期默认 DEBUG(不过滤)</b>: configure 之前恰恰是"配置还没读进来"的阶段, 这时候丢日志
     * 会把「为什么没读到配置」这件事本身一并丢掉。
     *
     * <p>{@code volatile}: 写只发生在 {@link #configure}(持 {@link #logLock}), 读发生在任意业务/回调线程,
     * 用 volatile 保证可见性而不必让每条日志都先抢锁。ERROR 是最高级, 永不被过滤。
     */
    private static volatile int minLevel = LV_DEBUG;

    private static OutputStream logStream = null;
    private static String logFileDate = null;   // 当前日志文件对应日期
    private static int logFileIndex = 1;        // 当前日志文件序号, 从 1 开始
    private static long logFileSize = 0;        // 当前文件已写入字节数
    /** 写文件失败是否已在 stderr 告警过 (持续失败只告警一次, 写入恢复后复位; 读写均在 logLock 内)。 */
    private static boolean writeFailureWarned = false;

    private DemoLog() {}

    /**
     * 注入日志配置: 根目录、单文件大小、最低落盘级别。会重开日志文件到新目录, 使后续日志立即落到配置位置。
     *
     * <p><b>由入口显式调用</b>(不再藏在 {@code DemoParams} 的构造里): 前两个参数来自
     * {@code demo_params.json} 的 {@code logging} 节, 级别来自 {@code config.json} 的
     * {@code sdk.log_level}——跨两个配置文件的副作用不该由某一个的"加载"方法顺手做掉。
     *
     * @param demoLogsDir  日志根目录 (下面再按天建 yyyy-MM-dd 子目录); null/空则保持当前值
     * @param maxFileMb    单个日志文件上限 (MB), &lt;=0 时按默认 30MB
     * @param level        最低落盘级别 {@code debug|info|warn|error}(忽略大小写, 兼容
     *                     trace/verbose/warning/fatal 等别名); null/空/无法识别时回退 DEBUG(不过滤)
     */
    public static void configure(String demoLogsDir, int maxFileMb, String level) {
        int parsed = parseLevel(level);
        synchronized (logLock) {
            if (demoLogsDir != null && !demoLogsDir.isEmpty()) {
                logRootDir = demoLogsDir;
            }
            maxFileSize = maxFileMb > 0 ? maxFileMb * 1024L * 1024L : DEFAULT_MAX_FILE_SIZE;
            // 无法识别的级别一律回退 DEBUG(不过滤)而非静默按 INFO 处理: 宁可多写, 不可因为
            // 一个拼错的级别名让排查时最需要的那几行凭空消失。
            minLevel = parsed >= 0 ? parsed : LV_DEBUG;
            // 强制下次写入时按新配置重开文件。
            closeLogStream();
            logFileDate = null;
            logFileIndex = 1;
            logFileSize = 0;
        }
        // 启动时打印解析后的绝对路径与生效级别, 让人一眼知道日志写到哪、哪些级别会被丢掉
        // (直接走 stdout, 不进日志文件)。级别必须回显: 否则 sdk.log_level=info 时 DEBUG 全被
        // 过滤掉, 用户翻日志找不到 Node 的"配置类型错误"之类线索却不知道是被自己配没的。
        String abs = new File(logRootDir).getAbsolutePath();
        System.out.println(
            "[DemoLog] 日志根目录 = " + abs + " (按天分子目录 yyyy-MM-dd, 单文件上限 "
            + (maxFileSize / (1024 * 1024)) + "MB, 最低级别 " + LEVEL_NAMES[minLevel]
            + (parsed >= 0 ? "" : "; config.json 的 sdk.log_level=" + level + " 无法识别, 已回退 DEBUG")
            + ")"
        );
    }

    /** 级别名 → 序数; 无法识别返回 -1。兼容常见别名, 避免用户写 warning/trace 时被判为非法。 */
    private static int parseLevel(String name) {
        if (name == null) {
            return -1;
        }
        String s = name.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            return -1;
        }
        if ("debug".equals(s) || "trace".equals(s) || "verbose".equals(s) || "all".equals(s)) {
            return LV_DEBUG;
        }
        if ("info".equals(s)) {
            return LV_INFO;
        }
        if ("warn".equals(s) || "warning".equals(s)) {
            return LV_WARN;
        }
        if ("error".equals(s) || "fatal".equals(s) || "severe".equals(s)) {
            return LV_ERROR;
        }
        return -1;
    }

    /**
     * 把用户写的 {@code sdk.log_level} 归一成 SDK 认得的字面量 (DEBUG/INFO/WARN/ERROR)。
     *
     * <p><b>为什么需要归一</b>: SDK 侧 {@code FlightHubSDK.parseLogLevel} 走
     * {@code Logger.LogLevel.valueOf(level.toUpperCase())}, <b>只认这四个名字</b>, 且无法识别时
     * 回退 {@code INFO}——与 demo 侧「兼容 trace/verbose/all/warning/fatal/severe 别名、回退 DEBUG」
     * 的口径并不一致。若把原始字符串直接透传给 SDK, 用户写 {@code trace} 会得到
     * 「demo 侧 DEBUG + SDK 侧 INFO」两套级别, 写 {@code fatal} 更会让 SDK 静默降到 INFO
     * 而不是 ERROR。故在传给 SDK 之前先归一, 让 README 承诺的「同时作用于 SDK 与 demo」成立。
     *
     * @param level 原始配置值 (可为 null/空/别名/大小写混写)
     * @return 四个规范名之一; 无法识别时给 {@code "DEBUG"}(与 demo 侧回退一致, 不过滤)
     */
    public static String canonicalLevelName(String level) {
        int parsed = parseLevel(level);
        return LEVEL_NAMES[parsed >= 0 ? parsed : LV_DEBUG];
    }

    /** 打印 DEBUG 级日志 (tag 为模块/场景名)。 */
    public static void debug(String tag, String msg) {
        if (minLevel > LV_DEBUG) {
            return;
        }
        log("DEBUG", tag, msg);
    }

    /** 打印 INFO 级日志 (tag 为模块/场景名)。 */
    public static void info(String tag, String msg) {
        if (minLevel > LV_INFO) {
            return;
        }
        log("INFO", tag, msg);
    }

    /** 打印 WARN 级日志 (tag 为模块/场景名)。 */
    public static void warn(String tag, String msg) {
        if (minLevel > LV_WARN) {
            return;
        }
        log("WARN", tag, msg);
    }

    /** 打印 ERROR 级日志 (tag 为模块/场景名)。ERROR 是最高级, 不受 {@link #minLevel} 过滤。 */
    public static void error(String tag, String msg) {
        log("ERROR", tag, msg);
    }

    /** 当前是否启用 DEBUG 级别 (即 DEBUG 日志会落盘)。供调用方判断是否执行敏感信息的控制台输出。 */
    public static boolean isDebugEnabled() {
        return minLevel <= LV_DEBUG;
    }

    /**
     * 打印 ERROR 级日志并附带异常: 先打「消息 | 异常摘要」, 再单独落一整段堆栈。
     * 同样不受 {@link #minLevel} 过滤。
     */
    public static void error(String tag, String msg, Throwable t) {
        log("ERROR", tag, msg + (t != null ? " | " + Utils.describe(t) : ""));
        if (t != null) {
            log("ERROR", tag, stackTrace(t));
        }
    }

    // ---- 内部实现 (log/writeLog/openLogFile) ----

    /** 组装一行日志并写入文件。tag 同时充当日志行格式中的 method 段。 */
    private static void log(String level, String tag, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String threadId = String.format("0x%x", Thread.currentThread().getId());
        String line = String.format(
            "[%s] %s [%s:%s] [%s::%s] %s",
            timestamp, level, threadId, "SDKDemo", "SDKDemo", tag, message
        );

        // 交由滚动写入器落盘 (按天分目录、超限切文件、写失败回退 stdout)。
        writeLog(line);
    }

    /** 将一条日志写入滚动日志文件 (超 30MB 切换 index, 跨天重置)。 */
    private static void writeLog(String logMessage) {
        // 编码一次: 字节数组同时用于「计算长度以判断是否需要滚动」和「写入文件」,
        // 避免原先 Writer 链中「getBytes 计长 + OutputStreamWriter 写入」两次编码的冗余。
        byte[] bytes = (logMessage + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        int byteLen = bytes.length;
        synchronized (logLock) {
            try {
                String today = LocalDateTime.now().format(FILE_DATE_FORMATTER);
                if (!today.equals(logFileDate)) {
                    logFileDate = today;
                    logFileIndex = 1;
                    openLogFile();
                } else if (logStream == null) {
                    openLogFile();
                } else if (logFileSize > 0 && logFileSize + byteLen > maxFileSize) {
                    logFileIndex++;
                    openLogFile();
                }
                logStream.write(bytes);
                logStream.flush();
                logFileSize += byteLen;
                writeFailureWarned = false;
            } catch (IOException e) {
                // 写文件失败: 回退标准输出, 保证日志不丢。失败原因走 stderr 且每轮失败只告警一次
                // (不可回调 DemoLog 自身, 会造成写入链重入; 持续失败逐行刷告警会淹没控制台)——
                // 否则文件日志静默失效, 用户只看到 stdout 照常滚动, 极难察觉与排查。
                closeLogStream();
                if (!writeFailureWarned) {
                    writeFailureWarned = true;
                    System.err.println("[WARN] DemoLog 写日志文件失败, 回退 stdout: " + Utils.describe(e));
                }
                System.out.println(logMessage);
            }
        }
    }

    /** 打开当前日期、当前 index 的日志文件 (追加模式), 跳过已写满的同序号文件。 */
    private static void openLogFile() throws IOException {
        closeLogStream();
        if (logFileDate == null) {
            // 防御性兜底: 正常路径由 writeLog 保证非 null(它先算出 today 再调本方法)。
            // 这里补一手, 使「文件名日期」与「目录日期」在任何调用路径下都出自同一个时间戳。
            logFileDate = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        }
        // 日期子目录 yyyy-MM-dd 直接由文件名日期 yyyy_MM_dd 换算, 不再第二次取 now():
        // 原实现两处各调一次 LocalDateTime.now(), 只要跨零点的那一瞬间落在两次调用之间,
        // 就会把 sdk_demo_log_<昨天>_0001.log 建到 <今天>/ 目录下——当天日志被劈成两半,
        // 且文件名与所在目录自相矛盾, 事后按目录捞日志会直接漏掉这一段。
        String subDir = logFileDate.replace('_', '-');
        File dir = new File(logRootDir, subDir);
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            // mkdirs 失败且目录确实不存在 (二次 exists 兼容并发创建时 mkdirs 返回 false 的竞态):
            // 显式抛出真实根因, 否则后续 FileOutputStream 只会报误导性的 "No such file or directory"。
            throw new IOException("无法创建日志目录: " + dir.getAbsolutePath());
        }
        while (true) {
            File f = new File(dir, buildLogFileName(logFileDate, logFileIndex));
            if (f.exists() && f.length() >= maxFileSize) {
                logFileIndex++;
            } else {
                break;
            }
        }
        File file = new File(dir, buildLogFileName(logFileDate, logFileIndex));
        logStream = new BufferedOutputStream(new FileOutputStream(file, true));
        logFileSize = file.length();
    }

    /**
     * 构造日志文件名: sdk_demo_log_yyyy_MM_dd_index.log。
     * index 用至少 4 位零填充 (0001..9999), 保证字典序与时间序一致 (1000 条以内无乱序问题);
     * 超过 4 位则按实际序号输出 (10000+)——单日产生 10000 个 30MB 文件即 300GB/天, 实际不可达。
     */
    private static String buildLogFileName(String date, int index) {
        return String.format("sdk_demo_log_%s_%04d.log", date, index);
    }

    /** 关闭当前日志文件 stream 并置空 (切文件/重配置/写失败时调用)。幂等。 */
    private static void closeLogStream() {
        if (logStream != null) {
            try {
                logStream.close();
            } catch (IOException ignored) {
                // 关闭失败: 直接走 stderr 打 WARN, 避免回调 DemoLog 造成日志写入链重入。
                System.err.println("[WARN] DemoLog 关闭日志文件失败: " + Utils.describe(ignored));
            }
            logStream = null;
        }
    }

    /** 把异常堆栈转成字符串 (供 error(tag, msg, t) 完整落盘)。 */
    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
