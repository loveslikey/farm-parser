package com.onesaf.farm.model;

import lombok.Data;

import java.util.List;

@Data
public class FarmTable {
    private short featureCount;        // FARM中特征总数
    private short attributeCount;      // FARM中属性总数
    private List<Integer> attributeCodes; // 属性代码
    private List<SpecifiedDataType> dataTypes; // 数据类型信息

    // 总大小：8字节 + 可变
}