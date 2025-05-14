package com.onesaf.farm.model;

import lombok.Data;

@Data
public class SpecifiedDataType {
    private short attributeDataType; // FARM AttributeDataType枚举索引
    private DataType dataType; // 具体数据类型

    // 总大小：2字节 + 可变
}