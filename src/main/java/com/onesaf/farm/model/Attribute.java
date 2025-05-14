package com.onesaf.farm.model;

import lombok.Data;

@Data
public class Attribute {
    private int code;         // 属性代码
    private int dataTypeEnum; // 属性数据类型枚举索引
    private int unitsEnum;    // 属性单位类型
    private int editability;  // 属性可编辑性布尔值

    // 总大小：16字节
}