package com.onesaf.farm.model.datatype;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FARM中DataType类的基类
 */
@Data
@NoArgsConstructor
public class DataType {
    private int offset; // 属性在特征覆盖中的偏移量

    public DataType(int offset) {
        this.offset = offset;
    }
}