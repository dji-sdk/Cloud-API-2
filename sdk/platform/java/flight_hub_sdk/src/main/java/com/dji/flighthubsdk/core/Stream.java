// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.IStream;
import com.dji.flighthubsdk.util.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流管理类，提供自定义 SEI 解析和拉流功能。
 * <p>通过 {@code FlightHubSDK.getStream()} 获取实例。</p>
 */
public class Stream {
    
    private IStream stream;
    
    // CustomSEI Module Container
    private CustomSEIModule customSEIModule = null;
    
    // PullStream Module Container - 使用Map存储多个streamId对应的实例
    private Map<String, PullStreamModule> pullStreamModules = new ConcurrentHashMap<>();

    /**
     * 构造流管理实例
     * @param stream 底层流接口
     */
    public Stream(IStream stream) {
        this.stream = stream;
    }

    /**
     * 获取自定义 SEI 编解码模块
     * @return CustomSEI 模块实例
     */
    public synchronized CustomSEIModule getCustomSEIModule() {
        if (customSEIModule == null) {
            try {
                com.dji.flighthubsdk.generated.ICustomSEIModule swigCustomSEIModule = stream.getCustomSEIModule();
                if (swigCustomSEIModule != null) {
                    customSEIModule = new CustomSEIModule(swigCustomSEIModule);
                    Logger.info("Stream", "GetCustomSEIModule: custom SEI module created");
                } else {
                    Logger.warn("Stream", "GetCustomSEIModule error: custom SEI module is null");
                }
            } catch (Exception e) {
                Logger.error("Stream", "GetCustomSEIModule error: " + e.getMessage(), e);
            }
        }
        return customSEIModule;
    }

    /**
     * 获取拉流模块（按 streamId 区分不同流）
     * @param streamId 流标识
     * @return PullStreamModule 实例
     */
    public synchronized PullStreamModule getPullStreamModule(String streamId) {
        // 检查是否已存在该streamId的模块
        if (pullStreamModules.containsKey(streamId)) {
            Logger.info("Stream", "GetPullStreamModule: 返回已存在的pull stream module, streamId: " + streamId);
            return pullStreamModules.get(streamId);
        }
        
        // 创建新的PullStreamModule
        try {
            com.dji.flighthubsdk.generated.IPullStreamModule swigPullStreamModule = stream.getPullStreamModule(streamId);
            if (swigPullStreamModule != null) {
                PullStreamModule pullStreamModule = new PullStreamModule(swigPullStreamModule);
                pullStreamModules.put(streamId, pullStreamModule);
                Logger.info("Stream", "GetPullStreamModule: pull stream module created, streamId: " + streamId + ", total modules: " + pullStreamModules.size());
                return pullStreamModule;
            } else {
                Logger.warn("Stream", "GetPullStreamModule error: pull stream module is null, streamId: " + streamId);
                return null;
            }
        } catch (Exception e) {
            Logger.error("Stream", "GetPullStreamModule error: " + e.getMessage() + ", streamId: " + streamId, e);
            return null;
        }
    }
}
