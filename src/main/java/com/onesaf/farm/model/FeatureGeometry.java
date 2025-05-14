package com.onesaf.farm.model;

/**
 * 特征几何类型枚举，对应FARM中的FeatureGeometry
 */
public enum FeatureGeometry {
    NULL(0),
    POINT(1),
    LINEAR(2),
    AREAL(3);

    private final int value;

    FeatureGeometry(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FeatureGeometry fromValue(int value) {
        for (FeatureGeometry geometry : values()) {
            if (geometry.value == value) {
                return geometry;
            }
        }
        throw new IllegalArgumentException("Unknown FeatureGeometry value: " + value);
    }
}