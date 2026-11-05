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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 飞行任务创建请求 DTO。
 *
 * <p>字段命名遵从前端 snake_case 约定，通过 Jackson {@code @JsonProperty} 映射。
 *
 * @author DJI DevKit Team
 */
public class CreateFlightTaskDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("task_type")
    private int taskType;

    @JsonProperty("wayline_uuid")
    private String waylineUuid;

    @JsonProperty("sn")
    private String sn;

    @JsonProperty("rth_altitude")
    private double rthAltitude = 100.0;

    @JsonProperty("out_of_control_action_in_flight")
    private int outOfControlActionInFlight = 1;

    @JsonProperty("time_zone")
    private String timeZone = "Asia/Shanghai";

    @JsonProperty("begin_at")
    private Long beginAt;

    @JsonProperty("end_at")
    private Long endAt;

    @JsonProperty("repeat_type")
    private Integer repeatType;

    @JsonProperty("plan_type")
    private Integer planType;

    @JsonProperty("wayline_precision_type")
    private Integer waylinePrecisionType;

    /**
     * 自动续飞策略：1=自动续飞（SDK 下发 {@code resumable_status="auto"}），
     * 其余值=手动续飞（下发 {@code "manual"}），null=不下发，沿用平台默认。
     *
     * <p><b>与 {@code is_break_point_resume} 的区别</b>：本字段是「出了断点后如何续飞」的
     * 策略；{@code is_break_point_resume} 是「本任务是否为断点续飞任务」的任务标识。
     * 两者语义不同，不可互相赋值。
     *
     * <p><b>已知局限</b>：仅创建路径生效。{@code EditTaskReq}（独立类，非
     * {@code CreateTaskReq} 子类）没有该字段的 setter，编辑任务无法改变续飞策略；
     * 任务详情 / 列表接口也只返回 {@code is_break_point_resume}，不回显本字段。
     * 编辑路径提交本字段时 BFF 会记录 WARN 并丢弃。
     */
    @JsonProperty("resumable_status")
    private Integer resumableStatus;

    @JsonProperty("rth_mode")
    private Integer rthMode;

    /**
     * 多模态识别开关。
     *
     * <p><b>已知局限（SDK 3.3.7）</b>：{@code CreateTaskReq} / {@code EditTaskReq} 均无
     * {@code vlm_enabled} 承载位，原生库请求字符串表中也没有任何 {@code vlm} 字段。
     * 本字段仅能被正确接收与校验，不能送达服务端；提交 {@code true} 时 BFF 会记录 WARN
     * 并丢弃，需 SDK 侧补齐字段后才能生效。
     */
    @JsonProperty("vlm_enabled")
    private Boolean vlmEnabled;

    @JsonProperty("interval")
    private Integer interval;

    @JsonProperty("min_battery_capacity")
    private Integer minBatteryCapacity;

    /**
     * 连续执行时段配置，形如 {@code [[startSeconds, endSeconds], ...]}。
     *
     * <p>HTTP 契约（创建航线任务 B6.2 接口）与前端
     * （{@code types/flightTask.ts} 的 {@code continuousPeriods: [number, number][]}）
     * 均为「二维数组」，与 SaaS 通道的 {@code continuous_task_periods?: Array<Array<number>>} 一致。
     * 本字段原为 {@code String}，一旦前端真的下发数组会直接反序列化失败，故对齐为嵌套列表。
     *
     * <p>注意：截至 SDK 3.3.5，{@code CreateTaskReq} 仍没有承载该字段的 setter，
     * 因此目前仅保证「能正确接收」，尚不能透传到服务端。
     */
    @JsonProperty("continuous_task_periods")
    private List<List<Integer>> continuousTaskPeriods;

    /**
     * 航线预估执行时长（秒）。
     *
     * <p>平台按 {@code int(wayline_duration) + preflight_prepare_duration} 计算任务时间窗，
     * 不传时平台退化为仅准备时长（100s）。
     *
     * <p><b>已知局限</b>：当前前端表单（{@code useFlightTaskForm.buildRequest}）尚未采集航线
     * 预估时长，恒不下发该字段，故冲突校验的候选时间窗恒退化为 100 秒下限——航线实际执行时长
     * 明显长于 100 秒时会漏判与后续任务的冲突。该窗口是「下限」而非「保守上限」，方向是漏判
     * 而非误判，接入方若需要准确校验，需补齐本字段的采集与下发。
     */
    @JsonProperty("wayline_duration")
    private Double waylineDuration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getWaylineUuid() {
        return waylineUuid;
    }

    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public double getRthAltitude() {
        return rthAltitude;
    }

    public void setRthAltitude(double rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    public int getOutOfControlActionInFlight() {
        return outOfControlActionInFlight;
    }

    public void setOutOfControlActionInFlight(int outOfControlActionInFlight) {
        this.outOfControlActionInFlight = outOfControlActionInFlight;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Long getBeginAt() {
        return beginAt;
    }

    public void setBeginAt(Long beginAt) {
        this.beginAt = beginAt;
    }

    public Long getEndAt() {
        return endAt;
    }

    public void setEndAt(Long endAt) {
        this.endAt = endAt;
    }

    public Integer getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(Integer repeatType) {
        this.repeatType = repeatType;
    }

    public Integer getPlanType() {
        return planType;
    }

    public void setPlanType(Integer planType) {
        this.planType = planType;
    }

    public Integer getWaylinePrecisionType() {
        return waylinePrecisionType;
    }

    public void setWaylinePrecisionType(Integer waylinePrecisionType) {
        this.waylinePrecisionType = waylinePrecisionType;
    }

    public Integer getResumableStatus() {
        return resumableStatus;
    }

    public void setResumableStatus(Integer resumableStatus) {
        this.resumableStatus = resumableStatus;
    }

    public Integer getRthMode() {
        return rthMode;
    }

    public void setRthMode(Integer rthMode) {
        this.rthMode = rthMode;
    }

    public Boolean getVlmEnabled() {
        return vlmEnabled;
    }

    public void setVlmEnabled(Boolean vlmEnabled) {
        this.vlmEnabled = vlmEnabled;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getMinBatteryCapacity() {
        return minBatteryCapacity;
    }

    public void setMinBatteryCapacity(Integer minBatteryCapacity) {
        this.minBatteryCapacity = minBatteryCapacity;
    }

    public List<List<Integer>> getContinuousTaskPeriods() {
        return continuousTaskPeriods;
    }

    public void setContinuousTaskPeriods(List<List<Integer>> continuousTaskPeriods) {
        this.continuousTaskPeriods = continuousTaskPeriods;
    }

    public Double getWaylineDuration() {
        return waylineDuration;
    }

    public void setWaylineDuration(Double waylineDuration) {
        this.waylineDuration = waylineDuration;
    }
}
