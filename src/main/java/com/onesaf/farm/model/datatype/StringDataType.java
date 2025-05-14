package com.onesaf.farm.model.datatype;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 表示StringDataType类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StringDataType extends DataType {
    public StringDataType(int offset) {
        super(offset);
    }
}