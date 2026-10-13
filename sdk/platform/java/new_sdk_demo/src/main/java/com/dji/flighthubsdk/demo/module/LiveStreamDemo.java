// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AudienceCountGroup;
import com.dji.flighthubsdk.bean.DeviceAudienceCountItem;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveQualitySettingParams;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.LiveModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 直播 / 码流模块 Demo(菜单驱动)：拉取<b>无人机 + 机场</b>两路码流并各自落盘为 {@code .h264}。
 *
 * <p>按 {@code DrcDemo} 的子菜单范式设计为可交互启停的模块化 demo。子菜单：
 * <ol>
 *   <li><b>拉取码流</b>——自动发现并拉取当前设备的无人机 + 机场可用直播源, 各写一个
 *       {@code <role>_<sn>_<时间戳>.h264} 到<b>输出目录</b>(可配 {@code live.output_dir}, 默认 {@code stream_data});非阻塞, 拉流在后台持续;</li>
 *   <li><b>拉流状态</b>——查看各路已收帧数 / 字节 / 输出文件;</li>
 *   <li><b>设置直播画质</b>——<b>非交互</b>:对已拉起的<b>飞机 / 机场各路</b>循环调用 {@code setLiveQuality},
 *       按固定序列先 0=自适应, <b>设置成功后</b>再 3=高清(档位表: 0=自适应 1=流畅 2=标清 3=高清 4=超清);
 *       每次请求的完整参数与设置结果均详细落<b>日志文件</b>;需先拉流成功, 变更后提示「停止再重拉」以重连生效;</li>
 *   <li><b>停止拉流</b>——按<b>退订 → stop → 关文件</b>的正确顺序停止全部路(避免「先关文件后退订」导致丢帧);</li>
 *   <li><b>获取在线观众数量</b>——接口飞机/机场共有, 联调实测仅<b>机场</b>直播有观众数据, 故本 demo 仅查机场, 需机场在线。</li>
 * </ol>
 *
 * <p><b>交互与日志</b>：拉取码流后<b>非阻塞</b>, 拉流在后台持续写盘。菜单<b>常驻</b>:每轮直接列菜单并读一次
 * 选择(含 {@code q}), 单次输入即生效, 不再有「先回车唤醒再选」的双读把按键吞掉。拉流期间 SDK 控制台日志
 * 照常滚动;实时收帧进度亦可由菜单 <b>[2] 拉流状态</b>按需查看, 每 30 帧另落一条到<b>日志文件</b>
 * (见 {@link StreamPull#onFrame})。
 *
 * <p><b>关键设计要点</b>：
 * <ul>
 *   <li>拉流结束<b>先退订再关文件</b>, 顺序由 {@link StreamPull#stop()} 显式保证, 会话退出 {@code finally} 幂等兜底;</li>
 *   <li>订阅只建立一次, <b>不</b>在 range 回调里重复 start/subscribe(避免模块覆盖与 listenId 泄漏);</li>
 *   <li>发现直播源(range)拿不到才不建文件——<b>不留 0 字节空文件</b>;若拉到 0 字节, 停止时自动删空文件并告警;</li>
 *   <li>选源按<b>本设备 SN 过滤</b>——range(live_capacity)可能包含同拓扑<b>其它设备</b>的源, 而
 *       {@code getLiveStream(info)} 不校验归属(传谁的源就拉谁的流), 盲取第一路会让飞机/机场两路
 *       重复录制<b>同一条流</b>(两文件字节级相同, 实测踩过);无本机源则继续等到超时并告警跳过, 不错拉他机流;</li>
 *   <li>帧回调不每帧 {@code flush()}(缓冲写, 关闭时统一 flush), 且整段 {@code catch(Throwable)} 不阻塞不外抛。</li>
 * </ul>
 *
 * <p>若拉到 0 字节, 通常不是 demo/SDK bug, 而是上游无真实推流(参考 0 字节 RCA), 检查设备是否在推流。
 *
 * <p><b>已知 SDK 限制(非本 demo bug)</b>：SDK {@code LiveModule} 用<b>多线程</b>池派发帧回调
 * (每帧一个 Runnable 投入 {@code newFixedThreadPool(>=2)}), 极端突发下落盘的 {@code .h264} 帧可能<b>乱序</b>
 * (播放器花屏)。demo 侧无法重排——{@code FrameInfo} 仅含 {@code isKeyFrame}, 无帧序号/时间戳可据以排序,
 * 且乱序发生在回调进入本 demo 之前。若需严格有序, 应在 SDK 侧把 {@code LiveModule} 的回调线程池
 * 改为<b>单线程</b>(该池为模块实例级, 由该模块全部订阅共享)。
 */
public class LiveStreamDemo extends ModuleDemo {

    /** 拉流落盘目录(默认值;可被 {@code live.output_dir} 覆盖)。 */
    private static final String DEFAULT_OUTPUT_DIR = "stream_data";
    /** 等待设备上报可用直播源(range)的默认超时(ms)。range 来自 SDK 每 5s 一轮的 live_capacity
     *  轮询, 10s 只有 ~2 轮机会(首轮 topo 拉取慢即超时), 20s 给到 ~4 轮。 */
    private static final long DEFAULT_RANGE_TIMEOUT_MS = 20000L;
    /** 停止单路拉流的 join 上限(ms)：略大于 stop() 内 liveModule.stop() 的 awaitTermination(~5s), 卡死则放弃等待。 */
    private static final long STOP_JOIN_TIMEOUT_MS = 6000L;
    /**
     * 画质档名称表(索引=档位), 沿用上云 API 的 live 画质枚举: 0=自适应 1=流畅 2=标清 3=高清 4=超清。
     * 注意: SDK 内部注释(request_message.h 与 LiveQualitySettingParams bean)另记为"0 自适应; 1 标清; 4 超清",
     * 与上云 API 枚举冲突(pf-live 仓库快照未实现 /stream/set-quality 端点, 后端是否校验该值无从核实), 以设备实际行为为准。
     */
    private static final String[] QUALITY_NAMES = {"自适应", "流畅", "标清", "高清", "超清"};
    /** 非交互画质设置的固定档位序列: 先 0=自适应, 成功后再 3=高清(前一档成功才设下一档)。 */
    private static final int[] QUALITY_SEQUENCE = {0, 3};

    /** 当前活动的拉流路(无人机 / 机场各一)。仅主线程(菜单)访问, 无需同步。 */
    private final List<StreamPull> pulls = new ArrayList<>();

    /** 落盘目录(run 时从配置解析)。 */
    private String outputDir = DEFAULT_OUTPUT_DIR;
    /** range 发现超时(ms)。 */
    private long rangeTimeoutMs = DEFAULT_RANGE_TIMEOUT_MS;
    /** start()/setLiveQuality() 回调超时(ms)。 */
    private long startTimeoutMs;

    @Override
    public String name() {
        return "LiveStreamDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入;正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式直播 demo");
            return;
        }

        // ---- 解析配置 ----
        Node live = params.node("live");
        outputDir = live.str("output_dir", DEFAULT_OUTPUT_DIR);
        rangeTimeoutMs = live.longVal("range_timeout_ms", DEFAULT_RANGE_TIMEOUT_MS);
        startTimeoutMs = timeout("live_stream_ready_timeout_ms");

        // 提前建好输出目录(失败不致命, 拉流时开文件会再暴露具体错误)。
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (Exception e) {
            DemoLog.warn(name(), "创建输出目录失败: " + outputDir + " -> " + Utils.describe(e));
        }
        console("拉流输出目录: " + new File(outputDir).getAbsolutePath());
        DemoLog.info(name(), "payload(drone)=" + payloadIndex() + " outputDir=" + outputDir
            + " rangeTimeout=" + rangeTimeoutMs + "ms");

        try {
            while (true) {
                // 常驻菜单: 每轮直接列菜单并读一次选择(含 q), 单次输入即生效——不再有「先回车唤醒再选」的双读
                // 把一次按键吞掉、显得「敲 q 无响应」的问题。实时收帧进度由 [2] 拉流状态按需查看(每 30 帧亦落日志文件)。
                Utils.printTitle("执行 " + name());
                if (!pulls.isEmpty()) {
                    console("(拉流中：" + pulls.size() + " 路后台写盘中; 选 [2] 看进度、[4] 停止, [5] 观众数量, 或 q 退出)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-6, q 退出): ", 1, 6);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出直播菜单");
                    break;
                }

                // 单个菜单动作失败(含超时、错误码)不退出菜单：记录后回菜单继续。
                // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离。
                try {
                    switch (choice) {
                        case 1:
                            startAll();
                            break;
                        case 2:
                            showStatus();
                            break;
                        case 3:
                            setQuality();
                            break;
                        case 4:
                            stopAll();
                            break;
                        case 5:
                            getAudienceCount();
                            break;
                        case 6:
                            getStreamURL();
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // 退出前收尾：停止全部拉流(会话退出的幂等兜底)。
            if (!pulls.isEmpty()) {
                stopAll();
            }
        }
    }

    // ==================== [1] 拉取码流 (发现→getLiveStream→start→subscribeLiveStream) ====================

    /** 拉取码流：自动拉取当前设备的无人机 + 机场可用直播源(各一路)。非阻塞, 拉流在后台持续。 */
    private void startAll() {
        if (!pulls.isEmpty()) {
            hint("已在拉流中(" + pulls.size() + " 路), 请先选 [4] 停止拉流再重新拉取");
            return;
        }

        int started = 0;
        // 无人机路：按会话匹配到的 payloadIndex 优先选源。
        // 离线即跳过(拓扑快照): 离线设备不可能有直播源, 硬等只会白耗满 range 超时(实测 20s),
        // 且串行阻塞机场路的启动。range 回调只在 live_capacity 变化时触发——源不存在时等待毫无收益。
        if (!session.hasDrone()) {
            DemoLog.info(name(), "当前设备无无人机, 跳过无人机码流");
        } else if (!session.isDroneOnline()) {
            hint("[drone] 无人机离线(选设备时刻的拓扑状态), 跳过拉流;若刚上线请回主菜单 r 重选设备刷新");
        } else {
            try {
                Drone drone = session.drone();
                if (startOne("drone", session.droneSn(), drone.getLiveDeviceModule(), drone, payloadIndex())) {
                    started++;
                }
            } catch (Exception e) {
                DemoLog.error(name(), "拉取无人机码流失败: " + Utils.describe(e), e);
                console("   [drone] 拉流失败: " + Utils.describe(e));
            }
        }

        // 机场路：机场无预设 payloadIndex, 取第一路可用源。离线跳过语义同上。
        if (!session.hasDock()) {
            DemoLog.info(name(), "当前设备无机场, 跳过机场码流");
        } else if (!session.isDockOnline()) {
            hint("[dock] 机场离线(选设备时刻的拓扑状态), 跳过拉流;若刚上线请回主菜单 r 重选设备刷新");
        } else {
            try {
                Dock dock = session.dock();
                if (startOne("dock", session.dockSn(), dock.getLiveDeviceModule(), dock, "")) {
                    started++;
                }
            } catch (Exception e) {
                DemoLog.error(name(), "拉取机场码流失败: " + Utils.describe(e), e);
                console("   [dock] 拉流失败: " + Utils.describe(e));
            }
        }

        if (started == 0) {
            hint("未拉起任何码流(设备无可用直播源, 可能未上报直播能力 live_capacity 或源均属其它设备)。可选 [2] 查看状态或稍后重试");
        } else {
            console("已拉起 " + started + " 路码流, 后台持续写盘;选 [4] 停止, [2] 看状态, [3] 设画质。");
        }
    }

    // ==================== [4] 停止拉流 (退订→stop→关文件) ====================

    /**
     * 停止拉流：<b>并行</b>停止全部路(每路 退订 → stop → 关文件)。
     *
     * <p>单路 {@link StreamPull#stop()} 内 {@code liveModule.stop()} 会 {@code awaitTermination}(最长约 5s)
     * 以 drain 回调线程池;串行停多路会把等待累加(双路最坏 ~10s), 期间无反馈时看起来像「q 后卡死」。故:
     * <ol>
     *   <li>停止<b>前</b>立刻给一条「正在停止…请稍候」提示——<b>日志文件({@link DemoLog}) + 控制台({@link #console})都打</b>,
     *       让用户知道已受理、在收尾;</li>
     *   <li>各路<b>并行</b>停(互不共享资源, {@link StreamPull#stop()} 幂等且各自加锁), 把最坏等待压到单路 ~5s。</li>
     * </ol>
     */
    private void stopAll() {
        if (pulls.isEmpty()) {
            hint("当前没有正在进行的拉流");
            return;
        }
        final int n = pulls.size();
        // 停止前双通道提示(日志文件 + 控制台): stop 可能阻塞数秒, 先告知「正在停止, 请稍候」, 免得被当成无响应。
        DemoLog.info(name(), "开始停止全部 " + n + " 路拉流(并行, 每路最长约 5s)");
        console("\n>> [停止拉流] 正在停止全部 " + n + " 路, 请稍候(最长约 5 秒) ...");

        // 并行停各路: drone / dock 各是独立 LiveModule(独立回调线程池与原生流), 可并发 stop, 等待取最慢一路而非累加。
        // 停止线程设为 daemon 且 join 带超时: 单路 stop() 内 liveModule.stop() 会 awaitTermination(最长 ~5s);
        // 万一某路原生 stop 卡死(如 0 帧 RTC 通道 teardown), 有界 join 后放弃等待并继续, daemon 身份保证它绝不拖住进程退出。
        List<Thread> workers = new ArrayList<>(n);
        for (final StreamPull p : pulls) {
            // p.stop() 内部各步各自 try/catch(Throwable), 不外抛
            Thread t = new Thread(() -> p.stop(), "live-stop-" + p.role);
            t.setDaemon(true);
            t.start();
            workers.add(t);
        }
        for (Thread t : workers) {
            try {
                t.join(STOP_JOIN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                // 保留中断状态并停止继续等待(剩余停止线程为 daemon, 会各自跑完收尾, 不阻塞主线程/退出)。
                Thread.currentThread().interrupt();
                DemoLog.warn(name(), "等待停止线程被中断, 放弃继续等待: " + t.getName());
                break;
            }
            if (t.isAlive()) {
                DemoLog.warn(name(), "停止线程超时未结束(" + STOP_JOIN_TIMEOUT_MS
                    + "ms, 原生 stop 可能卡在 RTC teardown), 放弃等待(daemon, 不拖住退出): " + t.getName());
            }
        }
        pulls.clear();
        DemoLog.info(name(), "全部 " + n + " 路停止流程结束");
        console("   全部已停止。");
    }

    // ==================== [5] 获取在线观众数量 (getAudienceCount) ====================

    /**
     * 获取在线观众数量。
     *
     * <p>接口挂在 {@code LiveDeviceModule} 上(飞机/机场共有, C++ 头注释语义为「查询本设备在线观众数量」,
     * group 参数可选——传 {@code null} 表示不按分组筛选、统计全部观众);
     * 联调实测仅机场直播有观众数据, 故本 demo 仅查机场, 需机场在线。
     * 本 demo 沿用旧版联调约定值 {@code level1Group="web"}(SDK/后端仓库无该分组定义),
     * 观众若挂在其它分组会被漏计。结果落日志文件并回显控制台。
     */
    private void getAudienceCount() {
        if (!session.hasDock()) {
            hint("当前设备无机场, 无法查询在线观众数量");
            return;
        }
        if (!session.isDockOnline()) {
            hint("机场离线, 无法查询在线观众数量;若刚上线请回主菜单 r 重选设备刷新");
            return;
        }
        Dock dock;
        try {
            dock = session.dock();
        } catch (Exception e) {
            DemoLog.warn(name(), "获取机场实例失败: " + Utils.describe(e));
            console("   获取机场实例失败: " + Utils.describe(e));
            return;
        }
        LiveDeviceModule ldm = dock.getLiveDeviceModule();
        if (ldm == null) {
            hint("获取 LiveDeviceModule 失败, 无法查询在线观众数量");
            return;
        }

        AudienceCountGroup group = new AudienceCountGroup();
        group.setLevel1Group("web");
        group.setLevel2Group("");

        console("\n   获取在线观众数量: level1Group=web level2Group=\"\"");
        DemoLog.info(name(), "getAudienceCount: level1Group=web level2Group=\"\" timeout=" + startTimeoutMs + "ms");

        final Await<FlightHubError> await = new Await<>(name() + ".getAudienceCount");
        final AtomicReference<List<DeviceAudienceCountItem>> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取在线观众数量", startTimeoutMs, await, () ->
            ldm.getAudienceCount(group, new FlightHubCallback.GetAudienceCountCallback() {
                @Override
                public void onResult(FlightHubError error, List<DeviceAudienceCountItem> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            List<DeviceAudienceCountItem> items = ref.get();
            int n = items != null ? items.size() : 0;
            DemoLog.info(name(), "getAudienceCount 成功: 共 " + n + " 台设备");
            console("   在线观众数量(共 " + n + " 台设备):");
            if (items != null) {
                for (DeviceAudienceCountItem item : items) {
                    String line = "   sn=" + item.getSn() + ", count=" + item.getCount();
                    DemoLog.info(name(), line);
                    console(line);
                }
            }
        }
    }

    // ==================== [6] 获取拉流地址 (startWithURL) ====================

    /** 获取拉流地址：调用 startWithURL 只获取后端拉流地址, 不自动拉流。 */
    private void getStreamURL() {
        // 优先无人机路, 其次机场路
        String role = null;
        String deviceSn = null;
        LiveDeviceModule ldm = null;
        Object device = null;
        String preferredPayloadIndex = "";

        if (session.hasDrone() && session.isDroneOnline()) {
            role = "drone";
            deviceSn = session.droneSn();
            device = session.drone();
            preferredPayloadIndex = payloadIndex();
            ldm = session.drone().getLiveDeviceModule();
        } else if (session.hasDock() && session.isDockOnline()) {
            role = "dock";
            deviceSn = session.dockSn();
            device = session.dock();
            ldm = session.dock().getLiveDeviceModule();
        }

        if (role == null) {
            hint("当前设备离线或无可用机型, 无法获取拉流地址");
            return;
        }
        if (ldm == null) {
            hint("[" + role + "] 获取直播发现模块失败");
            return;
        }

        // 1) 发现可用直播源
        Discovered d = awaitLiveInfo(role, ldm, deviceSn, preferredPayloadIndex, rangeTimeoutMs);
        if (d == null) {
            hint("[" + role + "] 无可用直播源, 无法获取拉流地址");
            return;
        }

        // 2) 取拉流模块
        LiveModule lm = getLiveStream(device, d.info);
        if (lm == null) {
            DemoLog.warn(name(), "[" + role + "] getLiveStream 返回 null");
            console("   [" + role + "] 获取拉流模块失败");
            d.rangeSub.close();
            registry().remove(d.rangeSub);
            return;
        }

        console("\n   [" + role + "] 获取拉流地址 (startWithURL, 不自动拉流)");
        DemoLog.info(name(), "startWithURL: [" + role + "] sn=" + deviceSn + " timeout=" + startTimeoutMs + "ms");

        // 3) 调用 startWithURL
        final Await<FlightHubError> await = new Await<>(name() + ".startWithURL");
        final AtomicReference<com.dji.flighthubsdk.bean.LiveStreamStartResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取拉流地址", startTimeoutMs, await, () ->
            lm.startWithURL(new FlightHubCallback.LiveStreamStartCallback() {
                @Override
                public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveStreamStartResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            com.dji.flighthubsdk.bean.LiveStreamStartResult result = ref.get();
            if (result != null) {
                String line = "   url_type=" + result.getUrlType()
                    + " url=" + result.getUrl()
                    + " publish_uid=" + result.getPublishUid()
                    + " provider_url=" + result.getProviderUrl()
                    + " expire_ts=" + result.getExpireTs();
                DemoLog.info(name(), "startWithURL 成功: " + line);
                console(line);
            } else {
                console("   startWithURL 返回成功但 result 为空");
            }
        }

        // 收尾 range 监听
        d.rangeSub.close();
        registry().remove(d.rangeSub);
    }

    // ==================== [2] 拉流状态 ====================

    /** 拉流状态：逐路打印帧数 / 字节 / 输出文件。 */
    private void showStatus() {
        if (pulls.isEmpty()) {
            hint("当前没有正在进行的拉流(选 [1] 开始拉取)");
            return;
        }
        console("\n>> [拉流状态] 共 " + pulls.size() + " 路：");
        for (StreamPull p : pulls) {
            console("   " + p.status());
        }
    }

    // ==================== [3] 设置直播画质 (setLiveQuality) ====================

    /**
     * 设置直播画质(<b>非交互</b>)：对每一路已拉起的直播(飞机 / 机场)循环执行固定档位序列
     * {@link #QUALITY_SEQUENCE}——先 0=自适应, <b>设置成功后</b>再 3=高清;某档失败则中止该路的
     * 后续档位(下一路照常执行)。每次 {@code setLiveQuality} 的<b>完整请求参数与设置结果</b>
     * (成功 / 失败原因)都详细记录到日志文件({@link DemoLog}), 控制台仅回显摘要。
     */
    private void setQuality() {
        if (pulls.isEmpty()) {
            hint("尚未拉流, 请先选 [1] 拉取码流(启动成功后)再设置画质");
            return;
        }
        console("\n>> [设置画质] 共 " + pulls.size() + " 路, 每路依次设置: "
            + qualitySequenceDesc() + " (详细过程见日志文件)");
        for (StreamPull p : pulls) {
            setQualitySequence(p);
        }
        // 画质切换需重新协商码流才生效：提示用户停止再重拉。
        hint("画质变更通常需重连生效: 建议选 [4] 停止拉流, 再选 [1] 重新拉取码流");
    }

    /**
     * 对单路直播按 {@link #QUALITY_SEQUENCE} 依次设置画质;<b>前一档成功才设下一档</b>。
     * 每档的请求调用(完整参数 + 超时)与设置结果(成功 / 失败及原因)逐条落日志文件。
     */
    private void setQualitySequence(final StreamPull p) {
        final String role = roleCn(p.role);
        for (int q : QUALITY_SEQUENCE) {
            final String desc = "videoQuality=" + q + "(" + qualityName(q) + ")";
            final LiveQualitySettingParams qp = buildQualityParams(p, q);
            // 请求调用留痕：完整参数落日志文件。
            DemoLog.info(name(), "[" + p.role + "] setLiveQuality 请求: sn=" + qp.getSn()
                + " camera=" + qp.getCamera() + " video=" + qp.getVideo()
                + " " + desc + " timeout=" + startTimeoutMs + "ms");
            console("   [" + role + "] 设置画质 " + desc + " ...");
            try {
                call("设置" + role + "直播画质(" + desc + ")", cb -> p.liveModule.setLiveQuality(qp, cb), startTimeoutMs);
                // 设置结果留痕：成功。
                DemoLog.info(name(), "[" + p.role + "] setLiveQuality 结果: 成功 " + desc);
                console("   [" + role + "] 设置画质 " + desc + " 成功");
            } catch (Exception e) {
                // 设置结果留痕：失败(含超时/错误码)。序列语义=前一档成功才设下一档, 故中止该路。
                DemoLog.error(name(), "[" + p.role + "] setLiveQuality 结果: 失败 " + desc
                    + " -> " + Utils.describe(e), e);
                console("   [" + role + "] 设置画质 " + desc + " 失败: " + Utils.describe(e)
                    + " (中止该路后续档位)");
                break;
            }
        }
    }

    // ==================== 拉流实现 ====================

    /**
     * 拉起单路码流：发现可用直播源 → getLiveStream → 开文件 → start(同步) → subscribe。
     * 启动之后的任何失败(含 start 超时/竞态、订阅失败、Error)一律经 {@link StreamPull#stop()} 收尾, 不留孤儿流。
     *
     * @return true 表示这一路成功拉起并加入 {@link #pulls}
     */
    private boolean startOne(String role, String deviceSn, LiveDeviceModule ldm,
            Object device, String preferredPayloadIndex) {
        if (ldm == null) {
            DemoLog.warn(name(), "[" + role + "] 获取 LiveDeviceModule 失败, 跳过");
            console("   [" + role + "] 获取直播发现模块失败, 跳过");
            return false;
        }

        // 1) 发现可用直播源(一次性等待;按本设备 SN 过滤, 防止错选同拓扑其它设备的源)。
        Discovered d = awaitLiveInfo(role, ldm, deviceSn, preferredPayloadIndex, rangeTimeoutMs);
        if (d == null) {
            hint("[" + role + "] 无本设备可用直播源(可能设备未上报直播能力 live_capacity), 跳过");
            return false;
        }

        // 2) 取拉流模块(按 payloadIndex 缓存)。尚未启动, 失败仅收尾 range。
        LiveModule lm = getLiveStream(device, d.info);
        if (lm == null) {
            DemoLog.warn(name(), "[" + role + "] getLiveStream 返回 null, 跳过");
            console("   [" + role + "] 获取拉流模块失败, 跳过");
            d.rangeSub.close();
            registry().remove(d.rangeSub);
            return false;
        }

        // 3) 先建输出文件(此时尚未启动直播, 失败不涉及已开流)。
        File file = new File(outputDir, buildFileName(role, deviceSn));
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            DemoLog.error(name(), "[" + role + "] 打开输出文件失败: " + Utils.describe(e), e);
            console("   [" + role + "] 打开输出文件失败: " + Utils.describe(e));
            d.rangeSub.close();
            registry().remove(d.rangeSub);
            return false;
        }

        // 先构造 StreamPull, 使「启动之后」的任何失败都能统一经 pull.stop() 收尾
        // (退订 → stop → 关文件[空文件自动删] → 收尾 range), 杜绝「已启动却无句柄可停」的孤儿流。
        final StreamPull pull = new StreamPull(role, d.info, lm, out, file, d.rangeSub);
        try {
            // 4) 启动直播(同步等待结果 + 校验错误码)。
            call("启动直播(" + role + ")", cb -> pull.liveModule.start(cb), startTimeoutMs);

            // 5) 订阅码流(每帧回调写盘)。
            int listenId = pull.liveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                @Override
                public void onResult(byte[] data, FrameInfo frameInfo) {
                    pull.onFrame(data);
                }
            });

            if (listenId == -1) {
                DemoLog.warn(name(), "[" + role + "] subscribeLiveStream 返回 -1, 订阅失败");
                console("   [" + role + "] 订阅码流失败");
                pull.stop();   // 退订(空) + stop 已启动的 module + 关/删空文件 + 收尾 range
                return false;
            }

            pull.streamSub = Subscription.of(role + "LiveStream", listenId,
                id -> pull.liveModule.unSubscribeLiveStream(id));
            // 登记会话 registry 兜底(close 幂等): 主动停止路径由 stop() 关闭并移除。
            registry().add(pull.streamSub);
        } catch (Throwable t) {
            // 关键收尾：call() 超时抛出时, start() 可能已在 SDK 侧把流拉起(回调晚于超时的竞态);
            // 订阅阶段亦可能抛 Error。一律 pull.stop() 停掉已启动的流并关/删文件, 再原样上抛——
            // 断开本端 RTC 链路并释放 SDK 回调线程池, 避免泄漏(stop 仅本端断链, 不调用服务端停播 API,
            // 设备侧是否停推由后端决定)。Exception 交 startAll 逐路 catch 记录; Error 交基类 runBody 兜底。
            pull.stop();
            if (t instanceof Error) {
                throw (Error) t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            // try 内无受检异常, 理论不可达;兜底包装以免方法签名需 throws。
            throw new FlightHubException("[" + role + "] 拉流启动失败: " + Utils.describe(t), t);
        }

        pulls.add(pull);
        console("   [" + role + "] 拉流已启动 -> " + file.getAbsolutePath()
            + " (源sn=" + d.info.getDeviceSn() + ", payload=" + d.info.getPayloadIndex()
            + ", video_id=" + d.info.getVideoId() + ")");

        DemoLog.info(name(), "[" + role + "] 拉流启动: sn=" + deviceSn
            + " source_sn=" + d.info.getDeviceSn()
            + " payload=" + d.info.getPayloadIndex() + " video_id=" + d.info.getVideoId()
            + " -> " + file.getAbsolutePath());

        return true;
    }

    /**
     * 一次性等待设备上报<b>本设备的</b>可用直播源(range)。注册监听后, 首个包含可选中源
     * (经 {@link #pickLiveInfo} 按 {@code ownSn} 归属过滤)的列表即返回;仅有其它设备的源时
     * 告警并<b>继续等待</b>(而非错选), 直到超时。超时 / 监听失败(id=-1) 返回 null。
     * 返回时 range 监听<b>仍开着</b>(交由调用方在停止时收尾), 便于观察源变化。
     */
    private Discovered awaitLiveInfo(final String role, LiveDeviceModule ldm,
            final String ownSn, final String preferred, long timeoutMs) {
        final Await<LiveInfo> await = new Await<>(name() + ".listenLiveAvailableRange#" + role);
        int id = ldm.listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
            @Override
            public void onResult(List<LiveInfo> liveInfos) {
                // ---- 选源过程 DEBUG 留痕(只写日志文件, 不打控制台)：每次 range 回调——包括被忽略的——
                // 都留痕, 便于复盘「回调来没来 / 上报了什么 / 为什么选了这路(或没选)」。
                // 已选定后 range 变化照常回调(监听保持到停止), 不再重复选源。
                if (await.isDone()) {
                    DemoLog.debug(name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] range 回调(已选定, 忽略): "
                        + (liveInfos == null ? "null" : liveInfos.size() + " 路"));
                    return;
                }
                if (liveInfos == null || liveInfos.isEmpty()) {
                    DemoLog.debug(name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] range 回调: 空列表, 继续等待");
                    return;
                }

                // 选源前把本次上报的原始 LiveInfo 列表全量落 debug 日志(toString 含
                // describeLiveInfos 不打的 switchableLensTypeSwigValues), 供排查选源/归属问题。
                DemoLog.debug(
                    name(),
                    "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] range 上报 " + liveInfos.size()
                        + " 路 LiveInfo: " + liveInfos
                        + " (ownSn=" + ownSn + ", preferredPayload="
                        + ((preferred == null || preferred.isEmpty()) ? "(无)" : preferred) + ")"
                );

                LiveInfo picked = pickLiveInfo(liveInfos, ownSn, preferred);
                if (picked == null) {
                    // 全部属于其它设备：宁可继续等(直到超时跳过), 也不错拉他机流——
                    // 否则飞机/机场两路会录成同一条流(两文件字节级相同)。
                    DemoLog.warn(
                        name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] 上报的 " + liveInfos.size()
                            + " 路直播源均不属于本设备(sn=" + ownSn + "): " + describeLiveInfos(liveInfos)
                            + ", 继续等待本机源"
                    );
                    return;
                }

                // 判定依据留痕：命中的是哪个优先级档(本机SN+payload / 本机SN首路 / sn为空兜底)。
                boolean ownMatch = ownSn != null && ownSn.equals(picked.getDeviceSn());
                boolean payloadMatch = preferred != null && !preferred.isEmpty()
                    && preferred.equals(picked.getPayloadIndex());
                DemoLog.debug(
                    name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] 选源判定: "
                        + (ownMatch ? (payloadMatch ? "本机SN+payload 命中" : "本机SN 首路") : "sn 为空兜底")
                        + " -> sn=" + picked.getDeviceSn() + " payload=" + picked.getPayloadIndex()
                        + " video_id=" + picked.getVideoId()
                );
                if (picked.getDeviceSn() == null || picked.getDeviceSn().isEmpty()) {
                    DemoLog.warn(
                        name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] 选中源未携带 deviceSn(无法验证归属), 兜底采用: payload="
                            + picked.getPayloadIndex() + " video_id=" + picked.getVideoId()
                    );
                }

                // 「选中」日志只在本结果真正生效(首个 set)后打——超时边界上迟到的回调 set 会被忽略,
                // 若照打日志会留下「先说跳过、后说选中」的矛盾记录, 误导排障。
                if (await.set(picked)) {
                    DemoLog.info(
                        name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] 可用直播源 " + liveInfos.size() + " 路: "
                            + describeLiveInfos(liveInfos) + "; 选中 sn=" + picked.getDeviceSn()
                            + " payload=" + picked.getPayloadIndex() + " video_id=" + picked.getVideoId()
                    );
                } else {
                    DemoLog.debug(
                        name(), "listenLiveAvailableRange() #List<LiveInfo>, [" + role + "] 选源结果迟到(等待方已超时放弃), 忽略: sn="
                            + picked.getDeviceSn() + " payload=" + picked.getPayloadIndex()
                    );
                }
            }
        });
        if (id == -1) {
            DemoLog.warn(name(), "[" + role + "] listenLiveAvailableRange 返回 -1, 无法发现直播源");
            return null;
        }

        DemoLog.debug(
            name(), "[" + role + "] 开始等待本设备直播源: listenId=" + id + " ownSn=" + ownSn
                + " preferredPayload=" + ((preferred == null || preferred.isEmpty()) ? "(无)" : preferred)
                + " 超时=" + timeoutMs + "ms"
        );

        Subscription rangeSub = Subscription.of(role + "LiveRange", id,
            cid -> ldm.cancelListenLiveAvailableRange(cid));
        // 登记会话 registry(close 幂等, 双保险): startOne 后续步骤(如 getLiveStream)抛异常时
        // 由会话收尾兜底退订, 防 range 监听泄漏至进程结束。
        registry().add(rangeSub);

        LiveInfo picked;
        try {
            picked = await.await(timeoutMs);
        } catch (FlightHubException e) {
            // 超时 = 该设备当前无(本机的)可用直播源。
            DemoLog.info(name(), "[" + role + "] 等待本设备可用直播源超时(" + timeoutMs + "ms): " + Utils.describe(e));
            rangeSub.close();
            registry().remove(rangeSub);
            return null;
        }

        return new Discovered(picked, rangeSub);
    }

    /**
     * 从可用源列表挑一路<b>属于本设备</b>的源。优先级：
     * <ol>
     *   <li>sn 匹配 {@code ownSn} 且 payloadIndex 匹配 {@code preferred};</li>
     *   <li>sn 匹配 {@code ownSn} 的第一路;</li>
     *   <li>sn 为空(上报未带归属, 无法验证)的 payload 匹配路 / 第一路——兜底采用(调用方告警);</li>
     *   <li>全部明确属于其它设备 → {@code null}(调用方继续等待或跳过)。</li>
     * </ol>
     */
    private LiveInfo pickLiveInfo(List<LiveInfo> infos, String ownSn, String preferred) {
        if (infos == null || infos.isEmpty()) {
            return null;
        }

        LiveInfo ownPayload = null;
        LiveInfo ownFirst = null;
        LiveInfo unknownPayload = null;
        LiveInfo unknownFirst = null;
        boolean wantPayload = preferred != null && !preferred.isEmpty();

        for (LiveInfo li : infos) {
            if (li == null) {
                continue;
            }
            String sn = li.getDeviceSn();
            boolean payloadMatch = wantPayload && preferred.equals(li.getPayloadIndex());
            if (ownSn != null && ownSn.equals(sn)) {
                if (payloadMatch && ownPayload == null) {
                    ownPayload = li;
                }
                if (ownFirst == null) {
                    ownFirst = li;
                }
            } else if (sn == null || sn.isEmpty()) {
                if (payloadMatch && unknownPayload == null) {
                    unknownPayload = li;
                }
                if (unknownFirst == null) {
                    unknownFirst = li;
                }
            }
            // 明确属于其它设备的源：直接忽略(errant pick 会两路录成同一条流)。
        }
        if (ownPayload != null) {
            return ownPayload;
        }
        if (ownFirst != null) {
            return ownFirst;
        }
        return unknownPayload != null ? unknownPayload : unknownFirst;
    }

    /** 候选源列表的人读描述："sn/payload/video_id" 分号分隔(用于选源/告警日志)。 */
    private static String describeLiveInfos(List<LiveInfo> infos) {
        StringBuilder sb = new StringBuilder();
        for (LiveInfo li : infos) {
            if (li == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(li.getDeviceSn()).append('/').append(li.getPayloadIndex()).append('/').append(li.getVideoId());
        }
        return sb.toString();
    }

    /** 按角色分派 getLiveStream(Drone / Dock)。 */
    private LiveModule getLiveStream(Object device, LiveInfo info) {
        if (device instanceof Drone) {
            return ((Drone) device).getLiveStream(info);
        }
        if (device instanceof Dock) {
            return ((Dock) device).getLiveStream(info);
        }
        return null;
    }

    /** 构造画质设置参数(sn / camera=payloadIndex / video=videoId / videoQuality)。videoId 为空时回退 {@code normal-0}。 */
    private LiveQualitySettingParams buildQualityParams(StreamPull p, int quality) {
        LiveQualitySettingParams qp = new LiveQualitySettingParams();
        qp.setSn(p.info.getDeviceSn());
        qp.setCamera(p.info.getPayloadIndex());
        String video = p.info.getVideoId();
        qp.setVideo((video != null && !video.isEmpty()) ? video : "normal-0");
        qp.setVideoQuality(quality);
        return qp;
    }

    /** 文件名：{@code <role>_<sn>_<yyyyMMdd_HHmmss_SSS>.h264}(毫秒粒度, 避免同秒二次拉取覆盖上一份录制)。 */
    private String buildFileName(String role, String deviceSn) {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String sn = (deviceSn != null && !deviceSn.isEmpty()) ? deviceSn : "unknown";
        return role + "_" + sn + "_" + ts + ".h264";
    }

    // ==================== 辅助方法 ====================

    /** 角色中文名(用于画质回显/日志)：dock→机场, 其余→飞机。 */
    private static String roleCn(String role) {
        return "dock".equals(role) ? "机场" : "飞机";
    }

    /** 画质档名称(0=自适应 1=流畅 2=标清 3=高清 4=超清);越界返回 "未知"。 */
    private static String qualityName(int q) {
        return (q >= 0 && q < QUALITY_NAMES.length) ? QUALITY_NAMES[q] : "未知";
    }

    /** 档位序列的人读描述, 如 {@code 0(自适应) → 3(高清)}(用于控制台/菜单回显)。 */
    private static String qualitySequenceDesc() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < QUALITY_SEQUENCE.length; i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(QUALITY_SEQUENCE[i]).append('(').append(qualityName(QUALITY_SEQUENCE[i])).append(')');
        }
        return sb.toString();
    }

    /** 纯控制台输出(不落日志文件), 用于菜单文案与结果回显——DemoLog 只落日志文件, 交互终端看不到。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足、取消等需留痕的提示。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：[1][4] 为组合流程(发现→getLiveStream→start→subscribeLiveStream / 退订→stop→关文件),
     * [3][5] 对应 setLiveQuality / getAudienceCount, [2] 为本地状态查看。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 直播 / 码流 (Live Stream) ===\n"
            + "  1. 拉取码流 (无人机 + 机场可用码流, 各落一个 .h264 到输出目录)\n"
            + "  2. 拉流状态 (各路帧数 / 字节 / 输出文件)\n"
            + "  3. 设置画质 (非交互: 各路依次 setLiveQuality 0=自适应 → 成功后 3=高清; 需先拉流成功, 过程详见日志文件)\n"
            + "  4. 停止拉流 (退订 → stop → 关文件)\n"
            + "  5. 获取在线观众数量 (机场; level1=web, level2=\"\")\n"
            + "  6. 获取拉流地址 (startWithURL: 只获取后端拉流地址, 不自动拉流)\n"
            + "  q. 返回上级菜单\n";
    }

    // ==================== 内部类型 ====================

    /** 发现结果：选中的直播源 + 仍开着的 range 监听句柄。 */
    private static final class Discovered {
        final LiveInfo info;
        final Subscription rangeSub;

        Discovered(LiveInfo info, Subscription rangeSub) {
            this.info = info;
            this.rangeSub = rangeSub;
        }
    }

    /**
     * 一路拉流的运行态 + 资源集合(无人机 / 机场各一)。封装帧写盘与逆序清理。
     *
     * <p><b>线程</b>：{@link #onFrame} 在 SDK 回调线程调用; {@link #stop} 通常在 {@link #stopAll} 的
     * 并行停止线程({@code live-stop-<role>}, daemon)上调用, 仅 {@link #startOne} 启动失败的收尾路径
     * 在主线程(菜单)调用; 各调用方对 {@link #out}/{@link #closed} 的访问都在 {@link #lock} 上串行,
     * 杜绝「写已关闭的流」。
     */
    private final class StreamPull {
        final String role;
        final LiveInfo info;
        final LiveModule liveModule;
        final Subscription rangeSub;
        private final OutputStream out;
        private final File file;
        private final Object lock = new Object();

        /** 订阅句柄：subscribe 成功后由 {@link #startOne} 赋值(在加入 pulls 之前)。 */
        volatile Subscription streamSub;

        private final AtomicLong frames = new AtomicLong();
        private final AtomicLong bytes = new AtomicLong();
        private volatile boolean closed;

        StreamPull(String role, LiveInfo info, LiveModule liveModule,
                OutputStream out, File file, Subscription rangeSub) {
            this.role = role;
            this.info = info;
            this.liveModule = liveModule;
            this.out = out;
            this.file = file;
            this.rangeSub = rangeSub;
        }

        /**
         * 每帧回调(SDK 线程)：缓冲写盘 + 每 30 帧记一次收流进度到日志文件。
         * 空帧忽略;已关闭忽略;任何异常只告警不外抛、不阻塞。
         *
         * <p>进度日志走 {@link DemoLog}(只落<b>日志文件</b>, 不打控制台), 记录形如
         * 「收到直播流数据 #N: X bytes(总计 Y)」的进度信息; 既留下拉流是否真在收帧的
         * 落盘证据, 又不淹没交互菜单(拉流期控制台留给 SDK 滚动日志与菜单)。
         */
        void onFrame(byte[] data) {
            if (data == null || data.length == 0) {
                return;
            }
            try {
                synchronized (lock) {
                    if (closed) {
                        return;
                    }
                    out.write(data);
                }
                long n = frames.incrementAndGet();
                long total = bytes.addAndGet(data.length);
                // 用原子自增的返回值判周期, 多线程回调下计数不丢;total 为近似累计(仅进度用, 不苛求精确)。
                if (n % 30 == 0) {
                    DemoLog.info(
                        name(), "[" + role + "] 收到直播流数据 #" + n + ": "
                            + data.length + " bytes (总计: " + total + " bytes)"
                    );
                }
            } catch (Throwable t) {
                DemoLog.warn(name(), "[" + role + "] 写码流异常: " + Utils.describe(t));
            }
        }

        /**
         * 停止本路(幂等)。顺序：<b>退订 → stop → flush+关文件 → 收尾 range</b>。
         * stop() 会尝试 drain 回调线程池(awaitTermination 上限约 5s); 常态下返回后不再有帧回调,
         * 但超时/中断路径只 shutdownNow 不等待在途任务, 此时 onFrame 仍可能并发——真正兜底的是
         * closed 标志 + lock。拉到 0 字节则删除空文件并告警。各步各自 try/catch(Throwable), 一步失败不影响后续。
         */
        void stop() {
            synchronized (lock) {
                if (closed) {
                    return;
                }
                closed = true;
            }

            // 1) 退订(停止新帧回调), 并移除 registry 死引用。
            if (streamSub != null) {
                streamSub.close();
                registry().remove(streamSub);
            }
            // 2) 停止拉流模块(内部 shutdown 回调线程池并等待其 drain)。
            try {
                liveModule.stop();
            } catch (Throwable t) {
                DemoLog.warn(name(), "[" + role + "] stop LiveModule 异常: " + Utils.describe(t));
            }
            // 3) flush + 关文件(常态下回调已 drain; drain 超时路径可能仍有在途 onFrame, 靠持锁 + closed 标志保证无竞态)。
            try {
                synchronized (lock) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t) {
                DemoLog.warn(name(), "[" + role + "] 关闭输出文件异常: " + Utils.describe(t));
            }
            // 4) 收尾 range 监听, 并移除 registry 死引用。
            if (rangeSub != null) {
                rangeSub.close();
                registry().remove(rangeSub);
            }

            long f = frames.get();
            long b = bytes.get();
            // 5) 0 字节 = 无真实推流：删空文件, 避免磁盘残留。
            if (b == 0) {
                boolean deleted = false;
                try {
                    deleted = file.delete();
                } catch (Throwable ignore) {
                    // 删除失败无所谓, 下面照常告警。
                }
                DemoLog.warn(name(), "[" + role + "] 0 帧 / 0 字节(启动失败或上游无真实推流, 非 demo bug), 空文件已"
                    + (deleted ? "删除" : "保留") + ": " + file.getName());
                console("   [" + role + "] 停止: 0 帧 / 0 字节 —— 未写入数据(启动失败或上游无推流), 空文件已"
                    + (deleted ? "删除" : "保留"));
            } else {
                DemoLog.info(name(), "[" + role + "] 停止: 帧数=" + f + " 字节=" + b
                    + String.format(" (%.2f MB) -> ", b / 1024.0 / 1024.0) + file.getAbsolutePath());
                console("   [" + role + "] 停止: 帧数=" + f + " 字节=" + b
                    + String.format(" (%.2f MB) -> ", b / 1024.0 / 1024.0) + file.getName());
            }
        }

        /** 状态行(供「拉流状态」打印)。 */
        String status() {
            return "[" + role + "] sn=" + info.getDeviceSn()
                + " payload=" + info.getPayloadIndex()
                + " 帧数=" + frames.get() + " 字节=" + bytes.get()
                + " 运行=" + (!closed)
                + " -> " + file.getName();
        }
    }
}
