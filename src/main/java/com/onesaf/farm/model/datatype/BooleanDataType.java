package com.onesaf.farm.model.datatype;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 表示BooleanDataType类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BooleanDataType extends DataType {
    private boolean defaultValue;

    public BooleanDataType(int offset, boolean defaultValue) {
        super(offset);
        this.defaultValue = defaultValue;
    }
}