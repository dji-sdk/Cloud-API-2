// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetPanoTierTileUrlResult;
import com.dji.flighthubsdk.bean.LiveVideoInfo;
import com.dji.flighthubsdk.bean.PanoStitchProgressMsg;
import com.dji.flighthubsdk.bean.PanoramaParam;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.SwitchCameraModeParams;
import com.dji.flighthubsdk.bean.SwitchLiveLensParams;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.value.CameraLensType;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.demo.base.Scenario;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 场景：驾驶舱手动全景拍照 → 后端自动触发全景拼接 → 获取瓦片 URL。
 *
 * <p>流程：
 * <ol>
 *   <li>获取负载控制权；</li>
 *   <li>建立 DRC 链路（全景拍照的硬前置）；</li>
 *   <li>订阅全景拼接进度（subscribePanoStitchProgress），等待后端自动触发拼接；</li>
 *   <li>订阅全景拍照状态（listenDrcPanoramaStatus）+ 相机模式/镜头状态
 *       （listenDrcCameraState / listenDrcLiveVideoInfo）；</li>
 *   <li>切换相机模式到全景拍照（drcSwitchCameraMode → PANO_PHOTO，幂等：已是目标模式则跳过，
 *       下发成功后 settle {@code mode_settle_ms}）；</li>
 *   <li>切换镜头（drcSwitchLiveLens → ZOOM 3X 或 WIDE 1X，幂等：已是目标镜头则跳过，
 *       下发成功后 settle {@code lens_settle_ms}）；</li>
 *   <li>下发 drcStartPanorama 启动全景拍照；</li>
 *   <li>等待全景拍照完成（percent=100）；</li>
 *   <li>等待后端自动触发拼接（panoStitchState=3 成功）；</li>
 *   <li>调 getPanoTierTileUrl 获取瓦片访问 URL；</li>
 *   <li>收尾：取消订阅 → 断 DRC → 释放控制权。</li>
 * </ol>
 *
 * <p><b>与驾驶舱页面行为对齐（deviceCode=327009 修复）：</b>切模式/切镜头前均先查当前状态，
 * 已是目标值则跳过下发；之前连续调用(0 延迟)会导致设备状态未收敛就收到下一条指令，
 * 返回 deviceCode=327009。逻辑与 {@link com.dji.flighthubsdk.demo.module.PanoStitchDemo}
 * 的 {@code startPanoPhoto}/{@code ensureDrcReady} 保持一致。
 *
 * <p>参数取自 {@code demo_params.json} 的 {@code drc_pano_scenario} 分组：
 * {@code pano_shoot_timeout_ms}（默认 300000，5 分钟）、
 * {@code pano_stitch_timeout_ms}（默认 600000，10 分钟）、
 * {@code lens_type}（1=WIDE 1X 球形，2=ZOOM 3X 超清，默认 2）、
 * {@code payloads_settle_ms}（等待负载列表推送的稳定延迟，默认 2000）、
 * {@code mode_settle_ms}（切模式下发成功后等待设备状态收敛的延迟，默认 2500，
 * 对齐驾驶舱页面 cameraTypeChanging 锁定时长）、
 * {@code lens_settle_ms}（切镜头下发成功后等待设备状态收敛的延迟，默认 1500）。
 */
public class DrcPanoScenario extends Scenario {

    @Override
    public String name() {
        return "DrcPanoScenario";
    }

    @Override
    protected void steps() throws Exception {
        if (!session.hasDrone()) {
            DemoLog.warn(name(), "该设备没有无人机, 无法运行驾驶舱全景拍照场景");
            System.out.println("   该设备没有无人机, 无法运行驾驶舱全景拍照场景");
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
            throw new FlightHubException(
                "获取模块失败: auth=" + (auth != null)
                + " drc=" + (drc != null)
                + " camera=" + (camera != null)
                + " media=" + (media != null)
            );
        }

        final String payloadIndex = payloadIndex();
        final long defaultTimeout = defaultTimeout();
        final com.dji.flighthubsdk.demo.runtime.Node cfg = params.node("drc_pano_scenario");
        final long shootTimeoutMs = cfg.longVal("pano_shoot_timeout_ms", 300_000L);
        final long stitchTimeoutMs = cfg.longVal("pano_stitch_timeout_ms", 600_000L);

        DemoLog.info(name(), "payloadIndex=" + payloadIndex
            + " shootTimeout=" + shootTimeoutMs + "ms"
            + " stitchTimeout=" + stitchTimeoutMs + "ms");

        boolean payloadAuthAttempted = false;
        boolean drcConnectAttempted = false;
        final List<Subscription> subs = new ArrayList<>();

        // 拼接完成的成片 fileId（由进度推送填充）
        final AtomicLong stitchResultFileId = new AtomicLong(-1L);
        // 等待拍照完成的 latch
        final CountDownLatch shootLatch = new CountDownLatch(1);
        // 等待拼接完成的 latch
        final CountDownLatch stitchLatch = new CountDownLatch(1);
        // 拼接最终状态
        final AtomicReference<String> stitchFinalState = new AtomicReference<>("unknown");
        // 最近一次收到的相机模式(见 CameraMode)，-1 表示尚未收到推送(未知)。
        final AtomicInteger currentCameraMode = new AtomicInteger(-1);
        // 最近一次收到的镜头类型 videoTypeSwig(见 CameraLensType)，-1 表示尚未收到推送(未知)。
        final AtomicInteger currentCameraLens = new AtomicInteger(-1);

        try {
            // ---- Step 0: 等待设备负载列表推送 ----
            // 云端通知服务的"订阅立即推送"是异步 WebSocket，有 100~500ms 延迟；
            // 若立即调 acquirePayloadControlAuthority，会因 camera_abs_map_ 未填充而 ERROR_INVALID_STATE。
            long payloadsSettleMs = cfg.longVal("payloads_settle_ms", 2000L);
            DemoLog.info(name(), "等待设备负载列表推送 " + payloadsSettleMs + "ms ...");
            System.out.println("   等待设备负载列表推送 " + payloadsSettleMs + "ms ...");
            sleep(payloadsSettleMs);

            // ---- Step 1: 获取负载控制权 ----
            step("获取负载控制权");
            call("acquirePayloadControlAuthority",
                cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb),
                defaultTimeout);
            payloadAuthAttempted = true;

            // ---- Step 2: 建立 DRC 链路 ----
            step("建立 DRC 链路");
            syncResult("connectDrcLink", drc::connectDrcLink, defaultTimeout);
            drcConnectAttempted = true;
            long linkSettleMs = params.node("drc").longVal("link_settle_ms", 3000L);
            DemoLog.info(name(), "DRC 建链成功, 等待链路稳定 " + linkSettleMs + "ms ...");
            System.out.println("   DRC 建链成功，等待链路稳定 " + linkSettleMs + "ms ...");
            sleep(linkSettleMs);

            // ---- Step 3: 订阅全景拼接进度 ----
            step("订阅全景拼接进度(subscribePanoStitchProgress)");
            int stitchListenId = media.subscribePanoStitchProgress(
                new FlightHubCallback.PanoStitchProgressCallback() {
                    @Override
                    public void onResult(PanoStitchProgressMsg msg) {
                        if (msg == null) return;
                        int state = msg.getPanoStitchState();
                        int progress = msg.getPanoStitchProgress();
                        long fid = msg.getFileId();
                        DemoLog.info(name(), "[拼接进度] fileId=" + fid
                            + " state=" + state + "(" + describeStitchState(state) + ")"
                            + " progress=" + progress + "%"
                            + " errorCode=" + msg.getPanoErrorCode());
                        System.out.println("   [拼接进度] state=" + state + "(" + describeStitchState(state) + ")"
                            + " progress=" + progress + "%");
                        if (fid > 0) {
                            stitchResultFileId.set(fid);
                        }
                        // state=3 成功 / state=4 失败 / state=5 取消 / state=6 余额不足
                        if (state == 3) {
                            stitchFinalState.set("success");
                            stitchLatch.countDown();
                        } else if (state == 4 || state == 5 || state == 6) {
                            stitchFinalState.set("failed(state=" + state + ")");
                            stitchLatch.countDown();
                        }
                    }
                });
            if (stitchListenId == -1) {
                DemoLog.warn(name(), "订阅拼接进度失败(listenId=-1)，请确认后端已合入 PaaS 进度分流");
                System.out.println("   [warn] 订阅拼接进度失败，拼接完成后需手动查询进度");
            } else {
                subs.add(subscribe("panoStitchProgress", stitchListenId,
                    id -> media.cancelListenPanoStitchProgress(id)));
                System.out.println("   拼接进度订阅成功(listenId=" + stitchListenId + ")");
            }

            // ---- Step 4: 订阅全景拍照状态 + 相机模式/镜头状态 ----
            step("订阅全景拍照状态(listenDrcPanoramaStatus)");
            int panoStatusListenId = camera.listenDrcPanoramaStatus(payloadIndex,
                new FlightHubCallback.PanoramaStatusCallback() {
                    @Override
                    public void onResult(PanoramaStatus status) {
                        if (status == null) return;
                        int percent = status.getPercent();
                        int currentStep = status.getCurrentStep();
                        DemoLog.info(name(), "[全景拍照状态] step=" + currentStep
                            + " percent=" + percent + "%"
                            + " photoState=" + status.getPhotoState());
                        System.out.println("   [全景拍照进度] step=" + currentStep + " percent=" + percent + "%");
                        if (percent >= 100) {
                            shootLatch.countDown();
                        }
                    }
                });
            if (panoStatusListenId < 0) {
                DemoLog.warn(name(), "订阅全景拍照状态失败(listenId=" + panoStatusListenId + ")");
            } else {
                subs.add(subscribe("panoStatus", panoStatusListenId,
                    id -> camera.cancelListenDrcPanoramaStatus(payloadIndex, id)));
                System.out.println("   全景拍照状态订阅成功(listenId=" + panoStatusListenId + ")");
            }

            // 订阅相机状态(cameraMode)：设备侧相机模式可能被驾驶舱页面手动切换等 DRC 之外的渠道
            // 改变；Step 5 切模式前需要感知真实当前模式，避免因状态冲突被拒绝(deviceCode=327009)。
            int cameraStateListenId = camera.listenDrcCameraState(payloadIndex,
                new FlightHubCallback.CameraStateCallback() {
                    @Override
                    public void onResult(CameraState state) {
                        if (state == null) return;
                        int mode = state.getCameraMode();
                        int prev = currentCameraMode.getAndSet(mode);
                        if (prev != mode) {
                            DemoLog.info(name(), "[相机状态] cameraMode=" + mode + " (由 " + prev + " 变化)");
                        }
                    }
                });
            if (cameraStateListenId >= 0) {
                subs.add(subscribe("cameraState", cameraStateListenId,
                    id -> camera.cancelListenDrcCameraState(payloadIndex, id)));
            } else {
                DemoLog.warn(name(), "订阅相机状态失败(listenId=" + cameraStateListenId
                    + "), 无法感知当前相机模式, 切模式前将无条件下发(可能因状态冲突被拒绝)");
            }

            // 订阅直播画面信息(video_type)：对应驾驶舱页面的 drc_live_video_info_push, 用于感知
            // 当前实际镜头(1X/3X/IR 等)。Step 6 切镜头前据此判断是否需要真的下发切换指令。
            int liveVideoInfoListenId = camera.listenDrcLiveVideoInfo(payloadIndex,
                new FlightHubCallback.LiveVideoInfoCallback() {
                    @Override
                    public void onResult(LiveVideoInfo info) {
                        if (info == null) return;
                        int lens = info.getVideoTypeSwig();
                        int prev = currentCameraLens.getAndSet(lens);
                        if (prev != lens) {
                            DemoLog.info(name(), "[直播镜头] videoType=" + lens + " (由 " + prev + " 变化)");
                        }
                    }
                });
            if (liveVideoInfoListenId >= 0) {
                subs.add(subscribe("liveVideoInfo", liveVideoInfoListenId,
                    id -> camera.cancelListenDrcLiveVideoInfo(payloadIndex, id)));
            } else {
                DemoLog.warn(name(), "订阅直播镜头信息失败(listenId=" + liveVideoInfoListenId
                    + "), 无法感知当前镜头, 切镜头前将无条件下发(可能因状态冲突被拒绝)");
            }

            // ---- Step 5: 切换相机模式到全景拍照(PANO_PHOTO) —— 幂等 + settle, 对齐驾驶舱页面 ----
            // drc_camera_photo_take 与普通拍照复用同一 method，靠设备当前 camera_mode 区分行为；
            // 必须先切到 PANO_PHOTO(3)，否则设备会当作普通拍照而非全景拍照执行。
            // 页面(uranus-cockpit-app LiveCameraControlPanel.vue cameraModeSelect)先判断当前状态==
            // 目标状态就跳过, 且切模式后用 cameraTypeChanging 锁定 2500ms 不发任何后续命令, 给设备
            // 状态收敛时间；之前 SDK 连续调用(0 延迟)导致设备返回 deviceCode=327009, 这里补齐同款逻辑。
            step("切换相机模式到全景(PANO_PHOTO)");
            int cachedMode = currentCameraMode.get();
            System.out.println("   当前相机模式(来自 listenDrcCameraState 推送): "
                + (cachedMode == -1 ? "未知" : String.valueOf(cachedMode)));
            if (cachedMode != CameraMode.PANO_PHOTO.getValue()) {
                SwitchCameraModeParams modeParams = new SwitchCameraModeParams();
                modeParams.setPayloadIndex(payloadIndex);
                modeParams.setCameraMode(CameraMode.PANO_PHOTO);
                call("drcSwitchCameraMode(PANO_PHOTO)",
                    cb -> camera.drcSwitchCameraMode(modeParams, cb),
                    defaultTimeout);
                System.out.println("   切相机模式成功: PANO_PHOTO（全景拍照）");
                long modeSettleMs = cfg.longVal("mode_settle_ms", 2500L);
                System.out.println("   等待设备状态收敛 " + modeSettleMs + "ms(对齐页面 cameraTypeChanging 锁定时长) ...");
                sleep(modeSettleMs);
            } else {
                System.out.println("   当前已是 PANO_PHOTO, 跳过切模式(幂等)");
            }

            // ---- Step 6: 切镜头（ZOOM=3X超清 / WIDE=1X球形）—— 幂等 + settle ----
            step("切换镜头");
            int lensTypeVal = cfg.intVal("lens_type", CameraLensType.ZOOM.getValue());
            CameraLensType lensType = CameraLensType.fromValue(lensTypeVal);
            if (lensType == null || lensType == CameraLensType.UNKNOWN) {
                lensType = CameraLensType.ZOOM;
            }
            int cachedLens = currentCameraLens.get();
            System.out.println("   当前镜头(来自 listenDrcLiveVideoInfo 推送): "
                + (cachedLens == -1 ? "未知" : String.valueOf(cachedLens))
                + ", 目标=" + lensType.getValue() + "(" + lensType + ")");
            if (cachedLens != lensType.getValue()) {
                SwitchLiveLensParams lensParams = new SwitchLiveLensParams();
                lensParams.setPayloadIndex(payloadIndex);
                lensParams.setLensType(lensType);
                final CameraLensType finalLensType = lensType;
                call("drcSwitchLiveLens(" + lensType + ")",
                    cb -> camera.drcSwitchLiveLens(lensParams, cb),
                    defaultTimeout);
                System.out.println("   切镜头成功: " + finalLensType
                    + (finalLensType == CameraLensType.ZOOM ? "（3X 超清全景）" : "（1X 球形全景）"));
                long lensSettleMs = cfg.longVal("lens_settle_ms", 1500L);
                System.out.println("   等待设备状态收敛 " + lensSettleMs + "ms ...");
                sleep(lensSettleMs);
            } else {
                System.out.println("   当前镜头已是目标值, 跳过切镜头(幂等)");
            }

            // ---- Step 7: 下发全景拍照指令 ----
            step("下发 drcStartPanorama 启动全景拍照");
            PanoramaParam panoParam = new PanoramaParam();
            panoParam.setPayloadIndex(payloadIndex);
            call("drcStartPanorama",
                cb -> camera.drcStartPanorama(panoParam, cb),
                defaultTimeout);
            System.out.println("   全景拍照指令下发成功，等待设备拍摄完成...");

            // ---- Step 8: 等待全景拍照完成 ----
            step("等待全景拍照完成(timeout=" + shootTimeoutMs + "ms)");
            boolean shootDone = shootLatch.await(shootTimeoutMs, TimeUnit.MILLISECONDS);
            if (!shootDone) {
                throw new FlightHubException("全景拍照超时(" + shootTimeoutMs + "ms)，未收到 percent=100");
            }
            System.out.println("   全景拍照完成！等待后端自动触发拼接...");
            DemoLog.info(name(), "全景拍照完成，等待后端自动拼接触发");

            // ---- Step 9: 等待拼接完成 ----
            step("等待后端自动触发全景拼接(timeout=" + stitchTimeoutMs + "ms)");
            boolean stitchDone = stitchLatch.await(stitchTimeoutMs, TimeUnit.MILLISECONDS);
            if (!stitchDone) {
                throw new FlightHubException("全景拼接超时(" + stitchTimeoutMs + "ms)，未收到完成推送");
            }
            String finalState = stitchFinalState.get();
            long resultFileId = stitchResultFileId.get();
            DemoLog.info(name(), "拼接结束: state=" + finalState + " resultFileId=" + resultFileId);
            System.out.println("   拼接结束: " + finalState + ", 成片 fileId=" + resultFileId);

            if (!"success".equals(finalState)) {
                throw new FlightHubException("全景拼接未成功: " + finalState);
            }

            // ---- Step 10: 获取瓦片 URL ----
            step("获取瓦片 URL(getPanoTierTileUrl, fileId=" + resultFileId + ")");
            final Await<FlightHubError> urlAck = new Await<>(name() + ".getPanoTierTileUrl");
            final AtomicReference<GetPanoTierTileUrlResult> urlRef = new AtomicReference<>();
            runOutcome("getPanoTierTileUrl", defaultTimeout, urlAck, () ->
                media.getPanoTierTileUrl(resultFileId,
                    new FlightHubCallback.GetPanoTierTileUrlCallback() {
                        @Override
                        public void onResult(FlightHubError error, GetPanoTierTileUrlResult r) {
                            urlRef.set(r);
                            urlAck.set(error);
                        }
                    }));
            GetPanoTierTileUrlResult urlResult = urlRef.get();
            if (urlResult != null) {
                System.out.println("   瓦片 URL:");
                System.out.println("     baseUrl=" + urlResult.getBaseUrl());
                Map<String, String> qp = urlResult.getQueryParams();
                if (qp != null && !qp.isEmpty()) {
                    System.out.println("     queryParams=" + qp);
                }
                DemoLog.info(name(), "getPanoTierTileUrl success: baseUrl=" + urlResult.getBaseUrl()
                    + " queryParams=" + qp);
            }

            System.out.println("\n   ✓ 驾驶舱全景拍照 → 自动拼接 → 获取瓦片 URL 全流程完成！");
            DemoLog.info(name(), "DrcPanoScenario 全流程完成");

        } finally {
            // ---- 收尾：取消订阅 → 断 DRC → 释放控制权 ----
            step("收尾清理");
            for (Subscription sub : subs) {
                try { sub.close(); registry().remove(sub); } catch (Exception ignored) {}
            }
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路",
                    () -> syncResult("disconnectDrcLink", drc::disconnectDrcLink, defaultTimeout));
            }
            if (payloadAuthAttempted) {
                quiet("释放负载控制权",
                    () -> call("releasePayloadControlAuthority",
                        cb -> auth.releasePayloadControlAuthority(payloadIndex, cb),
                        defaultTimeout));
            }
        }
    }

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
}
