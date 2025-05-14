package com.onesaf.farm.model.datatype;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 表示InstantiatedDataType&lt;T&gt;，用于整型和浮点型属性
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InstantiatedDataType<T extends Number> extends DataType {
    private T defaultValue;
    private T minValue;
    private T maxValue;

    public InstantiatedDataType(int offset, T defaultValue, T minValue, T maxValue) {
        super(offset);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}