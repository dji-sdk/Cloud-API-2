// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

import com.dji.fhsdk.demo.live.FFmpegPipeStreamer;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 单个浏览器 HttpSession 绑定的设备运行时上下文。
 * 持有 Drone/Dock/LiveModule 实例及各类 SDK listenId，deselect 时统一清理。
 */
public class DeviceContext {

    private String httpSessionId;
    private String gatewaySn;
    private String droneSn;
    private String payloadIndex;
    private boolean payloadConfigured;
    private LiveInfo selectedLiveInfo;
    private List<LiveInfo> availablePayloads = new CopyOnWriteArrayList<>();
    private boolean drcConnected;
    private Drone drone;
    private Dock dock;
    private LiveModule liveModule;
    private FFmpegPipeStreamer streamer;
    private String whepPlayUrl;
    private String streamKey;
    private final Map<String, Integer> listenIds = new ConcurrentHashMap<>();
    private int liveStreamListenId = -1;
    private int payloadRangeListenId = -1;
    private int arSeiListenId = -1;
    private int fovSeiListenId = -1;

    public String getHttpSessionId() { return httpSessionId; }
    public void setHttpSessionId(String httpSessionId) { this.httpSessionId = httpSessionId; }
    public String getGatewaySn() { return gatewaySn; }
    public void setGatewaySn(String gatewaySn) { this.gatewaySn = gatewaySn; }
    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String droneSn) { this.droneSn = droneSn; }
    public String getPayloadIndex() { return payloadIndex; }
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }
    public boolean isPayloadConfigured() { return payloadConfigured; }
    public void setPayloadConfigured(boolean payloadConfigured) { this.payloadConfigured = payloadConfigured; }
    public LiveInfo getSelectedLiveInfo() { return selectedLiveInfo; }
    public void setSelectedLiveInfo(LiveInfo selectedLiveInfo) { this.selectedLiveInfo = selectedLiveInfo; }
    public List<LiveInfo> getAvailablePayloads() { return availablePayloads; }
    public void setAvailablePayloads(List<LiveInfo> availablePayloads) {
        this.availablePayloads = availablePayloads != null ? availablePayloads : new CopyOnWriteArrayList<>();
    }
    public boolean isDrcConnected() { return drcConnected; }
    public void setDrcConnected(boolean drcConnected) { this.drcConnected = drcConnected; }
    public Drone getDrone() { return drone; }
    public void setDrone(Drone drone) { this.drone = drone; }
    public Dock getDock() { return dock; }
    public void setDock(Dock dock) { this.dock = dock; }
    public LiveModule getLiveModule() { return liveModule; }
    public void setLiveModule(LiveModule liveModule) { this.liveModule = liveModule; }
    public FFmpegPipeStreamer getStreamer() { return streamer; }
    public void setStreamer(FFmpegPipeStreamer streamer) { this.streamer = streamer; }
    public String getWhepPlayUrl() { return whepPlayUrl; }
    public void setWhepPlayUrl(String whepPlayUrl) { this.whepPlayUrl = whepPlayUrl; }
    public String getStreamKey() { return streamKey; }
    public void setStreamKey(String streamKey) { this.streamKey = streamKey; }
    public Map<String, Integer> getListenIds() { return listenIds; }
    public int getLiveStreamListenId() { return liveStreamListenId; }
    public void setLiveStreamListenId(int liveStreamListenId) { this.liveStreamListenId = liveStreamListenId; }
    public int getPayloadRangeListenId() { return payloadRangeListenId; }
    public void setPayloadRangeListenId(int payloadRangeListenId) { this.payloadRangeListenId = payloadRangeListenId; }
    public int getArSeiListenId() { return arSeiListenId; }
    public void setArSeiListenId(int arSeiListenId) { this.arSeiListenId = arSeiListenId; }
    public int getFovSeiListenId() { return fovSeiListenId; }
    public void setFovSeiListenId(int fovSeiListenId) { this.fovSeiListenId = fovSeiListenId; }

    public void putListenId(String key, int id) {
        if (id >= 0) {
            listenIds.put(key, id);
        }
    }

    public void clear() {
        gatewaySn = null;
        droneSn = null;
        payloadIndex = null;
        payloadConfigured = false;
        selectedLiveInfo = null;
        availablePayloads = new CopyOnWriteArrayList<>();
        drcConnected = false;
        drone = null;
        dock = null;
        liveModule = null;
        streamer = null;
        whepPlayUrl = null;
        streamKey = null;
        listenIds.clear();
        liveStreamListenId = -1;
        payloadRangeListenId = -1;
        arSeiListenId = -1;
        fovSeiListenId = -1;
    }
}
