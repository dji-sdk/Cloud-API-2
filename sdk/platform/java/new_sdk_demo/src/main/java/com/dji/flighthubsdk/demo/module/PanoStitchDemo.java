// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.bean.GetPanoTierTileUrlResult;
import com.dji.flighthubsdk.bean.LiveVideoInfo;
import com.dji.flighthubsdk.bean.MediaFileDeviceUpload;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.PanoStitchProgressMsg;
import com.dji.flighthubsdk.bean.PanoramaParam;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.SwitchCameraModeParams;
import com.dji.flighthubsdk.bean.SwitchLiveLensParams;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.value.CameraLensType;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 全景拼接 Demo
 *
 * <pre>
 * 主菜单
 * ├── [1] 全景拍照            等待负载列表推送 → 获取负载控制权(若未获取) → 建立 DRC 链路(若未连接)
 * │                            → 切模式(PANO_PHOTO, 幂等) → 切镜头(1X/3X, 幂等) → drcStartPanorama
 * │                            (切模式/切镜头前均先判断当前状态, 已是目标值则跳过; 切模式后 settle
 * │                            2500ms、切镜头后 settle 1500ms, 对齐驾驶舱页面的节流/幂等行为)
 * ├── [2] 停止全景拍照        drcStopPanorama(未建控制权/DRC 链路时自动建, 与 [1] 幂等复用)
 * ├── [3] 开始全景拼接        startPanoStitch(fileId, 全景照片目录)
 * ├── [4] 停止全景拼接        stopPanoStitch(fileId, 全景成片, 仅 state=1/2 可停)
 * ├── [5] 重试全景拼接        retryPanoStitch(fileId, 全景成片, 用于 state=4/5 时重试)
 * ├── [6] 查询 3X 全景成片列表 getMediaFilesList + 客户端过滤 file_name 含 _3X
 * ├── [7] 获取全景瓦片 URL     getPanoTierTileUrl(fileId), 返回 baseUrl + queryParams
 * ├── [8] 获取媒体下载地址     getMediaFilesDownloadURL(fileId), 用于下载原图/中间文件
 * ├── [9] 订阅媒体上传进度    listenMediaFileDeviceUploadMessage(deviceSn), 无百分比, 单文件完成通知
 * ├── [10] 订阅全景拼接进度   subscribePanoStitchProgress(), 实时百分比, 按回车结束订阅
 * └── [11] 订阅全景拍照进度   listenDrcPanoramaStatus(payloadIndex), 拍摄阶段百分比, 按回车结束订阅
 * </pre>
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>[1] 顺序与延迟均参照 {@code DrcPanoScenario} 的实测可用流程：负载控制权必须先于 DRC
 *       建链获取，且获取前必须等待 {@code payloads_settle_ms}——云端通知服务的"订阅立即推送"
 *       是异步 WebSocket(有 100~500ms 延迟)，若立即调 acquirePayloadControlAuthority 会因
 *       camera_abs_map_ 未填充而 ERROR_INVALID_STATE；DRC 链路与负载控制权均带幂等判断
 *       (已连接/已获取时自动跳过)；DRC 连接成功后会自动订阅全景拍照状态
 *       (listenDrcPanoramaStatus)；下发 drcStartPanorama 后会实时打印拍摄进度(percent 0~100)
 *       并阻塞等待拍摄完成(或超时)，让测试人员在同一个菜单动作内看到完整拍摄过程；</li>
 *   <li>[3] 触发拼接需要一个"全景照片目录" file_id(pano_total_count &gt; 0 的目录，
 *       非单张照片、非成片)；成功后服务端异步执行拼接，不会同步返回成片 file_id，
 *       需人工查后端接口或 WS 订阅确认结果；</li>
 *   <li>[4] 仅当成片 state=1(等待资源)/2(处理中) 时允许停止，其它状态服务端会拒绝；</li>
 *   <li>DRC 链路与负载控制权在 demo 退出时统一收尾(断开/释放)，[2] 仅停止拍照指令本身，
 *       不拆链路——同一 DRC 会话内可反复 [1]/[2] 多次测试；</li>
 *   <li>相机模式(PANO_PHOTO)与镜头(1X/3X) 的切换逻辑与驾驶舱页面(uranus-cockpit-app
 *       LiveCameraControlPanel.vue)对齐：切换前先查当前状态(来自 listenDrcCameraState /
 *       listenDrcLiveVideoInfo 推送)，已是目标值则跳过(幂等，同页面 cameraModeSelect/
 *       onSetUltraHdPanorama 的判断)；切模式下发成功后 settle {@code mode_settle_ms}(默认
 *       2500ms，对齐页面 cameraTypeChanging 锁定时长)，切镜头下发成功后 settle
 *       {@code lens_settle_ms}(默认 1500ms，页面无显式延迟但用户点击天然有间隔，这里补一个
 *       保守值)。此前 SDK demo 连续调用(0 延迟)曾导致设备返回 deviceCode=327009——本质是
 *       "发太快，设备状态还没切完就收到下一条指令"，而非协议字段错误。</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.drone()} 拿 {@code ControlAuthModule}/{@code DrcModule}/
 * {@code CameraControlModule}；{@code session.workspace().getMediaServiceModule()} 拿拼接接口。
 *
 * <p>链路稳定延迟/负载列表推送等待/切模式&切镜头 settle 延迟/镜头类型/拍摄超时取自
 * {@code demo_params.json} 的 {@code pano_stitch_demo} 分组：
 * {@code link_settle_ms}(DRC 建链后等待稳定的延迟，默认 3000)、
 * {@code payloads_settle_ms}(获取负载控制权前等待负载列表推送的延迟，默认 2000)、
 * {@code mode_settle_ms}(切模式下发成功后等待设备状态收敛的延迟，默认 2500，对齐驾驶舱页面
 * cameraTypeChanging 锁定时长)、
 * {@code lens_settle_ms}(切镜头下发成功后等待设备状态收敛的延迟，默认 1500)、
 * {@code lens_type}(目标镜头，1=WIDE 1X 球形，2=ZOOM 3X 超清，默认 2)、
 * {@code pano_shoot_timeout_ms}(等待拍摄完成 percent=100 的超时，默认 300000，5 分钟)。
 */
public class PanoStitchDemo extends ModuleDemo {

    /** DRC 链路是否已由本 demo 建立(用于幂等跳过与退出收尾)。 */
    private boolean drcConnected = false;
    /** 负载控制权是否已由本 demo 获取(用于幂等跳过与退出收尾)。 */
    private boolean payloadAuthAcquired = false;
    /** 全景拍照状态订阅句柄(DRC 连接成功后自动建立, demo 结束自动收尾)。 */
    private Subscription panoStatusSub;
    /**
     * 相机状态订阅句柄(DRC 连接成功后自动建立, demo 结束自动收尾)。
     *
     * <p>用于实时跟踪相机当前 cameraMode——设备侧相机模式可能被"驾驶舱页面手动切换"等
     * DRC 之外的渠道改变；若不感知直接下发 drcSwitchCameraMode(PANO_PHOTO)，设备可能因
     * 状态冲突拒绝该指令(deviceCode=327009)。见 [3] 切模式前的幂等判断。
     */
    private Subscription cameraStateSub;
    /**
     * 最近一次收到的相机模式(见 {@link CameraMode})，-1 表示尚未收到推送(未知)。
     * 由 {@link #cameraStateSub} 的回调更新，[1] 切相机模式前据此判断是否需要真的下发切换。
     */
    private final AtomicInteger currentCameraMode = new AtomicInteger(-1);
    /**
     * 直播镜头信息订阅句柄(DRC 连接成功后自动建立, demo 结束自动收尾)。
     *
     * <p>对应驾驶舱页面的 drc_live_video_info_push, 用于实时跟踪当前镜头(1X/3X/IR 等)——
     * 设备侧镜头可能被"驾驶舱页面手动切换"改变；若不感知直接下发 drcSwitchLiveLens，
     * 设备可能因状态冲突拒绝该指令(deviceCode=327009)。见 [1] 切镜头前的幂等判断。
     */
    private Subscription liveVideoInfoSub;
    /**
     * 最近一次收到的镜头类型 videoTypeSwig(见 {@link com.dji.flighthubsdk.value.CameraLensType}),
     * -1 表示尚未收到推送(未知)。由 {@link #liveVideoInfoSub} 的回调更新，[1] 切镜头前据此判断
     * 是否需要真的下发切换。
     */
    private final AtomicInteger currentCameraLens = new AtomicInteger(-1);
    /**
     * 当前拍摄轮次的完成信号：[1] 每次下发 drcStartPanorama 前重置为一个新 latch，
     * 持久订阅的回调在收到 percent&gt;=100 时对其 countDown；未在拍摄中时为 {@code null}，
     * 此时收到的推送(理论上不应发生)只打印不触发任何等待。
     */
    private final AtomicReference<CountDownLatch> shootLatchRef = new AtomicReference<>();

    @Override
    public String name() {
        return "PanoStitchDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式全景拼接 demo");
            return;
        }
        if (!session.hasDrone()) {
            DemoLog.warn(name(), "该设备没有无人机, 无法运行全景拼接 demo(全景拍照依赖 DRC/负载控制)");
            console("   该设备没有无人机, 无法运行全景拼接 demo");
            return;
        }

        final Drone drone;
        try {
            drone = session.drone();
        } catch (Exception e) {
            DemoLog.warn(name(), "获取无人机实例失败: " + Utils.describe(e));
            return;
        }
        if (drone == null) {
            DemoLog.warn(name(), "session.drone() 返回 null");
            return;
        }

        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final CameraControlModule camera = drone.getCameraControlModule();
        final MediaServiceModule media = session.workspace().getMediaServiceModule();
        if (auth == null || drc == null || camera == null || media == null) {
            console("   获取模块失败: auth=" + (auth != null) + " drc=" + (drc != null)
                + " camera=" + (camera != null) + " media=" + (media != null));
            DemoLog.error(name(), "获取模块失败");
            return;
        }

        final String payloadIndex = payloadIndex();
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "payloadIndex=" + payloadIndex + " actionTimeout=" + actionTimeout + "ms");

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                console("(DRC链路: " + (drcConnected ? "已连接" : "未连接")
                    + "  |  负载控制权: " + (payloadAuthAcquired ? "已获取" : "未获取") + ")");
                if (panoStatusSub != null) {
                    console("(全景拍照状态订阅中: 拍摄时实时打印进度)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-11, q 退出): ", 1, 11);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出全景拼接菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1: startPanoPhoto(auth, drc, camera, payloadIndex, actionTimeout); break;
                        case 2: stopPanoPhoto(auth, drc, camera, payloadIndex, actionTimeout);   break;
                        case 3: startPanoStitch(media, actionTimeout);                           break;
                        case 4: stopPanoStitch(media, actionTimeout);                            break;
                        case 5: retryPanoStitch(media, actionTimeout);                           break;
                        case 6: listPanoMediaFiles(media, actionTimeout);                        break;
                        case 7: getPanoTileUrl(media, actionTimeout);                            break;
                        case 8: getMediaDownloadUrl(media, actionTimeout);                       break;
                        case 9: watchUploadProgress(media);                                      break;
                        case 10: watchPanoStitchProgress(media);                                 break;
                        case 11: watchPanoShootProgress(auth, drc, camera, payloadIndex, actionTimeout); break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // 收尾：取消订阅 → 断 DRC → 释放负载控制权(仅回收本 demo 期间实际获取过的资源)
            if (panoStatusSub != null) {
                final Subscription sub = panoStatusSub;
                quiet("取消全景拍照状态订阅", () -> { sub.close(); registry().remove(sub); });
                panoStatusSub = null;
            }
            if (cameraStateSub != null) {
                final Subscription sub = cameraStateSub;
                quiet("取消相机状态订阅", () -> { sub.close(); registry().remove(sub); });
                cameraStateSub = null;
            }
            if (liveVideoInfoSub != null) {
                final Subscription sub = liveVideoInfoSub;
                quiet("取消直播镜头信息订阅", () -> { sub.close(); registry().remove(sub); });
                liveVideoInfoSub = null;
            }
            if (drcConnected) {
                quiet("断开 DRC 链路", () -> syncOutcome("disconnectDrcLink", drc::disconnectDrcLink, actionTimeout));
                drcConnected = false;
            }
            if (payloadAuthAcquired) {
                quiet("释放负载控制权", () -> callOutcome("releasePayloadControlAuthority",
                    cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), actionTimeout));
                payloadAuthAcquired = false;
            }
        }
    }

    // ==================== [1] 全景拍照 (获取负载控制权 → 建DRC → 切模式+切镜头(幂等+settle) → drcStartPanorama) ====================

    private void startPanoPhoto(ControlAuthModule auth, DrcModule drc, CameraControlModule camera,
            String payloadIndex, long timeoutMs) {
        Node cfg = params.node("pano_stitch_demo");

        // Step 1+2: 获取负载控制权 + 建立 DRC 链路(均带幂等) —— 抽成 ensureDrcReady 与 [5] 共用
        if (!ensureDrcReady(auth, drc, camera, payloadIndex, timeoutMs, "1-2/3")) {
            console("   前置(控制权/DRC)未就绪, 终止全景拍照流程");
            return;
        }

        // Step 3: 切换相机模式到全景(PANO_PHOTO) + 切镜头(1X/3X) —— 与驾驶舱页面行为对齐:
        // 页面(uranus-cockpit-app LiveCameraControlPanel.vue)把"切模式"与"切镜头"做成两次独立的
        // 用户点击, 且均带幂等判断(当前状态==目标状态就不下发); 切模式后页面用 cameraTypeChanging
        // 强制锁定 2500ms 不发任何后续命令, 给设备状态收敛时间(注释虽写"1.5s"但 setTimeout 字面量是
        // 2500, 以代码为准)。之前 SDK demo 连续调用(0 延迟)导致设备返回 deviceCode=327009,
        // 本质是"发太快, 设备状态还没切完就收到下一条指令"——这里补齐幂等判断 + settle 延迟对齐页面。
        int cachedMode = currentCameraMode.get();
        console("   [3/4] 当前相机模式(来自 listenDrcCameraState 推送): "
            + (cachedMode == -1 ? "未知(尚未收到推送)" : cachedMode + "(" + describeCameraMode(cachedMode) + ")"));
        if (cachedMode != CameraMode.PANO_PHOTO.getValue()) {
            console("   [3/4] 切换相机模式到全景(PANO_PHOTO) ...");
            SwitchCameraModeParams modeParams = new SwitchCameraModeParams();
            modeParams.setPayloadIndex(payloadIndex);
            modeParams.setCameraMode(CameraMode.PANO_PHOTO);
            Outcome modeOutcome = callOutcome("drcSwitchCameraMode(PANO_PHOTO)",
                cb -> camera.drcSwitchCameraMode(modeParams, cb), timeoutMs);
            if (modeOutcome != Outcome.SUCCESS) {
                console("   切相机模式失败, 终止全景拍照流程");
                return;
            }
            long modeSettleMs = cfg.longVal("mode_settle_ms", 2500L);
            console("   切模式指令下发成功, 等待设备状态收敛 " + modeSettleMs
                + "ms(对齐页面 cameraTypeChanging 锁定时长) ...");
            sleep(modeSettleMs);
        } else {
            console("   [3/4] 当前已是 PANO_PHOTO, 跳过切模式(幂等)");
        }

        // Step 4: 切镜头(ZOOM=3X 超清 / WIDE=1X 球形) —— 同样先判断当前镜头是否已是目标值
        int lensTypeVal = cfg.intVal("lens_type", CameraLensType.ZOOM.getValue());
        CameraLensType lensType = CameraLensType.fromValue(lensTypeVal);
        if (lensType == null || lensType == CameraLensType.UNKNOWN) {
            lensType = CameraLensType.ZOOM;
        }
        int cachedLens = currentCameraLens.get();
        console("   [4/4] 当前镜头(来自 listenDrcCameraState 推送): "
            + (cachedLens == -1 ? "未知(尚未收到推送)" : cachedLens + "(" + describeLensType(cachedLens) + ")")
            + ", 目标=" + lensType.getValue() + "(" + lensType + ")");
        if (cachedLens != lensType.getValue()) {
            SwitchLiveLensParams lensParams = new SwitchLiveLensParams();
            lensParams.setPayloadIndex(payloadIndex);
            lensParams.setLensType(lensType);
            final CameraLensType finalLensType = lensType;
            console("   [4/4] 切换镜头(" + finalLensType + ") ...");
            Outcome lensOutcome = callOutcome("drcSwitchLiveLens(" + finalLensType + ")",
                cb -> camera.drcSwitchLiveLens(lensParams, cb), timeoutMs);
            if (lensOutcome != Outcome.SUCCESS) {
                console("   切镜头失败, 终止全景拍照流程");
                return;
            }
            console("   切镜头成功: " + finalLensType
                + (finalLensType == CameraLensType.ZOOM ? "（3X 超清全景）" : "（1X 球形全景）"));
            long lensSettleMs = cfg.longVal("lens_settle_ms", 1500L);
            console("   切镜头指令下发成功, 等待设备状态收敛 " + lensSettleMs
                + "ms(页面无显式延迟, 但用户点击子菜单本身有自然间隔; 此处补一个保守值) ...");
            sleep(lensSettleMs);
        } else {
            console("   [4/4] 当前镜头已是目标值, 跳过切镜头(幂等)");
        }

        // Step 5: 下发全景拍照指令, 并实时等待拍摄进度(percent=100)或超时
        console("   [5/5] 下发 drcStartPanorama 启动全景拍照 ...");
        PanoramaParam panoParam = new PanoramaParam();
        panoParam.setPayloadIndex(payloadIndex);
        // 若订阅未生效(listenId<0, 极少数情况), shootLatchRef 无人 countDown, 会等到超时——
        // 这属预期(提示用户订阅失败, 无法感知拍摄进度), 不额外处理。
        CountDownLatch shootLatch = new CountDownLatch(1);
        shootLatchRef.set(shootLatch);
        Outcome startOutcome = callOutcome("drcStartPanorama",
            cb -> camera.drcStartPanorama(panoParam, cb), timeoutMs);
        if (startOutcome != Outcome.SUCCESS) {
            shootLatchRef.set(null);
            console("   全景拍照指令下发失败, 终止流程");
            return;
        }

        long shootTimeoutMs = cfg.longVal("pano_shoot_timeout_ms", 300_000L);
        console("   指令下发成功, 等待拍摄完成(超时=" + shootTimeoutMs + "ms) ...");
        boolean shootDone;
        try {
            shootDone = shootLatch.await(shootTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            shootDone = false;
        }
        shootLatchRef.set(null);
        if (shootDone) {
            console("   ✓ 全景拍照完成(percent=100)！");
            DemoLog.info(name(), "全景拍照完成(percent=100)");
        } else {
            console("   ⚠ 等待超时(" + shootTimeoutMs + "ms), 未收到 percent=100, 拍摄可能仍在进行中"
                + "(可在日志文件查看后续推送, 或稍后执行 [2] 停止全景拍照)");
            DemoLog.warn(name(), "全景拍照等待超时(" + shootTimeoutMs + "ms), 未收到 percent=100");
        }
    }

    // ==================== [2] 停止全景拍照 (drcStopPanorama) ====================

    /**
     * 停止当前正在进行的全景拍照。若尚未建立控制权/DRC 链路(例如用户在驾驶舱页面手动触发了拍照,
     * 而 demo 侧尚未做过 [1]), 会先自动走一遍 ensureDrcReady 前置——语义与 [1] 一致, 保证
     * "只要设备在线, 就能停"; 已就绪时该方法幂等跳过, 直接下发 drcStopPanorama。
     */
    private void stopPanoPhoto(ControlAuthModule auth, DrcModule drc, CameraControlModule camera,
            String payloadIndex, long timeoutMs) {
        if (!ensureDrcReady(auth, drc, camera, payloadIndex, timeoutMs, "前置")) {
            console("   前置(控制权/DRC)未就绪, 终止停止全景拍照流程");
            return;
        }
        callOutcome("停止全景拍照(drcStopPanorama)",
            cb -> camera.drcStopPanorama(payloadIndex, cb), timeoutMs);
    }

    // ==================== [3] 开始全景拼接 (startPanoStitch) ====================

    private void startPanoStitch(MediaServiceModule module, long timeoutMs) {
        Long fileId = readFileId("全景照片目录 file_id(pano_total_count > 0 的目录)");
        if (fileId == null) {
            return;
        }
        console("   > fileId=" + fileId);
        callOutcome("开始全景拼接(startPanoStitch)", cb -> module.startPanoStitch(fileId,
            new FlightHubCallback.PanoStitchActionCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    cb.onResult(error);
                }
            }), timeoutMs);
        console("   提示: 服务端已提交拼接任务, 请人工调后端查询接口或 WS 订阅 topic_pano_stitch_progress 确认结果");
    }

    // ==================== [4] 停止全景拼接 (stopPanoStitch) ====================

    private void stopPanoStitch(MediaServiceModule module, long timeoutMs) {
        Long fileId = readFileId("全景成片 file_id(仅 state=1/2 时允许停止)");
        if (fileId == null) {
            return;
        }
        console("   > fileId=" + fileId);
        callOutcome("停止全景拼接(stopPanoStitch)", cb -> module.stopPanoStitch(fileId,
            new FlightHubCallback.PanoStitchActionCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    cb.onResult(error);
                }
            }), timeoutMs);
    }

    // ==================== 辅助方法 ====================

    // ==================== [5] 重试全景拼接 (retryPanoStitch) ====================

    /**
     * 重试失败/取消的全景拼接：适用于成片 {@code pano_stitch_state=4(失败)/5(取消)} 的场景,
     * 无需重新触发 [3] 开始拼接(会走幂等判断跳过), 直接对已有的 {@code pano_job_id} 发起重试。
     */
    private void retryPanoStitch(MediaServiceModule module, long timeoutMs) {
        Long fileId = readFileId("全景成片 file_id(仅 state=4/5 时建议重试)");
        if (fileId == null) {
            return;
        }
        console("   > fileId=" + fileId);
        callOutcome("重试全景拼接(retryPanoStitch)", cb -> module.retryPanoStitch(fileId,
            new FlightHubCallback.PanoStitchActionCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    cb.onResult(error);
                }
            }), timeoutMs);
        console("   提示: 服务端已重新提交拼接任务, 请人工调后端查询接口或 WS 订阅 topic_pano_stitch_progress 确认结果");
    }

    // ==================== [6] 查询全景 3X 成片列表 (getMediaFilesList + 客户端按文件名过滤) ====================

    /**
     * 查询"3X 超清全景成片"列表：SDK {@code getMediaFilesList} 走 {@code /media-files/list} 通用接口,
     * 后端目前不返回 {@code pano_total_count/pano_stitch_state/pano_stitch_progress} 具体值,
     * 也不区分"成片/目录/原图", 故本菜单以文件名后缀 {@code _3X} 作为"3X 全景成片"的识别标志
     * (成片命名规则: {@code Pano_<uuid8>_3X}, 由云端媒体服务生成; 单张原图为
     * {@code DJI_..._V_PANO_NNN.jpeg}, 不含 _3X)。
     *
     * <p>零输入交互: {@code deviceSn} 用 {@link com.dji.flighthubsdk.demo.runtime.DeviceSession#dockSn()};
     * 自动翻页拉取 {@value #LIST_MAX_TOTAL} 条上限, 客户端过滤后仅展示前 {@value #LIST_DISPLAY_LIMIT}
     * 条 3X 成片(倒序, 新→旧)。
     *
     * <p>典型用途: 快速找到最近的成片 fileId, 供 {@code [7] 获取瓦片 URL} / {@code [8] 获取下载地址} 使用。
     */
    private void listPanoMediaFiles(MediaServiceModule module, long timeoutMs) {
        String deviceSn = session != null && session.hasDock() ? session.dockSn() : "";
        console("   > 过滤设备 SN(dockSn from session): " + (deviceSn.isEmpty() ? "(空, 不过滤)" : deviceSn));
        console("   > 自动翻页最多拉取 " + LIST_MAX_TOTAL + " 条, 客户端过滤后展示前 " + LIST_DISPLAY_LIMIT + " 条 3X 成片 ...");

        java.util.List<MediaFilesListItemInfoData> hits = new java.util.ArrayList<>();
        int fetched = 0;
        int startIndex = 0;
        int lastTotal = -1;
        for (int page = 1; page <= LIST_MAX_PAGES && fetched < LIST_MAX_TOTAL && hits.size() < LIST_DISPLAY_LIMIT; page++) {
            int pageSize = Math.min(LIST_PAGE_SIZE, LIST_MAX_TOTAL - fetched);

            GetMediaFilesListParams req = new GetMediaFilesListParams();
            req.setStartIndex(startIndex);
            req.setPageSize(pageSize);
            req.setDeviceSn(deviceSn);
            req.setFileTypes(java.util.Arrays.asList("2"));

            final AtomicReference<GetMediaFilesListResults> resultRef = new AtomicReference<>();
            Outcome outcome = callOutcome("getMediaFilesList(3X page=" + page + ",startIndex=" + startIndex + ")",
                cb -> module.getMediaFilesList(req, new FlightHubCallback.GetMediaFilesListCallback() {
                    @Override
                    public void onResult(FlightHubError error, GetMediaFilesListResults results) {
                        resultRef.set(results);
                        cb.onResult(error);
                    }
                }), timeoutMs);
            if (outcome != Outcome.SUCCESS) {
                console("   翻页失败, 使用已收集到的 " + hits.size() + " 条结果继续展示");
                break;
            }
            GetMediaFilesListResults r = resultRef.get();
            if (r == null || r.getList() == null || r.getList().isEmpty()) {
                break;
            }
            fetched += r.getList().size();
            lastTotal = r.getTotal();
            for (MediaFilesListItemInfoData it : r.getList()) {
                String name = it.getFileName();
                if (name != null && name.contains("_3X")) {
                    hits.add(it);
                }
            }
            int nextIndex = r.getNextIndex();
            if (nextIndex <= 0 || nextIndex == startIndex) {
                // 到底了(-1) 或后端未推进
                break;
            }
            startIndex = nextIndex;
        }

        console("   累计拉取 " + fetched + " 条(后端 total=" + (lastTotal < 0 ? "?" : lastTotal)
            + "), 命中 3X 成片 " + hits.size() + " 条");
        if (hits.isEmpty()) {
            console("   (提示) 未找到 3X 成片; 可能是该 dock 尚无全景拼接产物, 或 total 超过拉取上限");
            return;
        }
        // 倒序展示前 N 条(后端返回按 fileId 升序, 新的在后; 直接反向遍历)
        int show = Math.min(LIST_DISPLAY_LIMIT, hits.size());
        console("   展示最新 " + show + " 条(倒序, 新→旧):");
        console(String.format("   %-8s %-10s %-40s %-8s %s",
            "fileId", "size(MB)", "fileName", "suffix", "createdAt"));
        for (int i = hits.size() - 1, printed = 0; i >= 0 && printed < show; i--, printed++) {
            MediaFilesListItemInfoData it = hits.get(i);
            console(String.format("   %-8d %-10.2f %-40s %-8s %s",
                it.getFileId(),
                it.getSize() / 1024.0 / 1024.0,
                nz(it.getFileName()),
                nz(it.getSuffix()),
                nz(it.getCreatedAt())));
        }
        console("   ==> 用上表任一 fileId 执行 [7] 获取瓦片 URL / [8] 获取下载地址");
    }

    /** 每次 getMediaFilesList 请求的 pageSize (后端上限 500)。 */
    private static final int LIST_PAGE_SIZE = 500;
    /** 最多累计拉取的记录数上限, 避免全景成片在极靠后位置时无限翻页。 */
    private static final int LIST_MAX_TOTAL = 10000;
    /** 最多翻页数硬上限, 双保险。 */
    private static final int LIST_MAX_PAGES = LIST_MAX_TOTAL / LIST_PAGE_SIZE;
    /** 客户端过滤后展示的最大条数。 */
    private static final int LIST_DISPLAY_LIMIT = 20;

    // ==================== [7] 获取全景瓦片 URL (getPanoTierTileUrl) ====================

    /**
     * 获取全景成片的瓦片访问信息: baseUrl + queryParams(签名/权限参数)。
     *
     * <p>前置: 成片 {@code pano_stitch_state=3(成功)}, 且已跑过 {@code startPanoTier} 生成瓦片
     * (通常拼接完成后由后端自动触发, 无需手动干预)。
     *
     * <p>使用: 前端把 {@code baseUrl?queryParams} 传给全景查看器 SDK 即可加载分级瓦片。
     */
    private void getPanoTileUrl(MediaServiceModule module, long timeoutMs) {
        Long fileId = readFileId("全景成片 file_id(state=3 且已生成瓦片)");
        if (fileId == null) return;

        final AtomicReference<GetPanoTierTileUrlResult> ref = new AtomicReference<>();
        console("   > getPanoTierTileUrl(fileId=" + fileId + ") ...");
        Outcome outcome = callOutcome("getPanoTierTileUrl",
            cb -> module.getPanoTierTileUrl(fileId, new FlightHubCallback.GetPanoTierTileUrlCallback() {
                @Override
                public void onResult(FlightHubError error, GetPanoTierTileUrlResult result) {
                    ref.set(result);
                    cb.onResult(error);
                }
            }), timeoutMs);
        if (outcome != Outcome.SUCCESS) return;

        GetPanoTierTileUrlResult r = ref.get();
        if (r == null) {
            console("   (null) 后端返回空结果");
            return;
        }
        console("   state    = " + r.getState() + "(" + describeTierState(r.getState()) + ")");
        console("   baseUrl  = " + nz(r.getBaseUrl()));
        if (r.getQueryParams() != null && !r.getQueryParams().isEmpty()) {
            console("   queryParams:");
            for (java.util.Map.Entry<String, String> e : r.getQueryParams().entrySet()) {
                console("     " + e.getKey() + " = " + e.getValue());
            }
        } else {
            console("   queryParams: (空)");
        }
        if (r.getCookies() != null && !r.getCookies().isEmpty()) {
            console("   cookies  : " + r.getCookies().size() + " 项 (详见日志文件)");
            DemoLog.info(name(), "getPanoTierTileUrl cookies=" + r.getCookies());
        }
        // 拼装完整可访问示例 URL(仅打印, 不真正请求)
        if (!r.getBaseUrl().isEmpty() && r.getQueryParams() != null && !r.getQueryParams().isEmpty()) {
            StringBuilder qs = new StringBuilder();
            for (java.util.Map.Entry<String, String> e : r.getQueryParams().entrySet()) {
                if (qs.length() > 0) qs.append('&');
                qs.append(e.getKey()).append('=').append(e.getValue());
            }
            console("   完整访问示例(不含实际瓦片路径 z/x/y):");
            console("     " + r.getBaseUrl() + "?" + qs);
        }
    }

    // ==================== [8] 获取媒体文件下载地址 (getMediaFilesDownloadURL) ====================

    /**
     * 获取任一媒体文件的下载 URL: 可用于下载全景原图目录下的单张照片, 或成片对应的 tier zip 等。
     * 全景成片本身不通过此接口下载瓦片, 请使用 {@code [7] 获取全景瓦片 URL}。
     */
    private void getMediaDownloadUrl(MediaServiceModule module, long timeoutMs) {
        Long fileId = readFileId("任一媒体文件 file_id");
        if (fileId == null) return;

        final AtomicReference<GetMediaFilesDownloadUrlResults> ref = new AtomicReference<>();
        console("   > getMediaFilesDownloadURL(fileId=" + fileId + ") ...");
        Outcome outcome = callOutcome("getMediaFilesDownloadURL",
            cb -> module.getMediaFilesDownloadURL(fileId, new FlightHubCallback.GetMediaFilesDownloadUrlCallback() {
                @Override
                public void onResult(FlightHubError error, GetMediaFilesDownloadUrlResults result) {
                    ref.set(result);
                    cb.onResult(error);
                }
            }), timeoutMs);
        if (outcome != Outcome.SUCCESS) return;

        GetMediaFilesDownloadUrlResults r = ref.get();
        if (r == null || r.getUrl() == null || r.getUrl().isEmpty()) {
            console("   (空) 后端未返回下载 URL");
            return;
        }
        console("   下载 URL: " + r.getUrl());
    }

    // ==================== [9] 订阅媒体文件上传进度 (listenMediaFileDeviceUploadMessage) ====================

    /**
     * 订阅"设备端媒体文件上传完成"消息：{@link MediaFileDeviceUpload} 语义是"设备已把某个文件
     * 上传到云端"的一次性完成通知(携带 fileId/objectKey/objectSize/fileType), 而非
     * "0~100% 上传进度百分比"——SDK 当前未提供百分比级上传进度接口, 拍摄大量照片(如 3X
     * 全景 137 张)时会陆续收到多条本消息, 每条对应一张已上传完成的照片。
     *
     * <p>设备 SN 直接取当前会话选中的机场 {@code session.dockSn()}; 订阅期间阻塞等待用户按回车
     * 结束, 结束后自动取消订阅并打印累计收到的消息数。
     */
    private void watchUploadProgress(MediaServiceModule module) {
        String deviceSn = session != null && session.hasDock() ? session.dockSn() : "";
        console("   > 订阅设备上传完成通知(deviceSn=" + (deviceSn.isEmpty() ? "(空, 不过滤)" : deviceSn) + ") ...");
        console("   提示: 本消息是\"单个文件上传完成\"通知(无百分比进度), 拍摄多张照片时会收到多条");

        final AtomicInteger received = new AtomicInteger(0);
        int listenId = module.listenMediaFileDeviceUploadMessage(deviceSn,
            new FlightHubCallback.MediaFileDeviceUploadCallback() {
                @Override
                public void onResult(MediaFileDeviceUpload msg) {
                    if (msg == null) return;
                    int count = received.incrementAndGet();
                    DemoLog.info(name(), "listenMediaFileDeviceUploadMessage() #MediaFileDeviceUpload, [上传完成] #"
                        + count + " fileId=" + msg.getFileId() + " deviceSn=" + msg.getDeviceSn()
                        + " fileType=" + msg.getFileType() + " objectSize=" + msg.getObjectSize()
                        + " objectKey=" + msg.getObjectKey());
                    console("   [上传完成] #" + count + " fileId=" + msg.getFileId()
                        + " fileType=" + msg.getFileType()
                        + " size=" + String.format("%.2f", msg.getObjectSize() / 1024.0 / 1024.0) + "MB"
                        + " objectKey=" + msg.getObjectKey());
                }
            });
        if (listenId < 0) {
            console("   订阅失败(listenId=" + listenId + ")");
            return;
        }
        console("   订阅成功(listenId=" + listenId + "), 按回车结束订阅 ...");
        Utils.readLine(input, "");
        module.cancelListenMediaFileDeviceUploadMessage(listenId);
        console("   已取消订阅, 期间共收到 " + received.get() + " 条上传完成通知");
    }

    // ==================== [10] 订阅全景拼接进度 (subscribePanoStitchProgress) ====================

    /**
     * 订阅全景拼接进度推送：对应后端 topic_pano_stitch_progress, 携带 fileId/state/progress/errorCode,
     * 是 [3] 开始拼接 / [5] 重试拼接后确认结果的推荐方式(比人工轮询后端接口更实时)。
     *
     * <p>PaaS 全景拼接进度；订阅期间阻塞等待用户按回车结束，
     * 结束后自动取消订阅并打印累计收到的消息数。
     */
    private void watchPanoStitchProgress(MediaServiceModule module) {
        console("   > 订阅全景拼接进度 ...");

        final AtomicInteger received = new AtomicInteger(0);
        int listenId = module.subscribePanoStitchProgress(
            new FlightHubCallback.PanoStitchProgressCallback() {
                @Override
                public void onResult(PanoStitchProgressMsg msg) {
                    if (msg == null) return;
                    int count = received.incrementAndGet();
                    int state = msg.getPanoStitchState();
                    DemoLog.info(name(), "subscribePanoStitchProgress() #PanoStitchProgressMsg, [拼接进度] #"
                        + count + " fileId=" + msg.getFileId() + " state=" + state
                        + "(" + describeStitchState(state) + ") progress=" + msg.getPanoStitchProgress()
                        + "% errorCode=" + msg.getPanoErrorCode() + " name=" + msg.getName());
                    console("   [拼接进度] #" + count + " fileId=" + msg.getFileId()
                        + " state=" + state + "(" + describeStitchState(state) + ")"
                        + " progress=" + msg.getPanoStitchProgress() + "%"
                        + (msg.getPanoErrorCode() != 0 ? " errorCode=" + msg.getPanoErrorCode() : ""));
                }
            });
        if (listenId < 0) {
            console("   订阅失败(listenId=" + listenId + "), 请确认后端已合入 PaaS 进度分流");
            return;
        }
        console("   订阅成功(listenId=" + listenId + "), 按回车结束订阅 ...");
        Utils.readLine(input, "");
        module.cancelListenPanoStitchProgress(listenId);
        console("   已取消订阅, 期间共收到 " + received.get() + " 条拼接进度消息");
    }

    // ==================== [11] 订阅全景拍照进度 (listenDrcPanoramaStatus) ====================

    /**
     * 订阅全景拍照(拍摄阶段)进度：对应 DRC 推送 {@code drc_camera_photo_info_push}, 携带
     * currentStep/percent/photoState, 是[1] 内部自动打印的同一路推送——独立出这个菜单项方便在
     * 不经过 [1] 完整流程(比如驾驶舱页面手动拍)的情况下, 单独观察拍摄进度。
     *
     * <p><b>去重</b>：若 [1] 已经建立过常驻订阅({@link #panoStatusSub} 非空), 说明当前 DRC 会话
     * 已经在实时打印 "[全景拍照进度]"——此时不会再创建第二个 listenId(否则设备侧会把同一条推送
     * 分别推给两个订阅, 导致每条消息打印两遍), 而是直接提示复用已有订阅, 阻塞等待用户按回车返回。
     *
     * <p>需要 DRC 链路已连接(未连接会自动走 {@link #ensureDrcReady} 前置, 与 [1]/[2] 幂等复用)。
     * 订阅期间阻塞等待用户按回车结束, 结束后自动取消订阅并打印累计收到的消息数。
     */
    private void watchPanoShootProgress(ControlAuthModule auth, DrcModule drc, CameraControlModule camera,
            String payloadIndex, long timeoutMs) {
        if (!ensureDrcReady(auth, drc, camera, payloadIndex, timeoutMs, "前置")) {
            console("   前置(控制权/DRC)未就绪, 终止订阅全景拍照进度");
            return;
        }

        if (panoStatusSub != null) {
            console("   > 检测到 [1] 已建立常驻拍照进度订阅, 不重复订阅(避免同一条推送打印两遍)");
            console("   > 上方 \"[全景拍照进度]\" 即为实时进度, 按回车返回菜单 ...");
            Utils.readLine(input, "");
            return;
        }

        console("   > 订阅全景拍照进度(payloadIndex=" + payloadIndex + ") ...");
        console("   提示: 此订阅与 [1] 内部自动打印的进度是同一路推送, 可独立用于观察驾驶舱页面手动拍照的进度");

        final AtomicInteger received = new AtomicInteger(0);
        int listenId = camera.listenDrcPanoramaStatus(payloadIndex,
            new FlightHubCallback.PanoramaStatusCallback() {
                @Override
                public void onResult(PanoramaStatus status) {
                    if (status == null) return;
                    int count = received.incrementAndGet();
                    int percent = status.getPercent();
                    int currentStep = status.getCurrentStep();
                    int photoState = status.getPhotoState();
                    DemoLog.info(name(), "watchPanoShootProgress() #PanoramaStatus, [拍照进度] #" + count
                        + " step=" + currentStep + " percent=" + percent + "% photoState=" + photoState);
                    console("   [拍照进度] #" + count + " step=" + currentStep
                        + " percent=" + percent + "% photoState=" + photoState);
                }
            });
        if (listenId < 0) {
            console("   订阅失败(listenId=" + listenId + ")");
            return;
        }
        console("   订阅成功(listenId=" + listenId + "), 按回车结束订阅 ...");
        Utils.readLine(input, "");
        camera.cancelListenDrcPanoramaStatus(payloadIndex, listenId);
        console("   已取消订阅, 期间共收到 " + received.get() + " 条拍照进度消息");
    }

    /** 全景拼接 state 语义(与后端 pano_stitch_state 一致, 同 {@link PanoStitchProgressMsg#getPanoStitchState()})。 */
    private static String describeStitchState(int state) {
        switch (state) {
            case 0: return "未处理";
            case 1: return "等待资源";
            case 2: return "处理中";
            case 3: return "成功";
            case 4: return "失败";
            case 5: return "取消";
            case 6: return "余额不足";
            default: return "未知";
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }

    /** GetPanoTierTileUrlResult#state 语义(与后端 pano_tier_state 一致)。 */
    private static String describeTierState(int state) {
        switch (state) {
            case 0: return "未处理";
            case 1: return "等待";
            case 2: return "处理中";
            case 3: return "成功";
            case 4: return "失败";
            default: return "未知";
        }
    }

    /**
     * DRC 前置就绪保证：等待负载列表推送 → 获取负载控制权 → 建立 DRC 链路 → 订阅全景拍照状态 + 相机状态。
     * 均带幂等：已获取控制权/已连接 DRC 时跳过对应步骤；订阅仅在首次建链后建立一次。
     *
     * <p>顺序与延迟均参照 {@code DrcPanoScenario}：云端通知服务的"订阅立即推送"是异步
     * WebSocket(有 100~500ms 延迟)，故必须先等待 {@code payloads_settle_ms} 再获取控制权；
     * 且控制权必须先于 DRC 建链获取(否则 ERROR_INVALID_STATE)。
     *
     * @param stepLabel 步骤编号标签(如 "1-2/5" 或 "前置"), 仅用于控制台日志
     * @return 前置全部就绪返回 true; 任何一步失败返回 false(调用方应终止后续动作)
     */
    private boolean ensureDrcReady(ControlAuthModule auth, DrcModule drc, CameraControlModule camera,
            String payloadIndex, long timeoutMs, String stepLabel) {
        Node cfg = params.node("pano_stitch_demo");

        // Step 1: 获取负载控制权(若未获取)
        if (!payloadAuthAcquired) {
            long payloadsSettleMs = cfg.longVal("payloads_settle_ms", 2000L);
            console("   [" + stepLabel + "] 等待设备负载列表推送 " + payloadsSettleMs + "ms ...");
            sleep(payloadsSettleMs);

            console("   [" + stepLabel + "] 获取负载控制权 ...");
            Outcome authOutcome = callOutcome("acquirePayloadControlAuthority",
                cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), timeoutMs);
            if (authOutcome != Outcome.SUCCESS) {
                console("   获取负载控制权失败");
                return false;
            }
            payloadAuthAcquired = true;
        } else {
            console("   [" + stepLabel + "] 负载控制权已获取, 跳过");
        }

        // Step 2: 建立 DRC 链路(若未连接) —— 全景拍照的硬前置, 必须在获取负载控制权之后
        if (!drcConnected) {
            console("   [" + stepLabel + "] 建立 DRC 链路 ...");
            Outcome drcOutcome = syncOutcome("connectDrcLink", drc::connectDrcLink, timeoutMs);
            if (drcOutcome != Outcome.SUCCESS) {
                console("   DRC 链路建立失败");
                return false;
            }
            drcConnected = true;
            long linkSettleMs = cfg.longVal("link_settle_ms", 3000L);
            console("   DRC 建链成功, 等待链路稳定 " + linkSettleMs + "ms ...");
            sleep(linkSettleMs);

            // DRC 连接成功后自动订阅全景拍照状态：既落日志，也实时打印到控制台，
            // 并在 percent>=100 时对当前拍摄轮次的 latch(如有)countDown，供 [1] 的 Step 5 阻塞等待。
            int listenId = camera.listenDrcPanoramaStatus(payloadIndex,
                new FlightHubCallback.PanoramaStatusCallback() {
                    @Override
                    public void onResult(PanoramaStatus status) {
                        if (status == null) {
                            return;
                        }
                        int percent = status.getPercent();
                        int currentStep = status.getCurrentStep();
                        DemoLog.info(name(), "listenDrcPanoramaStatus() #PanoramaStatus, [全景拍照状态] step=" + currentStep
                            + " percent=" + percent + "%"
                            + " photoState=" + status.getPhotoState());
                        console("   [全景拍照进度] step=" + currentStep + " percent=" + percent + "%");
                        if (percent >= 100) {
                            CountDownLatch latch = shootLatchRef.get();
                            if (latch != null) {
                                latch.countDown();
                            }
                        }
                    }
                });
            if (listenId >= 0) {
                panoStatusSub = subscribe("listenDrcPanoramaStatus", listenId,
                    id -> camera.cancelListenDrcPanoramaStatus(payloadIndex, id));
                console("   全景拍照状态订阅成功(listenId=" + listenId + ")");
            } else {
                DemoLog.warn(name(), "订阅全景拍照状态失败(listenId=" + listenId + ")");
            }

            // 同时订阅相机状态(cameraMode)：设备侧相机模式可能被驾驶舱页面手动切换等
            // DRC 之外的渠道改变；[1] 切模式前需要感知真实当前模式，避免因
            // 状态冲突被设备拒绝(deviceCode=327009)。只落日志，不刷控制台(避免和拍照进度混淆)。
            int cameraStateListenId = camera.listenDrcCameraState(payloadIndex,
                new FlightHubCallback.CameraStateCallback() {
                    @Override
                    public void onResult(CameraState state) {
                        if (state == null) {
                            return;
                        }
                        int mode = state.getCameraMode();
                        int prev = currentCameraMode.getAndSet(mode);
                        if (prev != mode) {
                            DemoLog.info(name(), "listenDrcCameraState() #CameraState, [相机状态] cameraMode="
                                + mode + "(" + describeCameraMode(mode) + ") (由 " + prev + " 变化)");
                        }
                    }
                });
            if (cameraStateListenId >= 0) {
                cameraStateSub = subscribe("listenDrcCameraState", cameraStateListenId,
                    id -> camera.cancelListenDrcCameraState(payloadIndex, id));
                console("   相机状态订阅成功(listenId=" + cameraStateListenId + ")");
            } else {
                DemoLog.warn(name(), "订阅相机状态失败(listenId=" + cameraStateListenId + "), 无法感知当前相机模式,"
                    + " 切模式前将无条件下发(可能因状态冲突被拒绝)");
            }

            // 同时订阅直播画面信息(video_type)：对应驾驶舱页面的 drc_live_video_info_push, 用于
            // 感知当前实际镜头(1X/3X/IR 等)。[1] 切镜头前据此判断是否需要真的下发切换指令,
            // 避免设备已经是目标镜头时仍重复下发(与页面 onSetUltraHdPanorama 的幂等判断对齐)。
            int liveVideoInfoListenId = camera.listenDrcLiveVideoInfo(payloadIndex,
                new FlightHubCallback.LiveVideoInfoCallback() {
                    @Override
                    public void onResult(LiveVideoInfo info) {
                        if (info == null) {
                            return;
                        }
                        int lens = info.getVideoTypeSwig();
                        int prev = currentCameraLens.getAndSet(lens);
                        if (prev != lens) {
                            DemoLog.info(name(), "listenDrcLiveVideoInfo() #LiveVideoInfo, [直播镜头] videoType="
                                + lens + "(" + describeLensType(lens) + ") (由 " + prev + " 变化)");
                        }
                    }
                });
            if (liveVideoInfoListenId >= 0) {
                liveVideoInfoSub = subscribe("listenDrcLiveVideoInfo", liveVideoInfoListenId,
                    id -> camera.cancelListenDrcLiveVideoInfo(payloadIndex, id));
                console("   直播镜头信息订阅成功(listenId=" + liveVideoInfoListenId + ")");
            } else {
                DemoLog.warn(name(), "订阅直播镜头信息失败(listenId=" + liveVideoInfoListenId + "), 无法感知当前镜头,"
                    + " 切镜头前将无条件下发(可能因状态冲突被拒绝)");
            }
        } else {
            console("   [" + stepLabel + "] DRC 链路已连接, 跳过");
        }
        return true;
    }

    /** 读取 file_id(q / EOF 取消时返回 {@code null})。 */
    private Long readFileId(String hintText) {
        String line = Utils.readLineEcho(input, "   请输入 " + hintText + "(q 取消): ");
        if (line == null || line.trim().equalsIgnoreCase("q")) {
            hint("已取消操作");
            return null;
        }
        if (line.trim().isEmpty()) {
            hint("file_id 不能为空");
            return null;
        }
        try {
            return Long.parseLong(line.trim());
        } catch (NumberFormatException e) {
            hint("file_id 必须是数字: " + line.trim());
            return null;
        }
    }

    /** 收尾单步的 Throwable 兜底：吞掉异常并记录，不中断后续收尾(与 Scenario#quiet 同语义)。 */
    protected void quiet(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] " + label + " 失败(不阻断后续收尾): " + Utils.describe(t));
        }
    }

    /** 纯控制台输出(不落日志文件), 用于菜单文案与结果回显。 */
    protected void console(String line) {
        System.out.println(line);
    }

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足、取消等需留痕的提示。 */
    protected void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /** 相机模式枚举描述(出处: CameraState#getCameraMode 注释, 与 C++/CameraMode 一致)。 */
    private static String describeCameraMode(int mode) {
        switch (mode) {
            case 0:     return "PHOTO 拍照";
            case 1:     return "VIDEO 录像";
            case 2:     return "SMART_PHOTO 智能低光拍照";
            case 3:     return "PANO_PHOTO 全景拍照";
            case 4:     return "INTERVAL_PHOTO 定时拍照";
            case 65535: return "UNKNOWN 未知";
            default:    return "未知(" + mode + ")";
        }
    }

    /** 镜头类型枚举描述(出处: {@link com.dji.flighthubsdk.value.CameraLensType})。 */
    private static String describeLensType(int lens) {
        switch (lens) {
            case 0:     return "NORMAL 常规";
            case 1:     return "WIDE 广角(1X 球形全景)";
            case 2:     return "ZOOM 变焦(3X 超清全景)";
            case 3:     return "IR 红外";
            case 4:     return "THERMAL 热成像";
            case 65535: return "UNKNOWN 未知";
            default:    return "未知(" + lens + ")";
        }
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：5 个菜单项(见类 Javadoc 菜单树)。增删菜单项时须同步 {@link #run()} 中的
     * switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 全景拼接 (Pano Stitch) ===\n"
            + "  1.  全景拍照        (建控制权/DRC链路(若未建) → 切模式=PANO_PHOTO(幂等) → 切镜头(幂等) → drcStartPanorama)\n"
            + "  2.  停止全景拍照    (drcStopPanorama, 未建控制权/DRC链路时自动建)\n"
            + "  3.  开始全景拼接    (startPanoStitch, 需输入全景目录 fileId)\n"
            + "  4.  停止全景拼接    (stopPanoStitch, 需输入全景成片 fileId, 仅 state=1/2 可停)\n"
            + "  5.  重试全景拼接    (retryPanoStitch, 需输入全景成片 fileId, 用于 state=4/5 时重试)\n"
            + "  6.  查询 3X 全景成片列表 (getMediaFilesList + 客户端过滤 file_name 含 _3X)\n"
            + "  7.  获取全景瓦片URL (getPanoTierTileUrl, 返回 baseUrl + queryParams, 供前端全景查看器使用)\n"
            + "  8.  获取媒体下载URL (getMediaFilesDownloadURL, 用于下载原图/中间文件, 非瓦片)\n"
            + "  9.  订阅媒体上传进度 (listenMediaFileDeviceUploadMessage, 无百分比, 单文件上传完成通知)\n"
            + "  10. 订阅全景拼接进度 (subscribePanoStitchProgress, 实时百分比, 按回车结束订阅)\n"
            + "  11. 订阅全景拍照进度 (listenDrcPanoramaStatus, 拍摄阶段百分比, 按回车结束订阅)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
