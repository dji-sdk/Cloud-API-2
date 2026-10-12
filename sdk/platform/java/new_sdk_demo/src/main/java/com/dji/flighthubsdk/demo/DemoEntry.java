// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.module.AIBoxDemo;
import com.dji.flighthubsdk.demo.module.DeviceDebugDemo;
import com.dji.flighthubsdk.demo.module.DeviceManageDemo;
import com.dji.flighthubsdk.demo.module.DeviceOsdDemo;
import com.dji.flighthubsdk.demo.module.DockControlDemo;
import com.dji.flighthubsdk.demo.module.DrcDemo;
import com.dji.flighthubsdk.demo.module.FlightControlDemo;
import com.dji.flighthubsdk.demo.module.FlightSafetyDemo;
import com.dji.flighthubsdk.demo.module.FlightTaskDemo;
import com.dji.flighthubsdk.demo.module.FlightTaskRecordDemo;
import com.dji.flighthubsdk.demo.module.FlySafeUnlockDemo;
import com.dji.flighthubsdk.demo.module.LiveRecordDemo;
import com.dji.flighthubsdk.demo.module.LiveStreamDemo;
import com.dji.flighthubsdk.demo.module.MaintenanceDemo;
import com.dji.flighthubsdk.demo.module.MediaServiceDemo;
import com.dji.flighthubsdk.demo.module.PayloadControlDemo;
import com.dji.flighthubsdk.demo.module.PsdkDemo;
import com.dji.flighthubsdk.demo.module.SafetyServiceDemo;
import com.dji.flighthubsdk.demo.module.SystemManagementDemo;
import com.dji.flighthubsdk.demo.module.TerraBuildDemo;
import com.dji.flighthubsdk.demo.runtime.DemoConfig;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.DemoParams;
import com.dji.flighthubsdk.demo.runtime.DeviceSession;
import com.dji.flighthubsdk.demo.runtime.Devices;
import com.dji.flighthubsdk.demo.runtime.SdkRuntime;
import com.dji.flighthubsdk.demo.scenario.DrcFlightScenario;
import com.dji.flighthubsdk.demo.scenario.FlyToScenario;
import com.dji.flighthubsdk.demo.scenario.PoiScenario;
import com.dji.flighthubsdk.demo.scenario.WaylineScenario;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 模块化 Demo 统一入口。
 *
 * <p>数据驱动的注册表({@link #registry()})避免使用多个巨型 switch：
 * 每个模块/场景 demo 是一个独立类, 注册为一个 {@link Entry}(工厂 + 显示名)。
 *
 * <p>用法：
 * <ul>
 *   <li>交互菜单：{@code ./gradlew run}(或 {@code --args="config.json"});</li>
 *   <li>直接运行某个 demo：{@code ./gradlew run --args="config.json FlyToScenario"}(名称大小写不敏感)。</li>
 * </ul>
 */
public final class DemoEntry {

    /** 一个可运行的 demo 条目。 */
    static final class Entry {
        /** demo 显示名(菜单/命令行按此展示与查找, 查找时大小写不敏感)。 */
        final String key;
        /** 中文别名(仅用于菜单展示, 不参与查找/命令行匹配)。 */
        final String alias;
        /** 所属分组名(用于菜单归类, 见 {@link #groupModuleDemo}/{@link #groupScenario})。 */
        final String group;
        /** 延迟实例化工厂：每次运行按需 new 出独立的 {@link ModuleDemo}。 */
        final Supplier<ModuleDemo> factory;

        /** 全参构造：将显示名、别名、分组、工厂绑定到 final 字段(一次赋值, 构造后不可变)。 */
        Entry(String key, String alias, String group, Supplier<ModuleDemo> factory) {
            this.key = key;
            this.alias = alias;
            this.group = group;
            this.factory = factory;
        }
    }

    /** 纯静态工具类, 私有构造禁止实例化(所有能力经静态方法暴露)。 */
    private DemoEntry() {}

    /** 功能模块 demo 分组名。 */
    private static final String groupModuleDemo = "模块 Demo";
    /** 场景 demo 分组名。 */
    private static final String groupScenario = "场景 Demo";

    /** 交互输入流断开(EOF / stdin 关闭 / IOException)时的退出说明——区别于用户主动 q 退出。 */
    private static final String MSG_STDIN_EOF = "输入流已结束(EOF/stdin 关闭), 控制台无法继续交互, 退出";

    /** 注册表：新增 demo 只需在此加一行。 */
    static Map<String, Entry> registry() {
        Map<String, Entry> reg = new LinkedHashMap<String, Entry>();

        // ---- 功能模块 ----
        add(reg, "FlightControl", "飞行控制", groupModuleDemo, FlightControlDemo::new);
        add(reg, "PayloadControl", "负载控制", groupModuleDemo, PayloadControlDemo::new);
        add(reg, "LiveStream", "直播拉流", groupModuleDemo, LiveStreamDemo::new);
        add(reg, "LiveRecord", "直播录制", groupModuleDemo, LiveRecordDemo::new);
        add(reg, "DeviceOSD/State", "设备 OSD/State", groupModuleDemo, DeviceOsdDemo::new);
        add(reg, "DeviceManage", "设备管理", groupModuleDemo, DeviceManageDemo::new);
        add(reg, "DockControl", "机场控制", groupModuleDemo, DockControlDemo::new);
        add(reg, "DeviceDebug", "设备调试", groupModuleDemo, DeviceDebugDemo::new);
        add(reg, "DRCLink", "DRC 连接", groupModuleDemo, DrcDemo::new);
        add(reg, "PSDK", "PSDK 模块", groupModuleDemo, PsdkDemo::new);
        add(reg, "FlightTask", "飞行任务", groupModuleDemo, FlightTaskDemo::new);
        add(reg, "FlightSafety", "飞行安全", groupModuleDemo, FlightSafetyDemo::new);
        add(reg, "FlySafeUnlock", "限飞解禁", groupModuleDemo, FlySafeUnlockDemo::new);
        add(reg, "SafetyService", "安全服务", groupModuleDemo, SafetyServiceDemo::new);
        add(reg, "MediaService", "媒体服务", groupModuleDemo, MediaServiceDemo::new);
        add(reg, "TerraBuild", "Terra 重建", groupModuleDemo, TerraBuildDemo::new);
        add(reg, "SystemManagement", "系统管理", groupModuleDemo, SystemManagementDemo::new);
        add(reg, "FlightTaskRecord", "飞行记录", groupModuleDemo, FlightTaskRecordDemo::new);
        add(reg, "Maintenance", "报修管理", groupModuleDemo, MaintenanceDemo::new);
        add(reg, "AIBox", "AI 盒子", groupModuleDemo, AIBoxDemo::new);

        // ---- 场景 ----
        add(reg, "FlyToScenario", "FlyTo 指点飞行", groupScenario, FlyToScenario::new);
        add(reg, "PoiScenario", "POI 智能环绕", groupScenario, PoiScenario::new);
        add(reg, "WaylineScenario", "航线飞行", groupScenario, WaylineScenario::new);
        add(reg, "DrcFlightScenario", "DRC 飞行", groupScenario, DrcFlightScenario::new);

        return reg;
    }

    /** 往注册表登记一个 demo：以 key 的小写作为查找键(大小写不敏感), value 为 {@link Entry}。 */
    private static void add(Map<String, Entry> reg, String key, String alias, String group, Supplier<ModuleDemo> f) {
        reg.put(key.toLowerCase(), new Entry(key, alias, group, f));
    }

    /**
     * 程序入口。两种模式, 都先初始化一次 SDK、选一次设备：
     * <ul>
     *   <li>非交互单跑：{@code args[1]} 指定 demo 名 → 选设备后直接跑该 demo, 跑完退出;</li>
     *   <li>交互菜单：无 {@code args[1]} → 选设备后进菜单, 反复选 demo 直到 q 退出。</li>
     * </ul>
     *
     * @param args {@code args[0]}=config.json 路径(缺省 "config.json");{@code args[1]}=可选 demo 名
     */
    public static void main(String[] args) {
        // 控制台中文兜底(必须最先执行, 早于任何打印)：非 UTF-8 默认字符集的 JVM 上把 out/err 转 UTF-8。
        ensureUtf8Console();

        // 最后一道保险：任何线程(含 SDK 原生/RTC 回调线程)里逃逸的未捕获 Throwable 都记一条日志,
        // 而不是走 JVM 默认的裸 stderr 栈打印, 兑现"任何异常都要打印 log"。
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                DemoLog.error("DemoEntry", "线程 " + t.getName() + " 未捕获异常", e);
            }
        });

        String configPath = (args.length > 0) ? args[0] : "config.json";
        Map<String, Entry> reg = registry();

        // 非交互：第二个参数指定要跑的单个 demo; 解析不到就报错退出。
        Entry only = null;
        if (args.length > 1) {
            only = reg.get(args[1].toLowerCase());
            if (only == null) {
                warnBoth("未找到 demo: " + args[1]);
                logBoth("可用: " + String.join(", ", names(reg)));
                return;
            }
        }

        // 两种模式共用：初始化 SDK + 选设备; only != null 跑完即退, 否则进菜单。
        session(reg, configPath, only);

        // 兜底强制退出, 分两层(实测复盘: 用过 RTC 直播后 9/9 次退出全部卡死, 缺任何一层都不够):
        //  1) halt(0): 绕过 Java 层 shutdown hook 与残留非守护线程(旧问题: main 返回后 JVM 不退出)。
        //  2) 外部 SIGKILL 兜底(armExitBackstop): halt 仍要走 glibc exit() → 各 .so 的 atexit/静态析构;
        //     实测 FlightHubSDK.shutdown() 返回 SUCCESS 后仍残留 WebRTC-Shared-N/W/S 三条原生线程
        //     (VolcEngine RTC 共享线程, SDK 内部泄漏), 进程 exit 时 RTC 库析构函数与这些线程互等
        //     (pthread_cond_wait 死锁), halt 卡死在 native exit 路径——Java 层无法再补救,
        //     只能靠进程外的 kill -9(SIGKILL 不走任何退出处理)。
        // 顺序: 先布置兜底, 再 halt。正常路径(未用 RTC)halt 瞬间生效, 兜底进程 kill 到的是已消失的
        // pid, 静默无害;卡死路径则 ~3 秒后被 SIGKILL 收掉, 控制台如常回到 shell。
        // DemoLog 每条即时 flush, 不会因此丢日志;SdkRuntime.close 已尽力(带超时)优雅 shutdown 过。
        armExitBackstop();
        Runtime.getRuntime().halt(0);
    }

    /**
     * 布置「进程外 SIGKILL」退出兜底：spawn 一个独立的 {@code sh -c "sleep 3; kill -9 <本进程>"}。
     *
     * <p><b>为何需要</b>：{@link Runtime#halt} 只跳过 Java 层退出流程, 底层仍调用 glibc {@code exit()},
     * 会依次执行各动态库注册的 atexit/静态析构。本 SDK 用过 RTC 直播后会残留 WebRTC 原生线程,
     * RTC 库的退出析构与之死锁, {@code halt} 便永远不返回(实测每次必现)——此时 JVM 已在退出临界区,
     * 任何 Java 线程都会被冻结, 进程内无解;唯一可靠手段是进程外发 SIGKILL(不经过任何退出处理直接终止)。
     *
     * <p><b>退出码语义</b>：正常路径(未用过直播)halt 瞬间生效, 退出码 0;卡死路径由 SIGKILL 收尾,
     * 退出码 137(=128+9), 交互 shell 可能附带打印一行 "Killed"——<b>属预期</b>, 不是崩溃;
     * 外层脚本/CI 若用 {@code set -e}/{@code &&} 判定, 需把 137 视为成功(经 gradle run 则显示 FAILED, 同理忽略)。
     *
     * <p><b>已知不覆盖</b>：容器内 JVM 为 PID 1 时(如 {@code docker run <image> java -jar ...} 无 init 垫层),
     * 同命名空间内发给 PID 1 的 SIGKILL 会被内核静默丢弃(pid_namespaces(7)), 兜底无效——检测到时打
     * WARN 提示改用 {@code docker run --init}(或 tini), 让 java 不再是 PID 1。
     *
     * <p><b>无害性</b>：正常退出时本进程瞬间消失, 兜底 kill 的目标 pid 已不存在(3 秒内 pid 复用概率可忽略),
     * 错误输出已重定向丢弃;子进程 3 秒后自然结束。Windows 无 sh/kill(SDK 亦无 Windows 原生库), 直接跳过。
     * 任何失败(取不到 pid、exec 失败等)记 WARN 后退回纯 halt 行为, 不影响主流程。
     */
    private static void armExitBackstop() {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                return;
            }
            // Java 8 无 ProcessHandle, 取 pid 走 RuntimeMXBean 名称("pid@host")约定。
            String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            int at = jvmName.indexOf('@');
            long pid = Long.parseLong(jvmName.substring(0, at));
            if (pid == 1) {
                // 容器裸跑(java 即 PID 1)：同命名空间内 kill -9 1 会被内核静默忽略, 兜底注定无效。
                // 只能提示部署侧加 init 垫层;此处不 exec(避免制造一个必然无效的假兜底)。
                DemoLog.warn(
                    "DemoEntry", "本进程为 PID 1(容器内裸跑 java), SIGKILL 退出兜底无效;"
                    + "若用过直播后退出卡死, 请改用 docker run --init(或 tini)启动");
                return;
            }

            Runtime.getRuntime().exec(
                new String[] {
                    "sh", "-c", "sleep 3; kill -9 " + pid + " 2>/dev/null"
                }
            );
            DemoLog.debug("DemoEntry", "已布置退出兜底: 3 秒后 SIGKILL pid=" + pid
                + "(正常退出时为空操作;若由它收尾, 退出码 137 属预期)");
        } catch (Throwable t) {
            // 兜底布置失败：退回 halt-only(至多回到旧的卡死表现, 不引入新问题)。吞异常但不哑——
            // 此刻尚未进入退出临界区, DemoLog 每条即时 flush, 落一条 WARN 是安全的, 让排障能区分
            // 「兜底未生效」与「修复没部署」。
            DemoLog.warn("DemoEntry", "退出兜底布置失败, 退回 halt-only: " + Utils.describe(t));
        }
    }

    /**
     * Java 8 / 非 UTF-8 环境的控制台中文兜底：当 {@code System.out}/{@code System.err} 实际使用的字符集
     * 不是 UTF-8 时, 把它们重定向为 UTF-8 的 {@link PrintStream}, 保证中文日志/菜单不乱码——
     * 即便用户 {@code java -jar} 未加 {@code -Dfile.encoding=UTF-8} 也生效。
     *
     * <p><b>为何不能用 {@code Charset.defaultCharset()} 判断</b>：JDK 18+(JEP 400)把 {@code file.encoding}/
     * {@code defaultCharset()} 恒定为 UTF-8, 但控制台流真正使用的是 {@code stdout.encoding}/{@code stderr.encoding}
     * (JDK 19+) 或 {@code native.encoding}(JDK 18+); 在无 {@code LANG/LC_ALL}(POSIX C locale)的机器上后者可能是
     * {@code ANSI_X3.4-1968}(ASCII), 此时 System.out 会把中文烤成 {@code ?}, 而 {@code defaultCharset()} 仍报 UTF-8。
     * 故这里按「实际流编码」优先级 {@code stdout.encoding → native.encoding → file.encoding} 判断。
     *
     * <p>已是 UTF-8 则不动(为 no-op)。任何异常都吞掉(退回原样输出), 绝不因控制台设置本身把程序搞崩
     * (呼应本类"任何异常不裸崩"的基调)。日志文件不受影响：{@code DemoLog} 落盘时已显式用 UTF-8。
     */
    private static void ensureUtf8Console() {
        try {
            if (!isUtf8(effectiveEncoding("stdout.encoding", "sun.stdout.encoding"))) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
            }
            if (!isUtf8(effectiveEncoding("stderr.encoding", "sun.stderr.encoding"))) {
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
            }
        } catch (Throwable ignore) {
            // 兜底失败就退回默认输出流, 不影响主流程。
        }
    }

    /** 解析某个标准流「实际使用的字符集名」：按现代 key → 旧 key(Java 8) → native.encoding → file.encoding 依次取首个非空。 */
    private static String effectiveEncoding(String modernKey, String legacyKey) {
        String[] keys = { modernKey, legacyKey, "native.encoding", "file.encoding" };
        for (String k : keys) {
            String v = System.getProperty(k);
            if (v != null && !v.isEmpty()) {
                return v;
            }
        }
        return null;
    }

    /** 字符集名是否等价于 UTF-8(容错大小写/别名)。null/未知/非法一律视为「非 UTF-8」→ 触发兜底包装。 */
    private static boolean isUtf8(String encodingName) {
        if (encodingName == null || encodingName.isEmpty()) {
            return false;
        }
        try {
            return StandardCharsets.UTF_8.equals(Charset.forName(encodingName));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 会话主流程(交互菜单 + 非交互单跑共用)：
     * <ol>
     *   <li>加载配置 → 初始化 SDK(一次, try-with-resources 保证退出时 deinit);</li>
     *   <li>拉在线设备列表 → 选设备(仅一台自动选中, 多台提示) → 打印所选设备信息;</li>
     *   <li>{@code only != null}(非交互)：用共享 runtime 跑这一个 demo, 跑完退出;</li>
     *   <li>{@code only == null}(交互)：循环展示菜单, 反复选 demo/重选设备, q 退出。</li>
     * </ol>
     *
     * @param only 非交互指定要跑的单个 demo; 为 null 时进交互菜单
     */
    private static void session(Map<String, Entry> reg, String configPath, Entry only) {
        List<Entry> entries = new ArrayList<Entry>(reg.values());
        DemoConfig cfg = null;
        DemoParams params = null;
        try {
            cfg = DemoConfig.load(configPath);
            params = DemoParams.load();
        } catch (Exception ex) {
            errBoth("加载配置失败", ex);
            return;
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            // 初始化一次;退出自动 shutdown;
            SdkRuntime rt = SdkRuntime.start(cfg)) {

            Workspace ws = rt.workspace();
            long timeoutMs = params.defaultTimeout();

            // SDK 版本信息：同时落日志文件和控制台。
            String sdkVersion = FlightHubSDK.getVersion();
            String sdkBuildInfo = FlightHubSDK.getBuildInfo();
            logBoth("SDK 版本: " + (sdkVersion != null && !sdkVersion.isEmpty() ? sdkVersion : "(未获取到)"));
            logBoth("SDK 构建信息: " + (sdkBuildInfo != null && !sdkBuildInfo.isEmpty() ? sdkBuildInfo : "(未获取到)"));

            // 是否需要设备：交互菜单可能跑到任意 demo, 先选一次; 非交互单跑按该 demo 的
            // requiresDevice() 决定——否则无设备 demo 在无在线设备时会被误挡。
            //
            // 注意：非交互模式先实例化 demo 以读取 requiresDevice(), 后续 runEntry 直接复用该实例
            // (而非二次调用工厂), 避免多余的对象创建——若将来某个 demo 在构造函数中有副作用(如分配资源),
            // 双重实例化会变成真正的 Bug。
            ModuleDemo onlyDemo = (only != null) ? only.factory.get() : null;
            boolean needDevice = (only == null) || onlyDemo.requiresDevice();
            DeviceTopo selected = null;
            if (needDevice) {
                // 选一次设备(仅一台自动选中, 多台提示)。
                selected = pickDevice(ws, timeoutMs, in);
                if (selected == null) {
                    logBoth("未选择设备, 退出");
                    return;
                }
            }

            // 非交互单跑：直接执行指定 demo, 跑完退出。
            if (only != null) {
                System.out.println("\n>>> 运行: [" + only.group + "] " + only.key + ", 开始执行(执行 demo 需要一定时间, 请耐心等待) ...");
                DemoLog.info("DemoEntry", "非交互运行 demo: " + only.key + "(分组: " + only.group + ")");
                try {
                    runEntry(onlyDemo, rt, params, ws, selected, in);
                } catch (Throwable t) {
                    // 含原生 Error：记录后正常收尾 shutdown, 不裸崩。
                    DemoLog.error("DemoEntry", "demo 运行异常: " + Utils.describe(t), t);
                    System.out.println("demo 运行异常: " + Utils.describe(t));
                }

                logBoth("demo 结束, 正在关闭 SDK, 请耐心等待(约 10 秒) ...");
                return;
            }

            // 菜单主循环。
            while (true) {
                printMenu(entries);
                System.out.println("\n  [其它] (*)重新选择设备 r   (*)退出 q");

                String line = Utils.readLine(in, "\n请输入编号, 重选设备(r), 退出(q): ");
                // 别把两种退出触发混为一谈:
                //  - line == null: readLine 遇到 EOF / stdin 关闭 / IOException(Ctrl+D、重定向输入耗尽等),
                //    控制台已无法再交互——不是「用户退出」。混记会误导, 且此后每个 readLine 都立即返回 null,
                //    菜单会「不等输入就跳过并退出」, 正是「控制台没法和用户交互」的现象。
                //  - "q": 用户主动退出。
                if (line == null) {
                    warnBoth(MSG_STDIN_EOF);
                    break;
                }
                if (line.equalsIgnoreCase("q")) {
                    logBoth("用户退出, 正在关闭 SDK, 请稍候(约 10 秒) ...");
                    break;
                }
                if (line.equalsIgnoreCase("r")) {
                    logBoth("重新选择设备 ...");
                    DeviceTopo re = pickDevice(ws, timeoutMs, in);
                    if (re != null) {
                        selected = re;
                    }
                    continue;
                }

                Entry e = parseEntry(entries, line);
                if (e == null) {
                    warnBoth("无效编号: " + line);
                    continue;
                }

                // 提示所选 demo 及分组, 随即开始执行。
                System.out.println("\n>>> 已选择: [" + e.group + "] " + e.key + ", 开始执行(执行 demo 需要一定时间, 请耐心等待) ......");
                DemoLog.info("DemoEntry", "用户选择 demo: " + e.key + "(分组: " + e.group + "), 开始执行");

                // 单个 demo 的任何失败(含原生 Error)都在此隔离: 记录后回菜单, 绝不拖垮整个交互会话。
                try {
                    runEntry(e, rt, params, ws, selected, in);
                } catch (Throwable t) {
                    DemoLog.error("DemoEntry", "demo 运行异常(已隔离, 返回菜单): " + Utils.describe(t), t);
                    System.out.println("demo 运行异常: " + Utils.describe(t));
                }
                // 「按回车返回菜单」若 stdin 已断(返回 null), 立即以 EOF 收尾, 不空转到下一轮再退。
                if (Utils.readLine(in, "\n[demo 结束]  按回车返回菜单 ...") == null) {
                    warnBoth(MSG_STDIN_EOF);
                    break;
                }
            }
        } catch (Throwable ex) {
            // 两种模式共用的最后兜底: SdkRuntime.start / workspace() / pickDevice 等逃逸出来的
            // 原生 Error 也在此接住(catch Throwable, 非 Exception), 至少打印 + 走 try-with-resources 的 shutdown。
            System.out.println("运行失败: " + Utils.describe(ex));
            DemoLog.error("DemoEntry", "会话异常终止: " + Utils.describe(ex), ex);
        }
    }

    // ==================== 双通道日志(日志文件 + 控制台) ====================
    // 入口关键事件统一走这几个 helper: 既落 DemoLog 日志文件留痕, 又打 System.out 让交互用户当场可见,
    // 避免「只落文件用户看不到」或「只打控制台无痕可查」。tag 固定 "DemoEntry"。

    /** INFO 级双通道：日志文件 + 控制台。 */
    private static void logBoth(String msg) {
        DemoLog.info("DemoEntry", msg);
        System.out.println(msg);
    }

    /** WARN 级双通道：日志文件 + 控制台。 */
    private static void warnBoth(String msg) {
        DemoLog.warn("DemoEntry", msg);
        System.out.println(msg);
    }

    /** ERROR 级双通道：日志文件(含异常堆栈) + 控制台(附异常摘要)。 */
    private static void errBoth(String msg, Throwable t) {
        DemoLog.error("DemoEntry", msg, t);
        System.out.println(t != null ? msg + ": " + Utils.describe(t) : msg);
    }

    /** 拉在线设备列表并交互选择, 打印所选设备信息。 */
    private static DeviceTopo pickDevice(Workspace ws, long timeoutMs, BufferedReader in) {
        try {
            List<DeviceTopo> onlineDeviceTopoList = Devices.fetchOnlineDeviceList(ws, timeoutMs);
            DeviceTopo topo = Devices.selectInteractive(onlineDeviceTopoList, in);
            if (topo != null) {
                Devices.printInfo(topo);
            }
            return topo;
        } catch (Throwable ex) {
            // catch Throwable: 设备查询走 JNI, 原生 Error 也要接住, 让选设备失败=返回 null(不崩/不拖垮会话)。
            System.out.println("获取/选择设备失败: " + Utils.describe(ex));
            DemoLog.error("DemoEntry", "获取/选择设备失败: " + Utils.describe(ex), ex);
            return null;
        }
    }

    /**
     * 运行一个 demo(交互菜单路径)：通过工厂新建 demo 实例, 需要设备则新建会话(独立 registry), 跑完退订。
     * 非交互路径请使用 {@link #runEntry(ModuleDemo, SdkRuntime, DemoParams, Workspace, DeviceTopo, BufferedReader)}
     * 以复用已创建的实例, 避免二次实例化。
     */
    private static void runEntry(Entry e, SdkRuntime rt, DemoParams params,
        Workspace ws, DeviceTopo selected, BufferedReader in) {
        runEntry(e.factory.get(), rt, params, ws, selected, in);
    }

    /**
     * 运行一个 demo(复用已创建实例)：供非交互路径调用, 避免「先实例化读 requiresDevice() 再抛弃、
     * 又实例化一次真正运行」的双重创建——若 demo 构造函数未来有副作用(如分配资源), 双重实例化会变成 Bug。
     */
    private static void runEntry(ModuleDemo demo, SdkRuntime rt, DemoParams params,
        Workspace ws, DeviceTopo selected, BufferedReader in) {
        DeviceSession session = demo.requiresDevice() ? DeviceSession.of(ws, selected, params) : null;
        demo.runInSession(rt, params, session, in);
    }

    /** 把用户输入的编号解析成 Entry;非法返回 null。 */
    private static Entry parseEntry(List<Entry> entries, String line) {
        try {
            int idx = Integer.parseInt(line) - 1;
            if (idx < 0 || idx >= entries.size()) {
                return null;
            }
            return entries.get(idx);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** 打印交互菜单：标题 + 按分组归类的编号列表(同组只在首项前打印一次组名)。 */
    private static void printMenu(List<Entry> entries) {
        Utils.printTitle("Flight Hub SDK Demo");

        String lastGroup = "";
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            // 组名变化时打印一次分组头。
            if (!e.group.equals(lastGroup)) {
                Utils.printHeader("[" + e.group + "]");
                lastGroup = e.group;
            }

            System.out.printf("  %3d. %s(%s)%n", i + 1, e.key, e.alias);
        }
    }

    /** 返回注册表中所有 demo 的显示名列表(用于「未找到」时提示可用项)。 */
    private static List<String> names(Map<String, Entry> reg) {
        List<String> lst = new ArrayList<String>();
        for (Entry e : reg.values()) {
            lst.add(e.key);
        }

        return lst;
    }
}
