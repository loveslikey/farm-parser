package com.onesaf.farm.model;

import lombok.Data;

@Data
public class Feature {
    private int category;         // 特征类别
    private int code;             // 特征代码
    private int geometryEnum;     // 特征几何枚举索引
    private int usageBitmask;     // 使用位掩码
    private int precedence;       // 特征优先级
    private int attributeOverlaySize; // 特征属性覆盖大小

    // 总大小：24字节
}