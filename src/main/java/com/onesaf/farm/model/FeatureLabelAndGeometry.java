package com.onesaf.farm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 特征标签和几何类型的组合
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureLabelAndGeometry {
    private String label;
    private FeatureGeometry geometry;
}