package com.onesaf.farm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BooleanDataType extends DataType {
    private int defaultValue; // 0为false，1为true

    // 总大小：8字节
}