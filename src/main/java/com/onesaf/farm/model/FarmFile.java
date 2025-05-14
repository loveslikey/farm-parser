package com.onesaf.farm.model;

import lombok.Data;

import java.util.Map;

@Data
public class FarmFile {
    private short endianness;        // 字节序，0表示大端，1表示小端
    private Version version;         // 版本信息
    private FarmTable farmTable;     // FARM表

    // 映射关系
    private Map<FeatureLabelAndGeometry, Integer> flagToFeatCategoryMap; // 特征标签和几何到类别映射
    private Map<Integer, Feature> featCategoryToFeatureMap;              // 特征类别到特征映射
    private Map<Integer, Attribute> attributeCodeToAttributeMap;         // 属性代码到属性映射

    // 总大小：14字节 + 可变
}