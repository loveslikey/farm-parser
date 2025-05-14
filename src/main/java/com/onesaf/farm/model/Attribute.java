package com.onesaf.farm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 代表FARM中的Attribute类
 */
@Data
@NoArgsConstructor
public class Attribute {
    private String label;              // 属性标签
    private int code;                  // 属性代码
    private AttributeDataType dataType; // 数据类型
    private AttributeUnits units;      // 单位
    private boolean editability;       // 是否可编辑

    public Attribute(String label, int code, AttributeDataType dataType, AttributeUnits units, boolean editability) {
        this.label = label;
        this.code = code;
        this.dataType = dataType;
        this.units = units;
        this.editability = editability;
    }

    public int getCategory() {
        return code; // 在C++代码中，category就是code
    }

    public boolean isValid() {
        return code != -999;
    }
}