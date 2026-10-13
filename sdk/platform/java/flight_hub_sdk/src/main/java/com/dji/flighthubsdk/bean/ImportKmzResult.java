// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 上传 KMZ 并导入的结果 */
public class ImportKmzResult {
    /** 导入成功的航线列表 */
    private List<ImportKmzItem> list = new ArrayList<>();
    /** 导入成功的数量 */
    private int total;

    public ImportKmzResult() {}

    /** 导入成功的航线列表 */
    public List<ImportKmzItem> getList() { return list; }

    /** @param list 导入成功的航线列表 */
    public void setList(List<ImportKmzItem> list) { this.list = list != null ? list : new ArrayList<>(); }

    /** 导入成功的数量 */
    public int getTotal() { return total; }

    /** @param total 导入成功的数量 */
    public void setTotal(int total) { this.total = total; }

    @Override
    public String toString() {
        return "ImportKmzResult{list=" + list + ", total=" + total + '}';
    }
}
