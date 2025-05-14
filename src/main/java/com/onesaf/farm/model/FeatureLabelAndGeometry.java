package com.onesaf.farm.model;

import lombok.Data;

@Data
public class FeatureLabelAndGeometry {
    private String label;        // 特征标签
    private short geometryEnum;  // 几何类型枚举

    // 总大小：2字节 + 可变

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureLabelAndGeometry that = (FeatureLabelAndGeometry) o;
        return geometryEnum == that.geometryEnum &&
                (label != null ? label.equals(that.label) : that.label == null);
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (int) geometryEnum;
        return result;
    }
}