// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.config.LiveProperties;
import com.dji.fhsdk.demo.config.OryxProperties;
import com.dji.fhsdk.demo.live.FFmpegPipeStreamer;
import com.dji.fhsdk.demo.model.DeviceContext;
import com.dji.fhsdk.demo.model.StreamUrlResponse;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 直播：SDK 拉流 → Annex-B（H264 或 HEVC）经 FFmpeg WHIP 推送到 Oryx → 前端 WHEP 播放。
 * <p>
 * 编码按码流自动识别（也可配置强制 h264/hevc）；HEVC 可选转 H264 再推 WHIP。
 * {@link DeviceContext#getLiveModule()} 在 startLive 后创建，供 SEI 订阅与 stopLive 释放。
 */
@Service
public class LiveService {

    private static final Logger log = LoggerFactory.getLogger(LiveService.class);

    private enum VideoCodec { H264, HEVC }

    private final OryxProperties oryxProperties;
    private final LiveProperties liveProperties;
    private final PayloadService payloadService;
    private final SeiSubscriptionService seiSubscriptionService;

    public LiveService(OryxProperties oryxProperties, LiveProperties liveProperties, PayloadService payloadService,
                       SeiSubscriptionService seiSubscriptionService) {
        this.oryxProperties = oryxProperties;
        this.liveProperties = liveProperties;
        this.payloadService = payloadService;
        this.seiSubscriptionService = seiSubscriptionService;
    }

    public void startLive(DeviceContext ctx) {
        Drone drone = ctx.getDrone();
        if (drone == null) {
            throw new IllegalStateException("drone not set");
        }
        if (ctx.getLiveModule() != null) {
            throw new IllegalStateException("live already started");
        }
        payloadService.requireConfigured(ctx);

        LiveInfo liveInfo = ctx.getSelectedLiveInfo();
        String streamKey = "demo_" + ctx.getGatewaySn();

        log.info("Starting drone live gateway={} drone={} payload={} videoId={}",
                ctx.getGatewaySn(), ctx.getDroneSn(), liveInfo.getPayloadIndex(), liveInfo.getVideoId());

        LiveModule liveModule = drone.getLiveStream(liveInfo);
        ctx.setLiveModule(liveModule);

        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<FlightHubError> startError = new AtomicReference<>();
        liveModule.start(error -> {
            startError.set(error);
            startLatch.countDown();
        });

        try {
            if (!startLatch.await(20, TimeUnit.SECONDS)) {
                throw new IllegalStateException("live start timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("live start interrupted", e);
        }
        if (startError.get() == null || !startError.get().isSuccess()) {
            ctx.setLiveModule(null);
            throw new IllegalStateException("live start failed: " + startError.get());
        }

        // 等参数集 AU 再启 FFmpeg；HEVC 转 H264 时还需 IDR/关键帧，避免从 P 帧中间解码
        FFmpegPipeStreamer streamer = new FFmpegPipeStreamer();
        String whipUrl = buildWhipUrl(streamKey);
        String formatHint = liveProperties.getInputFormat();
        boolean transcodeHevc = liveProperties.isTranscodeToH264();
        ctx.setStreamer(streamer);

        AtomicLong frameCount = new AtomicLong();
        AtomicReference<Boolean> ffmpegStarted = new AtomicReference<>(false);
        Object startLock = new Object();
        int streamListenId = liveModule.subscribeLiveStream((data, frameInfo) -> {
            if (data == null || data.length == 0) {
                return;
            }
            synchronized (startLock) {
                if (Boolean.TRUE.equals(ffmpegStarted.get())) {
                    if (!streamer.isRunning()) {
                        return;
                    }
                    long n = frameCount.incrementAndGet();
                    if (n == 1 || n % 100 == 0) {
                        boolean key = frameInfo != null && frameInfo.isKeyFrame();
                        log.info("live frame #{} bytes={} key={}", n, data.length, key);
                    }
                    streamer.pushFrame(data);
                    return;
                }
                boolean keyFrame = frameInfo != null && frameInfo.isKeyFrame();
                VideoCodec codec = resolveCodec(formatHint, data);
                if (codec == null) {
                    long dropped = frameCount.incrementAndGet();
                    if (dropped == 1 || dropped % 50 == 0) {
                        log.info("waiting bootstrap dropped={} hint={} sdkKey={} headHex={}",
                                dropped, formatHint, keyFrame, hexPrefix(data, 24));
                    }
                    return;
                }
                String ffmpegFormat = codec == VideoCodec.HEVC ? "hevc" : "h264";
                boolean doTranscode = codec == VideoCodec.HEVC && transcodeHevc;
                if (!canStartFfmpeg(data, codec, keyFrame, doTranscode)) {
                    long dropped = frameCount.incrementAndGet();
                    if (dropped == 1 || dropped % 50 == 0) {
                        log.info("waiting bootstrap dropped={} hint={} codec={} transcode={} sdkKey={} nals={}",
                                dropped, formatHint, codec, doTranscode, keyFrame,
                                summarizeAnnexBNals(data, codec, 8));
                    }
                    return;
                }
                try {
                    streamer.start(whipUrl, ffmpegFormat, doTranscode);
                    streamer.pushFrame(data);
                    ffmpegStarted.set(true);
                    log.info("FFmpeg started bytes={} codec={} ffmpegFormat={} transcode={} sdkKey={} nals={}",
                            data.length, codec, ffmpegFormat, doTranscode, keyFrame,
                            summarizeAnnexBNals(data, codec, 12));
                } catch (Exception e) {
                    log.error("ffmpeg WHIP start failed: {}", e.getMessage(), e);
                }
            }
        });
        ctx.setLiveStreamListenId(streamListenId);
        ctx.setStreamKey(streamKey);
        ctx.setWhepPlayUrl(oryxProperties.getWhepUrlTemplate().replace("{streamKey}", streamKey));
        seiSubscriptionService.subscribe(ctx);
        log.info("Live subscribed gateway={} whip={} whep={} formatHint={}",
                ctx.getGatewaySn(), whipUrl, ctx.getWhepPlayUrl(), formatHint);
    }

    private String buildWhipUrl(String streamKey) {
        String url = oryxProperties.getWhipUrlTemplate().replace("{streamKey}", streamKey);
        String secret = oryxProperties.getPublishSecret();
        if (secret != null && !secret.isBlank() && !url.contains("secret=")) {
            url += (url.contains("?") ? "&" : "?") + "secret=" + secret.trim();
        }
        return url;
    }

    public void stopLive(DeviceContext ctx) {
        if (ctx == null) return;
        LiveModule live = ctx.getLiveModule();

        if (ctx.getLiveStreamListenId() >= 0 && live != null) {
            try { live.unSubscribeLiveStream(ctx.getLiveStreamListenId()); } catch (Exception ignored) {}
            ctx.setLiveStreamListenId(-1);
        }
        seiSubscriptionService.unsubscribe(ctx);
        if (live != null) {
            try { live.stop(); } catch (Exception ignored) {}
            ctx.setLiveModule(null);
        }
        if (ctx.getStreamer() != null) {
            ctx.getStreamer().stop();
            ctx.setStreamer(null);
        }
    }

    public StreamUrlResponse getStreamUrl(DeviceContext ctx) {
        if (ctx == null || ctx.getLiveModule() == null || ctx.getWhepPlayUrl() == null) {
            throw new IllegalStateException("直播未就绪（飞机离线或未开流）");
        }
        return new StreamUrlResponse(ctx.getWhepPlayUrl(), ctx.getStreamKey(), ctx.getPayloadIndex());
    }

    /** 配置强制编码，或从 Annex-B 首个可识别 NAL 推断；尚无法判断时返回 null。 */
    private static VideoCodec resolveCodec(String formatHint, byte[] data) {
        String hint = formatHint == null ? "auto" : formatHint.trim().toLowerCase();
        if ("hevc".equals(hint) || "h265".equals(hint)) {
            return VideoCodec.HEVC;
        }
        if ("h264".equals(hint) || "avc".equals(hint)) {
            return VideoCodec.H264;
        }
        return detectAnnexBCodec(data);
    }

    /**
     * 扫描 Annex-B：出现 HEVC VPS/SPS/PPS → HEVC；否则出现 H264 SPS/PPS → H264。
     */
    private static VideoCodec detectAnnexBCodec(byte[] data) {
        boolean sawH264 = false;
        int pos = 0;
        while (true) {
            int sc = findStartCode(data, pos);
            if (sc < 0) {
                break;
            }
            int scLen = startCodeLength(data, sc);
            int nalPos = sc + scLen;
            if (nalPos >= data.length) {
                break;
            }
            int hevcType = (data[nalPos] >> 1) & 0x3F;
            int h264Type = data[nalPos] & 0x1F;
            if (hevcType == 32 || hevcType == 33 || hevcType == 34) {
                return VideoCodec.HEVC;
            }
            if (h264Type == 7 || h264Type == 8) {
                sawH264 = true;
            }
            pos = nalPos + 1;
        }
        return sawH264 ? VideoCodec.H264 : null;
    }

    /** H264：SPS+PPS；HEVC 转码：VPS+SPS+PPS 且 SDK 关键帧或 Annex-B IDR/CRA。 */
    private static boolean canStartFfmpeg(byte[] data, VideoCodec codec, boolean sdkKeyFrame, boolean transcodeHevc) {
        if (!hasBootstrapParamSets(data, codec)) {
            return false;
        }
        if (codec == VideoCodec.HEVC && transcodeHevc) {
            return sdkKeyFrame || hasRandomAccessNal(data, codec);
        }
        return true;
    }

    private static boolean hasRandomAccessNal(byte[] data, VideoCodec codec) {
        int pos = 0;
        while (true) {
            int sc = findStartCode(data, pos);
            if (sc < 0) {
                break;
            }
            int scLen = startCodeLength(data, sc);
            int nalPos = sc + scLen;
            if (nalPos >= data.length) {
                break;
            }
            if (codec == VideoCodec.HEVC) {
                int t = (data[nalPos] >> 1) & 0x3F;
                if (t == 19 || t == 20 || t == 21) {
                    return true;
                }
            } else {
                int t = data[nalPos] & 0x1F;
                if (t == 5) {
                    return true;
                }
            }
            pos = nalPos + 1;
        }
        return false;
    }

    private static boolean hasBootstrapParamSets(byte[] data, VideoCodec codec) {
        boolean vps = false, sps = false, pps = false;
        int pos = 0;
        while (true) {
            int sc = findStartCode(data, pos);
            if (sc < 0) {
                break;
            }
            int scLen = startCodeLength(data, sc);
            int nalPos = sc + scLen;
            if (nalPos >= data.length) {
                break;
            }
            if (codec == VideoCodec.HEVC) {
                int t = (data[nalPos] >> 1) & 0x3F;
                if (t == 32) vps = true;
                else if (t == 33) sps = true;
                else if (t == 34) pps = true;
                if (vps && sps && pps) {
                    return true;
                }
            } else {
                int t = data[nalPos] & 0x1F;
                if (t == 7) sps = true;
                else if (t == 8) pps = true;
                if (sps && pps) {
                    return true;
                }
            }
            pos = nalPos + 1;
        }
        return false;
    }

    private static String summarizeAnnexBNals(byte[] data, VideoCodec codec, int max) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int count = 0;
        while (count < max) {
            int sc = findStartCode(data, pos);
            if (sc < 0) {
                break;
            }
            int scLen = startCodeLength(data, sc);
            int nalPos = sc + scLen;
            if (nalPos >= data.length) {
                break;
            }
            int nalType = codec == VideoCodec.HEVC
                    ? ((data[nalPos] >> 1) & 0x3F)
                    : (data[nalPos] & 0x1F);
            if (count > 0) {
                sb.append(',');
            }
            sb.append(nalType);
            count++;
            pos = nalPos + 1;
        }
        return sb.toString();
    }

    private static int startCodeLength(byte[] data, int sc) {
        return (sc + 2 < data.length && data[sc + 2] == 1) ? 3 : 4;
    }

    private static int findStartCode(byte[] data, int from) {
        for (int i = Math.max(0, from); i + 3 < data.length; i++) {
            if (data[i] != 0 || data[i + 1] != 0) {
                continue;
            }
            if (data[i + 2] == 1) {
                return i;
            }
            if (data[i + 2] == 0 && data[i + 3] == 1) {
                return i;
            }
        }
        return -1;
    }

    private static String hexPrefix(byte[] data, int n) {
        if (data == null) {
            return "";
        }
        int len = Math.min(n, data.length);
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", data[i] & 0xFF));
        }
        return sb.toString();
    }
}
