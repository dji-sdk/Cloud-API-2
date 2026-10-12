// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 要素绑定的媒体资源（对齐 proto ElementMediaResources） */
public final class ElementMediaResources {

    private List<ElementMediaImage> images = Collections.emptyList();

    public ElementMediaResources() {
    }

    /** 图片列表；无绑定图片时为空列表 */
    public List<ElementMediaImage> getImages() { return images; }

    /** 图片列表；无绑定图片时为空列表 */
    public void setImages(List<ElementMediaImage> images) {
        this.images = images != null ? images : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(images, ((ElementMediaResources) o).images);
    }

    @Override
    public int hashCode() { return Objects.hash(images); }

    @Override
    public String toString() { return "ElementMediaResources{images.size=" + images.size() + '}'; }
}
