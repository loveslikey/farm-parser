package com.onesaf.farm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstantiatedDataType<T> extends DataType {
    private T defaultValue;
    private T minValue;
    private T maxValue;

    // 总大小(Int32): 16字节
    // 总大小(Float64): 28字节
}