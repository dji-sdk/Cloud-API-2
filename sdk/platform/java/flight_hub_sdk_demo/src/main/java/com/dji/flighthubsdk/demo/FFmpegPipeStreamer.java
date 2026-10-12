// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

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
 * 使用 FFmpeg 通过 stdin 管道推送 H264 裸码流至 RTMP 服务器。
 * 仅依赖命令行 ffmpeg，不依赖 JavaCV。
 */
public class FFmpegPipeStreamer {
    private Process process;
    private OutputStream ffmpegStdin;
    private final LinkedBlockingQueue<byte[]> frameQueue = new LinkedBlockingQueue<>();
    private Thread writerThread;
    private Thread monitorThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 重连相关参数
    private String rtmpUrl;
    private int fps, width, height, bitrate;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private long frameCount = 0;
    
    // 缓冲区控制参数
    private static final int MAX_QUEUE_SIZE = 300; // 最大队列大小，防止内存堆积
    private static final AtomicLong droppedFrames = new AtomicLong(0);

    /**
     * 启动 ffmpeg 进程，准备推流。
     *
     * @param rtmpUrl  目标 RTMP 地址（形如 rtmp://host/app/streamKey）
     * @param fps      帧率
     * @param width    宽（可选，仅用于日志或未来扩展）
     * @param height   高（可选，仅用于日志或未来扩展）
     * @param bitrate  码率（bps），仅用于未来扩展，如需转码
     * @throws IOException 进程启动失败
     */
    public synchronized void start(String rtmpUrl, int fps, int width, int height, int bitrate) throws IOException {
        if (running.get()) {
            return;
        }

        // 保存参数用于重连
        this.rtmpUrl = rtmpUrl;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.reconnectAttempts = 0;

        startFFmpegProcess();
        running.set(true);
        
        // 启动写入线程
        writerThread = new Thread(this::drainAndWrite, "ffmpeg-writer");
        writerThread.setDaemon(true);
        writerThread.start();
        
        // 启动监控线程
        monitorThread = new Thread(this::monitorProcess, "ffmpeg-monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void startFFmpegProcess() throws IOException {
        // 基于裸 H264 码流，直接封装为 FLV 推 RTMP，尽量避免转码开销
        // -f h264 指定输入格式，-r 指定输入帧率，-i pipe:0 指定从 stdin 读取
        // -c copy 直接拷贝视频流，-f flv 指定输出封装格式
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-loglevel");
        cmd.add("error"); // 改回error级别，减少输出
        cmd.add("-y");
        // 去掉 -re 参数, 拉流的地方已经对速率做了控制，推流不需要控制了
        // cmd.add("-re");
        cmd.add("-fflags");
        cmd.add("+flush_packets");
        cmd.add("-f");
        cmd.add("h264");
        cmd.add("-r");
        cmd.add(String.valueOf(Math.max(1, fps)));
        cmd.add("-i");
        cmd.add("pipe:0");
        cmd.add("-c:v");
        cmd.add("copy");
        cmd.add("-f");
        cmd.add("flv");
        // 添加缓冲区控制参数，防止延迟累积
        cmd.add("-max_muxing_queue_size");
        cmd.add("1024"); // 限制输出队列大小
        cmd.add("-bufsize");
        cmd.add("1M"); // 设置缓冲区大小为1MB
        cmd.add(rtmpUrl);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // 合并 stderr -> stdout，便于排查
        process = pb.start();
        ffmpegStdin = process.getOutputStream();
        
        // 启动错误输出读取线程
        Thread errorReader = new Thread(this::readErrorOutput, "ffmpeg-error-reader");
        errorReader.setDaemon(true);
        errorReader.start();
    }

    /**
     * 停止推流并清理资源。
     */
    public synchronized void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        // 结束监控线程
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            monitorThread = null;
        }

        // 结束写入线程
        if (writerThread != null) {
            writerThread.interrupt();
            try {
                writerThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            writerThread = null;
        }

        // 关闭 stdin 以通知 ffmpeg 结束
        if (ffmpegStdin != null) {
            try {
                ffmpegStdin.flush();
                ffmpegStdin.close();
            } catch (IOException ignored) {
            }
            ffmpegStdin = null;
        }

        // 终止进程
        if (process != null) {
            try {
                process.getOutputStream().close();
            } catch (Exception ignored) {
            }
            process.destroy();
            try {
                // 使用超时避免无限等待
                boolean finished = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                if (!finished) {
                    System.err.println("[FFmpegPipeStreamer] FFmpeg进程未在2秒内结束，强制销毁");
                    process.destroyForcibly();
                    try {
                        process.waitFor(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            process = null;
        }

        // 清空残留帧
        frameQueue.clear();
    }

    /**
     * 推送一帧 H264 数据（Annex B 格式）。
     * 非阻塞，数据会放入队列由后台线程写入。
     *
     * @param data 裸 H264 字节数组
     */
    public void pushFrame(byte[] data) {
        if (!running.get() || data == null || data.length == 0) {
            return;
        }
        
        frameCount++;
        
        // 检查队列大小，如果队列满了就丢包
        if (frameQueue.size() >= MAX_QUEUE_SIZE) {
            droppedFrames.incrementAndGet();
            if (droppedFrames.get() % 50 == 0) { // 每丢50帧打印一次
                System.err.println("[FFmpegPipeStreamer] 队列满，已丢弃 " + droppedFrames.get() + " 帧，当前队列大小: " + frameQueue.size());
            }
            return;
        }
        
        // 添加调试日志
        if (frameCount % 100 == 0) { // 每100帧打印一次
            System.out.println("[FFmpegPipeStreamer] 推送第" + frameCount + "帧，大小: " + data.length + " bytes，队列大小: " + frameQueue.size());
        }
        
        // 尽量避免阻塞回调线程
        frameQueue.offer(data);
    }

    /**
     * 检查推流是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    private void drainAndWrite() {
        try {
            while (running.get()) {
                byte[] frame = frameQueue.poll(1, TimeUnit.SECONDS); // 使用超时避免无限阻塞
                if (frame == null) {
                    continue; // 超时继续循环
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

    /**
     * 读取FFmpeg的错误输出
     */
    private void readErrorOutput() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
                // 检查关键错误信息
                if (line.contains("Connection refused") || 
                    line.contains("Connection timed out") ||
                    line.contains("Server returned 404") ||
                    line.contains("Invalid data found") ||
                    line.contains("Connection lost") ||
                    line.contains("Broken pipe")) {
                    System.err.println("[FFmpeg ERROR] " + line);
                    // 触发重连
                    scheduleReconnect();
                }
            }
        } catch (IOException e) {
            System.err.println("[FFmpeg] 读取输出失败: " + e.getMessage());
        }
    }

    /**
     * 监控FFmpeg进程状态
     */
    private void monitorProcess() {
        while (running.get()) {
            try {
                if (process != null && !process.isAlive()) {
                    int exitCode = process.exitValue();
                    System.err.println("[FFmpeg] 进程异常退出，退出码: " + exitCode);
                    if (exitCode != 0) {
                        scheduleReconnect();
                    }
                    break;
                }
                Thread.sleep(1000); // 每秒检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            System.out.println("[FFmpeg] 尝试重连，第 " + reconnectAttempts + " 次");
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // 等待2秒
                    if (running.get()) {
                        stop();
                        Thread.sleep(1000);
                        start(rtmpUrl, fps, width, height, bitrate);
                    }
                } catch (Exception e) {
                    System.err.println("[FFmpeg] 重连失败: " + e.getMessage());
                }
            }).start();
        } else {
            System.err.println("[FFmpeg] 重连次数超限，停止推流");
            stop();
        }
    }
}


