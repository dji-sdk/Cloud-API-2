/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.fh.kernel.mocksdk;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock SDK 的可调配置。
 *
 * <p>对应配置前缀 {@code fh.skeleton.mock.*}。ISV 可在 application.yml / application-mock.yml
 * 通过该前缀覆盖任意字段。
 *
 * <p>默认值面向 v0.1 开发态：1 虚拟 drone + 1 虚拟 dock + 1Hz OSD + 启动后 3s 设备上线脚本。
 *
 * @author DJI DevKit Team
 */
@ConfigurationProperties(prefix = "fh.skeleton.mock")
public class MockSdkProperties {

    /**
     * Mock SDK 总开关。默认 true。
     */
    private boolean enabled = true;

    /**
     * 虚拟 drone 配置。
     */
    private Device device = new Device("MOCK-DRONE-001", "Mock Drone");

    /**
     * 虚拟 dock 配置。
     */
    private Device dock = new Device("MOCK-DOCK-001", "Mock Dock");

    /**
     * OSD 推送配置。
     */
    private Osd osd = new Osd();

    /**
     * 启动脚本配置。
     */
    private Script script = new Script();

    /**
     * 设备配置（drone / dock 共用结构）。
     */
    public static class Device {

        /**
         * 设备 SN。
         */
        private String sn;

        /**
         * 设备型号。
         */
        private String model;

        public Device() {
        }

        public Device(String sn, String model) {
            this.sn = sn;
            this.model = model;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    /**
     * OSD 定时推送配置。
     */
    public static class Osd {

        /**
         * 推送间隔（毫秒）。默认 1000 = 1Hz。
         */
        private long intervalMillis = 1000L;

        public long getIntervalMillis() {
            return intervalMillis;
        }

        public void setIntervalMillis(long intervalMillis) {
            this.intervalMillis = intervalMillis;
        }
    }

    /**
     * 启动脚本配置。
     */
    public static class Script {

        /**
         * 启用启动脚本回放。默认 true。
         */
        private boolean enabled = true;

        /**
         * 事件列表，按 atMillis 升序回放。
         */
        private List<ScriptEvent> events = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<ScriptEvent> getEvents() {
            return events;
        }

        public void setEvents(List<ScriptEvent> events) {
            this.events = events;
        }
    }

    /**
     * 脚本单条事件描述。
     */
    public static class ScriptEvent {

        /**
         * 启动后多少毫秒触发。
         */
        private long atMillis;

        /**
         * 事件类型：device-online / device-offline / flight-task-progress / hms-alert / osd-once。
         */
        private String type;

        /**
         * 目标设备：drone / dock（按 type 语义解释）。
         */
        private String device;

        /**
         * 飞行任务状态（type=flight-task-progress 时使用）。
         */
        private String status;

        public long getAtMillis() {
            return atMillis;
        }

        public void setAtMillis(long atMillis) {
            this.atMillis = atMillis;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Device getDock() {
        return dock;
    }

    public void setDock(Device dock) {
        this.dock = dock;
    }

    public Osd getOsd() {
        return osd;
    }

    public void setOsd(Osd osd) {
        this.osd = osd;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
