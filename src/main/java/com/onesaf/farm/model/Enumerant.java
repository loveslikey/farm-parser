package com.onesaf.farm.model;

import lombok.Data;

@Data
public class Enumerant {
    private int attributeCode; // 枚举所属的属性代码
    private int enumerantCode; // 枚举代码

    // 总大小：8字节
}