// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 使用 FFmpeg 通过 stdin 管道推送 H264 裸码流至 Oryx（WHIP / WebRTC 推流）。
 * 仅依赖命令行 ffmpeg（需支持 -f whip），不依赖 JavaCV。
 */
public class FFmpegPipeStreamer {
    /** ffmpeg 管道输入名义帧率；分辨率/码率由 H.264 码流自带（copy 不转码） */
    private static final int INPUT_FPS = 30;

    private Process process;
    private OutputStream ffmpegStdin;
    private final LinkedBlockingQueue<byte[]> frameQueue = new LinkedBlockingQueue<>();
    private Thread writerThread;
    private Thread monitorThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private String whipUrl;
    private String configuredInputFormat = "h264";
    private boolean configuredTranscodeToH264 = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private long frameCount = 0;

    private static final int MAX_QUEUE_SIZE = 300;
    private static final AtomicLong droppedFrames = new AtomicLong(0);

    /**
     * 启动 ffmpeg 进程，准备 WHIP 推流。
     *
     * @param whipUrl Oryx WHIP 地址
     */
    public synchronized void start(String whipUrl, String inputFormat, boolean transcodeToH264) throws IOException {
        if (running.get()) {
            return;
        }

        this.whipUrl = whipUrl;
        this.configuredInputFormat = normalizeInputFormat(inputFormat);
        this.configuredTranscodeToH264 = transcodeToH264;
        this.reconnectAttempts = 0;

        startFFmpegProcess(this.configuredInputFormat, this.configuredTranscodeToH264);
        running.set(true);

        writerThread = new Thread(this::drainAndWrite, "ffmpeg-writer");
        writerThread.setDaemon(true);
        writerThread.start();

        monitorThread = new Thread(this::monitorProcess, "ffmpeg-monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void startFFmpegProcess(String inputFormat, boolean transcodeToH264) throws IOException {
        // 裸 Annex-B(h264/hevc) -> copy/转码 -> WHIP（WebRTC 推流至 Oryx）
        // 调用方应保证首包含参数集的关键帧，否则 hevc 会 PPS id out of range
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-loglevel");
        cmd.add("warning");
        cmd.add("-y");
        cmd.add("-fflags");
        cmd.add("+genpts+discardcorrupt+flush_packets");
        cmd.add("-err_detect");
        cmd.add("ignore_err");
        cmd.add("-probesize");
        cmd.add("5M");
        cmd.add("-analyzeduration");
        cmd.add("5M");
        cmd.add("-f");
        cmd.add(normalizeInputFormat(inputFormat));
        cmd.add("-r");
        cmd.add(String.valueOf(INPUT_FPS));
        cmd.add("-i");
        cmd.add("pipe:0");
        if (transcodeToH264) {
            cmd.add("-c:v");
            cmd.add("libx264");
            cmd.add("-preset");
            cmd.add("veryfast");
            cmd.add("-tune");
            cmd.add("zerolatency");
            cmd.add("-pix_fmt");
            cmd.add("yuv420p");
            cmd.add("-g");
            cmd.add("30");
            cmd.add("-bf");
            cmd.add("0");
        } else {
            cmd.add("-c:v");
            cmd.add("copy");
        }
        cmd.add("-f");
        cmd.add("whip");
        cmd.add(whipUrl);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        process = pb.start();
        ffmpegStdin = process.getOutputStream();

        Thread errorReader = new Thread(this::readErrorOutput, "ffmpeg-error-reader");
        errorReader.setDaemon(true);
        errorReader.start();
    }

    public synchronized void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            monitorThread = null;
        }

        if (writerThread != null) {
            writerThread.interrupt();
            try {
                writerThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            writerThread = null;
        }

        if (ffmpegStdin != null) {
            try {
                ffmpegStdin.flush();
                ffmpegStdin.close();
            } catch (IOException ignored) {
            }
            ffmpegStdin = null;
        }

        if (process != null) {
            try {
                process.getOutputStream().close();
            } catch (Exception ignored) {
            }
            process.destroy();
            try {
                boolean finished = process.waitFor(2, TimeUnit.SECONDS);
                if (!finished) {
                    System.err.println("[FFmpegPipeStreamer] FFmpeg 未在 2 秒内结束，强制销毁");
                    process.destroyForcibly();
                    try {
                        process.waitFor(500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            process = null;
        }

        frameQueue.clear();
    }

    public void pushFrame(byte[] data) {
        if (!running.get() || data == null || data.length == 0) {
            return;
        }

        frameCount++;

        if (frameQueue.size() >= MAX_QUEUE_SIZE) {
            droppedFrames.incrementAndGet();
            if (droppedFrames.get() % 50 == 0) {
                System.err.println("[FFmpegPipeStreamer] 队列满，已丢弃 " + droppedFrames.get()
                        + " 帧，当前队列: " + frameQueue.size());
            }
            return;
        }

        if (frameCount % 100 == 0) {
            System.out.println("[FFmpegPipeStreamer] 第 " + frameCount + " 帧，"
                    + data.length + " bytes，队列: " + frameQueue.size());
        }

        frameQueue.offer(data);
    }

    public boolean isRunning() {
        return running.get();
    }

    private void drainAndWrite() {
        try {
            while (running.get()) {
                byte[] frame = frameQueue.poll(1, TimeUnit.SECONDS);
                if (frame == null) {
                    continue;
                }
                if (ffmpegStdin != null) {
                    ffmpegStdin.write(frame);
                    ffmpegStdin.flush();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("[FFmpegPipeStreamer] 写入失败: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private void readErrorOutput() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
                if (line.contains("Connection refused")
                        || line.contains("Connection timed out")
                        || line.contains("Server returned 404")
                        || line.contains("Invalid data found")
                        || line.contains("Connection lost")
                        || line.contains("Broken pipe")
                        || line.contains("WHIP")
                        || line.contains("handshake")) {
                    System.err.println("[FFmpeg ERROR] " + line);
                    scheduleReconnect();
                }
            }
        } catch (IOException e) {
            System.err.println("[FFmpeg] 读取输出失败: " + e.getMessage());
        }
    }

    private void monitorProcess() {
        while (running.get()) {
            try {
                if (process != null && !process.isAlive()) {
                    int exitCode = process.exitValue();
                    System.err.println("[FFmpeg] 进程退出，码: " + exitCode);
                    if (exitCode != 0) {
                        scheduleReconnect();
                    }
                    break;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void scheduleReconnect() {
        // 中途重连会丢掉 VPS/SPS/PPS，HEVC 会一直 PPS id out of range。
        // 鉴权/网络失败时直接停，由上层重新开流（会再次等到参数集 AU）。
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            System.err.println("[FFmpeg] WHIP 失败次数超限，停止推流（请检查 Oryx secret / 网络后重新开流）");
            stop();
            return;
        }
        reconnectAttempts++;
        System.err.println("[FFmpeg] WHIP 异常，停止推流（不自动重连，避免无参数集开解） attempt="
                + reconnectAttempts);
        stop();
    }

    private String normalizeInputFormat(String inputFormat) {
        if (inputFormat == null) {
            return "h264";
        }
        String v = inputFormat.trim().toLowerCase();
        if ("hevc".equals(v) || "h265".equals(v)) {
            return "hevc";
        }
        return "h264";
    }
}
