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

package com.dji.fh.kernel.spi.event;

/**
 * HMS 告警事件。
 *
 * @author DJI DevKit Team
 */
public final class HmsAlertEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final String alertCode;
    private final HmsAlertLevel level;
    private final String message;

    /**
     * @param source    触发来源
     * @param deviceSn  设备 SN
     * @param alertCode 告警码
     * @param level     告警级别
     * @param message   告警消息
     */
    public HmsAlertEvent(String source, String deviceSn, String alertCode,
                         HmsAlertLevel level, String message) {
        super(source);
        this.deviceSn = deviceSn;
        this.alertCode = alertCode;
        this.level = level;
        this.message = message;
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return 告警码
     */
    public String alertCode() {
        return alertCode;
    }

    /**
     * @return 告警级别
     */
    public HmsAlertLevel level() {
        return level;
    }

    /**
     * @return 告警消息
     */
    public String message() {
        return message;
    }
}
