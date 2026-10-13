// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.dji.flighthubsdk.util.Logger;

/**
 * JNI库加载器
 * 负责从JAR包中提取并加载平台特定的JNI库
 */
public class JniLibraryLoader {
    
    private static boolean libraryLoaded = false;
    private static final Object loadLock = new Object();
    
    // 依赖库加载顺序（按照依赖关系，从底层到上层）
    // macOS：JNI / paho 已改为 @loader_path 依赖打包的 OpenSSL 3（BIN-P0-02），必须先解压并加载
    private static final String[] DEPENDENCY_LIBS_MACOS = {
        "libcrypto.3.dylib",           // OpenSSL crypto（最底层）
        "libssl.3.dylib",              // OpenSSL SSL（依赖 crypto）
        "libwebsockets.12.dylib",      // WebSocket库（依赖 OpenSSL）
        "libpaho-mqtt3as.1.dylib",     // Paho MQTT库（依赖 OpenSSL）
        // C-01 (ES320 v3.6 第二轮): Volc RTC / Agora RTC 已从发布构建移除, FHSDK 收敛到 WebRTC + DRTC.
        // 相关 dylib 不再随包分发, 依赖列表同步剥离; 如需恢复请与 CMake / bin/build.sh 里的 gate 同步启用.
    };

    // Linux平台的依赖库文件名
    private static final String[] DEPENDENCY_LIBS_LINUX = {
        "libcrypto.so.1.1",            // OpenSSL加密库（最底层依赖）
        "libssl.so.1.1",               // OpenSSL SSL库（依赖crypto）
        "libwebsockets.so.12",         // WebSocket库（依赖ssl）
        "libpaho-mqtt3as.so.1",       // MQTT库（SSL 版本）
        // C-01: Volc / Agora native 已从本构建移除, 依赖列表同步剥离 (与 CMake OFF + build.sh gate 保持一致)
        // DRTC user SDK 动态库（按依赖关系顺序，从底层到上层） - DJI 自有, 保留
        "libdrpc.so",                 // DRTC RPC 底层库（被 libdrtc_device_sdk.so 依赖）
        "libdrtc_device_sdk.so",      // DRTC device SDK 主库（被 libflighthubsdk_jni.so 依赖）
    };
    
    // 主JNI库文件名
    private static final String MAIN_LIBRARY_NAME = "flighthubsdk_jni";
    
    /**
     * 根据平台获取对应的依赖库列表
     */
    private static String[] getDependencyLibsForPlatform(String platformName) {
        switch (platformName) {
            case "macosx":
                return DEPENDENCY_LIBS_MACOS;
            case "linux":
                return DEPENDENCY_LIBS_LINUX;
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platformName);
        }
    }
    
    /**
     * 检查库是否已加载
     * @return 是否已加载
     */
    public static boolean isLibraryLoaded() {
        synchronized (loadLock) {
            return libraryLoaded;
        }
    }
    
    /**
     * 加载JNI库
     */
    public static void loadLibrary() {
        synchronized (loadLock) {
            if (libraryLoaded) {
                return;
            }


            // 不需要从系统路径加载库
            // for (String libraryPath : LIBRARY_PATHS) {
            //     try {
            //         // 首先尝试从系统路径加载
            //         System.loadLibrary(libraryPath);
    
            //         libraryLoaded = true;
            //         System.out.println("[FlightHubSDK] Loaded library from system path: " + libraryPath);
            //     } catch (UnsatisfiedLinkError e) {
            //         // 系统路径加载失败，尝试从JAR包加载
            //         System.out.println("[FlightHubSDK] System library not found, trying to load from JAR...");
            //     }
            // }

            
            // 从JAR包中提取并加载库
            String platform = getPlatform();
            Logger.info("FlightHubSDK", "Detected platform: " + platform);
            
            // 使用完整的平台标识（如 macosx-arm64）
            String fullPlatform = platform;
            
            try {
                // 1. 首先按照依赖关系顺序加载所有依赖库
                Path depsTempDir = loadDependencyLibraries(fullPlatform);
                
                // 2. 然后加载主JNI库到同一目录
                String mainLibPath = extractMainLibraryFromJar(fullPlatform, depsTempDir);
                System.load(mainLibPath);
                Logger.info("FlightHubSDK", "Successfully loaded main JNI library: " + mainLibPath);
            } catch (IOException e) {
                Logger.error("FlightHubSDK", "Failed to load libraries: " + e.getMessage(), e);
                throw new RuntimeException("Failed to load Flight Hub SDK JNI libraries", e);
            }            
            libraryLoaded = true;
        }
    }
    
    /**
     * 获取当前平台标识
     * @return 平台标识
     */
    private static String getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        // 规范化架构名称
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            osArch = "x86_64";
        } else if (osArch.equals("aarch64") || osArch.equals("arm64")) {
            osArch = "arm64";
        }
        
        if (osName.contains("linux")) {
            return "linux-" + osArch;
        }
        if (osName.contains("mac") || osName.contains("darwin")) {
            return "macosx-" + osArch;
        }
        throw new UnsupportedOperationException("Unsupported platform: " + osName + "-" + osArch);
    }
    
    /**
     * 按照依赖关系顺序加载所有依赖库
     * @param fullPlatform 完整平台标识（如 macosx-arm64）
     * @return 依赖库的临时目录路径
     */
    private static Path loadDependencyLibraries(String fullPlatform) throws IOException {
        Logger.info("FlightHubSDK", "加载依赖库，平台: " + fullPlatform);
        
        // 从完整平台标识中提取平台名称
        String platformName = fullPlatform.split("-")[0];
        
        // 获取当前平台的依赖库列表
        String[] dependencyLibs = getDependencyLibsForPlatform(platformName);
        if (dependencyLibs.length == 0) {
            Logger.info("FlightHubSDK", "当前平台无需加载依赖库");
            // 创建空目录并返回
            Path tempDir = Files.createTempDirectory("flighthub_sdk_deps");
            tempDir.toFile().deleteOnExit();
            return tempDir;
        }
        
        Path tempDir = Files.createTempDirectory("flighthub_sdk_deps");
        tempDir.toFile().deleteOnExit();
        
        // 第一步：先提取所有库文件到临时目录（不加载）
        // 这样可以确保所有依赖都在同一个目录中，@loader_path 可以正确解析
        String[] extractedLibPaths = new String[dependencyLibs.length];
        for (int i = 0; i < dependencyLibs.length; i++) {
            String libName = dependencyLibs[i];
            String jarPath = "/lib/" + fullPlatform + "/" + libName;
            System.out.println("[FlightHubSDK] 提取依赖库: " + jarPath);
            
            String libPath = extractLibraryFromJar(jarPath, libName, tempDir);
            extractedLibPaths[i] = libPath;
            System.out.println("[FlightHubSDK] 已提取依赖库: " + libName + " -> " + libPath);
        }
        
        // 第二步：按照依赖关系顺序加载每个库
        // 此时所有库文件都在同一个临时目录中，@loader_path 可以正确解析依赖
        for (int i = 0; i < extractedLibPaths.length; i++) {
            String libName = dependencyLibs[i];
            String libPath = extractedLibPaths[i];
            System.out.println("[FlightHubSDK] 加载依赖库: " + libName);
            
            try {
                System.load(libPath);
                System.out.println("[FlightHubSDK] 成功加载依赖库: " + libName);
            } catch (UnsatisfiedLinkError e) {
                System.err.println("[FlightHubSDK] 加载依赖库失败: " + libName);
                System.err.println("[FlightHubSDK] 库路径: " + libPath);
                System.err.println("[FlightHubSDK] 错误信息: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to load dependency library: " + libName, e);
            }
        }
        
        // 返回依赖库目录，让主JNI库也加载到同一位置
        return tempDir;
    }
    
    /**
     * 从JAR包中提取主JNI库文件
     * @param fullPlatform 完整平台标识（如 macosx-arm64）
     * @param tempDir 临时目录
     * @return 提取的库文件路径
     */
    private static String extractMainLibraryFromJar(String fullPlatform, Path tempDir) throws IOException {
        // 从完整平台标识中提取平台名称
        String platformName = fullPlatform.split("-")[0];
        
        String extension;
        switch (platformName) {
            case "macosx":
                extension = ".dylib";
                break;
            case "linux":
                extension = ".so";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platformName);
        }
        
        String libraryFileName = "lib" + MAIN_LIBRARY_NAME + extension;
        String jarPath = "/lib/" + fullPlatform + "/" + libraryFileName;
        
        Logger.info("FlightHubSDK", "提取主JNI库: " + jarPath);
        return extractLibraryFromJar(jarPath, libraryFileName, tempDir);
    }
    
    /**
     * 从JAR包中提取指定的库文件
     * @param jarPath JAR包中的资源路径
     * @param fileName 文件名
     * @param tempDir 临时目录
     * @return 提取的库文件路径
     * @throws IOException IO异常
     */
    private static String extractLibraryFromJar(String jarPath, String fileName, Path tempDir) throws IOException {
        InputStream inputStream = JniLibraryLoader.class.getResourceAsStream(jarPath);
        if (inputStream == null) {
            throw new FileNotFoundException("JNI library not found in resources: " + jarPath);
        }
        
        // 使用传入的临时目录
        Path tempFile = tempDir.resolve(fileName);
        
        try {
            Logger.debug("FlightHubSDK", "Creating temporary file: " + tempFile.toAbsolutePath());
            
            // 复制库文件到临时目录
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // 验证文件大小
            long fileSize = Files.size(tempFile);
            Logger.debug("FlightHubSDK", "Temporary file size: " + fileSize + " bytes");
            
            if (fileSize == 0) {
                throw new IOException("Extracted library file size is 0, may be corrupted");
            }
            
            boolean executable = tempFile.toFile().setExecutable(true);
            Logger.debug("FlightHubSDK", "Set executable permission: " + executable);
            
            // 注册清理钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(tempFile);
                    Logger.debug("FlightHubSDK", "Cleaned up temporary file: " + tempFile);
                } catch (IOException e) {
                    Logger.error("FlightHubSDK", "Failed to cleanup temporary file: " + e.getMessage(), e);
                }
            }));
            
        } finally {
            inputStream.close();
        }
        
        return tempFile.toAbsolutePath().toString();
    }
}