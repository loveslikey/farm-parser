package com.onesaf.farm.model.datatype;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 表示UUIDDataType类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UUIDDataType extends DataType {
    public UUIDDataType(int offset) {
        super(offset);
    }
}