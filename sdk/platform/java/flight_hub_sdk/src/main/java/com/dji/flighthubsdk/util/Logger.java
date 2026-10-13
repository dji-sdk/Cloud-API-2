// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 日志工具类
 * 支持将日志保存到本地文件，支持日志级别和文件滚动
 */
public class Logger {
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG(0, "DEBUG"),
        INFO(1, "INFO"),
        WARN(2, "WARN"),
        ERROR(3, "ERROR");
        
        private final int level;
        private final String name;
        
        LogLevel(int level, String name) {
            this.level = level;
            this.name = name;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getName() {
            return name;
        }
    }
    
    // 使用 volatile 确保多线程可见性，避免加锁读取
    private static volatile LogLevel currentLevel = LogLevel.INFO;
    private static volatile boolean initialized = false;
    
    private static String logDir = "./logs";
    private static String logFileName = "flight_hub_sdk";
    private static boolean logToFile = true;
    private static boolean logToConsole = true;
    private static long maxFileSize = 10 * 1024 * 1024; // 10MB
    private static int maxFiles = 5;
    
    private static final ReentrantLock lock = new ReentrantLock();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static PrintWriter writer = null;
    private static String currentLogFile = null;
    private static long currentFileSize = 0;
    
    /**
     * 初始化日志系统
     * @param logDir 日志目录
     * @param logFileName 日志文件名（不含扩展名）
     * @param logLevel 日志级别
     * @param logToFile 是否写入文件
     * @param logToConsole 是否输出到控制台
     * @param maxFileSize 单个日志文件最大大小（字节）
     * @param maxFiles 保留的最大日志文件数
     */
    public static void initialize(String logDir, String logFileName, LogLevel logLevel, 
                                  boolean logToFile, boolean logToConsole,
                                  long maxFileSize, int maxFiles) {
        lock.lock();
        try {
            Logger.logDir = logDir != null ? logDir : "./logs";
            Logger.logFileName = logFileName != null ? logFileName : "flight_hub_sdk";
            Logger.currentLevel = logLevel != null ? logLevel : LogLevel.INFO;
            Logger.logToFile = logToFile;
            Logger.logToConsole = logToConsole;
            Logger.maxFileSize = maxFileSize > 0 ? maxFileSize : 10 * 1024 * 1024;
            Logger.maxFiles = maxFiles > 0 ? maxFiles : 5;
            Logger.initialized = true;
            
            // 关闭之前的日志文件（如果存在）
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // 忽略关闭错误
                }
                writer = null;
            }
            
            if (logToFile) {
                ensureLogDirectory();
                openLogFile();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 使用默认配置初始化
     */
    public static void initialize() {
        initialize(null, null, LogLevel.INFO, true, true, 10 * 1024 * 1024, 5);
    }
    
    /**
     * 设置日志级别
     * @param level 参数
     */
    public static void setLogLevel(LogLevel level) {
        lock.lock();
        try {
            currentLevel = level != null ? level : LogLevel.INFO;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 设置日志级别（字符串形式）
     * @param level 参数
     */
    public static void setLogLevel(String level) {
        if (level == null) {
            return;
        }
        try {
            setLogLevel(LogLevel.valueOf(level.toUpperCase()));
        } catch (IllegalArgumentException e) {
            warn("Invalid log level: " + level + ", using INFO");
            setLogLevel(LogLevel.INFO);
        }
    }
    
    /**
     * 关闭日志系统
     */
    public static void shutdown() {
        lock.lock();
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
            }
        } catch (Exception e) {
            // 忽略关闭时的错误
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 记录DEBUG级别日志
     * @param tag 参数
     * @param message 参数
     */
    public static void debug(String tag, String message) {
        log(LogLevel.DEBUG, tag, message, null);
    }
    
    /**
     * 记录INFO级别日志
     * @param tag 参数
     * @param message 参数
     */
    public static void info(String tag, String message) {
        log(LogLevel.INFO, tag, message, null);
    }
    
    /**
     * 记录WARN级别日志
     * @param tag 参数
     * @param message 参数
     */
    public static void warn(String tag, String message) {
        log(LogLevel.WARN, tag, message, null);
    }
    
    /**
     * 记录ERROR级别日志
     * @param tag 参数
     * @param message 参数
     */
    public static void error(String tag, String message) {
        log(LogLevel.ERROR, tag, message, null);
    }
    
    /**
     * 记录ERROR级别日志（带异常）
     * @param tag 参数
     * @param message 参数
     * @param throwable 参数
     */
    public static void error(String tag, String message, Throwable throwable) {
        log(LogLevel.ERROR, tag, message, throwable);
    }
    
    /**
     * 记录日志（无tag版本）
     * @param message 参数
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, "", message, null);
    }
    
    public static void info(String message) {
        log(LogLevel.INFO, "", message, null);
    }
    
    public static void warn(String message) {
        log(LogLevel.WARN, "", message, null);
    }
    
    public static void error(String message) {
        log(LogLevel.ERROR, "", message, null);
    }
    
    public static void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, "", message, throwable);
    }
    
    /**
     * 核心日志记录方法
     */
    private static void log(LogLevel level, String tag, String message, Throwable throwable) {
        if (level.getLevel() < currentLevel.getLevel()) {
            return;
        }
        
        // 如果还未初始化，只输出到控制台，不写入文件
        // 这样可以避免在 SDK 初始化之前创建日志文件
        if (!initialized) {
            // 只输出到控制台（使用默认的 logToConsole = true）
            String timestamp = dateFormat.format(new Date());
            String tagStr = tag != null && !tag.isEmpty() ? "[" + tag + "] " : "";
            long threadId = Thread.currentThread().getId();
            String logMessage = String.format("%s [%s] %s[Thread-%d] %s", timestamp, level.getName(), tagStr, threadId, message);
            if (level == LogLevel.ERROR) {
                System.err.println(logMessage);
                if (throwable != null) {
                    throwable.printStackTrace(System.err);
                }
            } else {
                System.out.println(logMessage);
            }
            return;
        }
        
        lock.lock();
        try {
            String timestamp = dateFormat.format(new Date());
            String tagStr = tag != null && !tag.isEmpty() ? "[" + tag + "] " : "";
            long threadId = Thread.currentThread().getId();
            String logMessage = String.format("%s [%s] %s[Thread-%d] %s", timestamp, level.getName(), tagStr, threadId, message);
            
            // 输出到控制台
            if (logToConsole) {
                if (level == LogLevel.ERROR) {
                    System.err.println(logMessage);
                    if (throwable != null) {
                        throwable.printStackTrace(System.err);
                    }
                } else {
                    System.out.println(logMessage);
                }
            }
            
            // 写入文件
            if (logToFile) {
                ensureLogFile();
                if (writer != null) {
                    writer.println(logMessage);
                    if (throwable != null) {
                        throwable.printStackTrace(writer);
                    }
                    // 注意：这里不立即 flush，让系统缓冲，减少磁盘IO阻塞
                    // 只在 ERROR 级别或文件滚动时强制刷新，确保关键日志不丢失
                    if (level == LogLevel.ERROR) {
                        writer.flush(); // ERROR 级别立即刷新，确保不丢失
                    }
                    currentFileSize += logMessage.length() + 1; // +1 for newline
                    
                    // 检查文件大小，需要滚动
                    if (currentFileSize >= maxFileSize) {
                        rollLogFile();
                    }
                }
            }
        } catch (Exception e) {
            // 日志记录失败时，至少输出到控制台
            System.err.println("[Logger] Failed to write log: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 确保日志目录存在
     */
    private static void ensureLogDirectory() {
        try {
            Path dir = Paths.get(logDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("[Logger] Failed to create log directory: " + e.getMessage());
        }
    }
    
    /**
     * 确保日志文件已打开
     */
    private static void ensureLogFile() {
        if (writer == null) {
            openLogFile();
        }
    }
    
    /**
     * 打开日志文件
     */
    private static void openLogFile() {
        try {
            ensureLogDirectory();
            String dateStr = fileDateFormat.format(new Date());
            String fileName = logFileName + "_" + dateStr + ".log";
            currentLogFile = Paths.get(logDir, fileName).toString();
            
            File logFile = new File(currentLogFile);
            boolean fileExists = logFile.exists();
            
            writer = new PrintWriter(new FileWriter(logFile, true));
            currentFileSize = fileExists ? logFile.length() : 0;
            
            // 清理旧日志文件
            cleanupOldLogFiles();
        } catch (IOException e) {
            System.err.println("[Logger] Failed to open log file: " + e.getMessage());
            writer = null;
        }
    }
    
    /**
     * 滚动日志文件
     */
    private static void rollLogFile() {
        try {
            if (writer != null) {
                writer.flush(); // 文件滚动前强制刷新，确保数据不丢失
                writer.close();
                writer = null;
            }
            
            // 重命名当前文件，添加时间戳
            if (currentLogFile != null) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File oldFile = new File(currentLogFile);
                String newName = currentLogFile.replace(".log", "_" + timestamp + ".log");
                File newFile = new File(newName);
                if (oldFile.exists()) {
                    oldFile.renameTo(newFile);
                }
            }
            
            // 打开新文件
            openLogFile();
        } catch (Exception e) {
            System.err.println("[Logger] Failed to roll log file: " + e.getMessage());
        }
    }
    
    /**
     * 清理旧日志文件
     */
    private static void cleanupOldLogFiles() {
        try {
            File dir = new File(logDir);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            
            File[] files = dir.listFiles((d, name) -> name.startsWith(logFileName) && name.endsWith(".log"));
            if (files == null || files.length <= maxFiles) {
                return;
            }
            
            // 按修改时间排序，删除最旧的文件
            java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            int filesToDelete = files.length - maxFiles;
            for (int i = 0; i < filesToDelete; i++) {
                files[i].delete();
            }
        } catch (Exception e) {
            // 忽略清理错误
        }
    }
    
    /**
     * 获取当前日志文件路径
     */
    public static String getCurrentLogFile() {
        lock.lock();
        try {
            return currentLogFile;
        } finally {
            lock.unlock();
        }
    }
}

