// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备影子同步单字段状态 */
public final class FlightSafeSyncField {
    /** 字段名，如 "rth_altitude", "out_of_control_action" */
    private String field;
    /** 该字段是否已同步到设备 */
    private boolean synced;
    public FlightSafeSyncField() {
    }

/**
     * 构造 FlightSafeSyncField。
     *
     * @param field 字段名，如 "rth_altitude", "out_of_control_action"
     * @param synced 该字段是否已同步到设备
     */
    public FlightSafeSyncField(String field, boolean synced) {
        this.field = field != null ? field : "";
        this.synced = synced;
    }

    /** 字段名，如 "rth_altitude", "out_of_control_action" */
    public String getField() { return field; }
    /** 该字段是否已同步到设备 */
    public boolean isSynced() { return synced; }

    /** 字段名，如 "rth_altitude", "out_of_control_action" */
    public void setField(String field) {
        this.field = field;
    }

    /** 该字段是否已同步到设备 */
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightSafeSyncField that = (FlightSafeSyncField) o;
        return Objects.equals(field, that.field)
                && synced == that.synced;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, synced);
    }

    @Override
    public String toString() {
        return "FlightSafeSyncField{field='" + field + '\'' + ", synced=" + synced + '}';
    }
}
