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

package com.dji.fh.feature.flightarea.model;

/**
 * 地形障碍物数据更新状态枚举。
 *
 * <p>对应 SDK {@code SafetyServiceModule} 的离线地图更新生命周期。
 *
 * @author DJI DevKit Team
 */
public enum OfflineMapUpdateStatus {

    /** 空闲，无更新任务 */
    IDLE,

    /** 等待服务端准备 */
    WAITING,

    /** 正在处理（下载/生成地形数据） */
    PROCESSING,

    /** 更新完成 */
    FINISH,

    /** 更新失败 */
    FAIL,

    /** 已取消 */
    CANCEL,

    /** 超时 */
    TIMEOUT
}
