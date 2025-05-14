package com.onesaf.farm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnumerantDataType extends DataType {
    private Enumerant defaultEnum;
    private List<Enumerant> validEnums;

    // 总大小：8字节 + 可变
}