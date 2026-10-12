// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场气象信息。 */
public final class DockMeteorologicalInfo {

    /** 舱内温度; */
    private double temperature;
    /** 环境温度; */
    private double environmentTemperature;
    /** 舱内湿度; */
    private int humidity;
    /** 风速; */
    private double windSpeed;
    /** 雨量; */
    private int rainfall;
    public DockMeteorologicalInfo() {
    }

/**
     * 构造 DockMeteorologicalInfo。
     *
     * @param temperature 舱内温度（摄氏度）
     * @param environmentTemperature 环境温度（摄氏度）
     * @param humidity 湿度
     * @param windSpeed 风速
     * @param rainfall 降雨量
     */
    public DockMeteorologicalInfo(
            double temperature,
            double environmentTemperature,
            int humidity,
            double windSpeed,
            int rainfall) {
        this.temperature = temperature;
        this.environmentTemperature = environmentTemperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.rainfall = rainfall;
    }

    /** 舱内温度; */
    public double getTemperature() {
        return temperature;
    }

    /** @param temperature 舱内温度; */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /** 环境温度; */
    public double getEnvironmentTemperature() {
        return environmentTemperature;
    }

    /** @param environmentTemperature 环境温度; */
    public void setEnvironmentTemperature(double environmentTemperature) {
        this.environmentTemperature = environmentTemperature;
    }

    /** 舱内湿度; */
    public int getHumidity() {
        return humidity;
    }

    /** @param humidity 舱内湿度; */
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    /** 风速; */
    public double getWindSpeed() {
        return windSpeed;
    }

    /** @param windSpeed 风速; */
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    /** 雨量; */
    public int getRainfall() {
        return rainfall;
    }

    /** @param rainfall 雨量; */
    public void setRainfall(int rainfall) {
        this.rainfall = rainfall;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockMeteorologicalInfo that = (DockMeteorologicalInfo) o;
        return Double.compare(that.temperature, temperature) == 0
                && Double.compare(that.environmentTemperature, environmentTemperature) == 0
                && humidity == that.humidity
                && Double.compare(that.windSpeed, windSpeed) == 0
                && rainfall == that.rainfall;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, environmentTemperature, humidity, windSpeed, rainfall);
    }

    @Override
    public String toString() {
        return "DockMeteorologicalInfo{temperature=" + temperature
                + ", environmentTemperature=" + environmentTemperature
                + ", humidity=" + humidity
                + ", windSpeed=" + windSpeed
                + ", rainfall=" + rainfall + '}';
    }
}
