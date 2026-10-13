// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fhsdk")
public class FhsdkProperties {

    private String address;
    private String port;
    private String notificationServer;
    private String logLevel = "info";
    private boolean logToConsole = true;
    private String logFilePath = "logs/flight_hub_sdk.log";
    private String drtcConfigPath;
    private String authHeaderKey = "user_id";
    private String authHeaderValue = "";

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    public String getNotificationServer() { return notificationServer; }
    public void setNotificationServer(String notificationServer) { this.notificationServer = notificationServer; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public boolean isLogToConsole() { return logToConsole; }
    public void setLogToConsole(boolean logToConsole) { this.logToConsole = logToConsole; }
    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }
    public String getDrtcConfigPath() { return drtcConfigPath; }
    public void setDrtcConfigPath(String drtcConfigPath) { this.drtcConfigPath = drtcConfigPath; }
    public String getAuthHeaderKey() { return authHeaderKey; }
    public void setAuthHeaderKey(String authHeaderKey) { this.authHeaderKey = authHeaderKey; }
    public String getAuthHeaderValue() { return authHeaderValue; }
    public void setAuthHeaderValue(String authHeaderValue) { this.authHeaderValue = authHeaderValue; }
}
