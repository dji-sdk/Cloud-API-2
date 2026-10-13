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

package com.dji.fh.feature.dashboard.dto;

/**
 * 机场统计 DTO。
 *
 * @param total     总数
 * @param idle      空闲
 * @param working   作业中
 * @param debugging 调试中
 * @param offline   离线
 * @author DJI DevKit Team
 */
public record DockStatsDto(int total, int idle, int working, int debugging, int offline) {}
