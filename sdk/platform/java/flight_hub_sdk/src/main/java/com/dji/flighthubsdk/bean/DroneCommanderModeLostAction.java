// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 指点飞行失控动作（commander_mode_lost_action）。EA340 消息推送补全（Phase D）。 */
public final class DroneCommanderModeLostAction {

    private long commanderModeLostAction;

    public DroneCommanderModeLostAction() {
    }

    /**
     * 构造 DroneCommanderModeLostAction。
     *
     * @param commanderModeLostAction 指点飞行失控动作
     */
    public DroneCommanderModeLostAction(long commanderModeLostAction) {
        this.commanderModeLostAction = commanderModeLostAction;
    }

    /** 指点飞行失控动作 */
    public long getCommanderModeLostAction() {
        return commanderModeLostAction;
    }

    /** 指点飞行失控动作 */
    public void setCommanderModeLostAction(long commanderModeLostAction) {
        this.commanderModeLostAction = commanderModeLostAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneCommanderModeLostAction that = (DroneCommanderModeLostAction) o;
        return commanderModeLostAction == that.commanderModeLostAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commanderModeLostAction);
    }

    @Override
    public String toString() {
        return "DroneCommanderModeLostAction{commanderModeLostAction=" + commanderModeLostAction + '}';
    }
}
