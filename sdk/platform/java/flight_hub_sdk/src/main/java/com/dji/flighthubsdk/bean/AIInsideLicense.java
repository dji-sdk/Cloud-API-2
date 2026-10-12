// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** AI Inside 算法授权信息 */
public class AIInsideLicense {
    /** 授权飞机 SN 列表，仅列表内设备可装载此算法 */
    private List<String> snList = new ArrayList<>();
    /** 授权开始时间（Unix 秒） */
    private long startedUtc;
    /** 授权到期时间（Unix 秒），超期后算法状态变为 expired */
    private long expiredUtc;

    public AIInsideLicense() {}

    /** 授权飞机 SN 列表，仅列表内设备可装载此算法 */
    public List<String> getSnList() { return snList; }

    /** @param v 授权飞机 SN 列表，仅列表内设备可装载此算法 */
    public void setSnList(List<String> v) { this.snList = v != null ? v : new ArrayList<>(); }

    /** 授权开始时间（Unix 秒） */
    public long getStartedUtc() { return startedUtc; }

    /** @param v 授权开始时间（Unix 秒） */
    public void setStartedUtc(long v) { this.startedUtc = v; }

    /** 授权到期时间（Unix 秒），超期后算法状态变为 expired */
    public long getExpiredUtc() { return expiredUtc; }

    /** @param v 授权到期时间（Unix 秒），超期后算法状态变为 expired */
    public void setExpiredUtc(long v) { this.expiredUtc = v; }
}
