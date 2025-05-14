package com.onesaf.farm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代表FARM中的Feature类
 */
@Data
@NoArgsConstructor
public class Feature {
    private int category;                // 特征类别
    private String label;                // 特征标签
    private int code;                    // 特征代码
    private FeatureGeometry geometry;    // 几何类型
    private int usageBitmask;           // 使用位掩码
    private int precedence;             // 优先级
    private int attributesOverlaySize;  // 属性覆盖大小

    public Feature(int category, String label, int code, FeatureGeometry geometry,
                   int usageBitmask, int precedence, int attributesOverlaySize) {
        this.category = category;
        this.label = label;
        this.code = code;
        this.geometry = geometry;
        this.usageBitmask = usageBitmask;
        this.precedence = precedence;
        this.attributesOverlaySize = attributesOverlaySize;
    }

    public boolean isValid() {
        return code != -999 && geometry != FeatureGeometry.NULL;
    }
}