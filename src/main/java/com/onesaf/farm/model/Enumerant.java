package com.onesaf.farm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代表FARM中的Enumerant类
 */
@Data
@NoArgsConstructor
public class Enumerant {
    private String eaLabel;    // 属性标签
    private String eeLabel;    // 枚举标签
    private int eaCode;        // 属性代码
    private int eeCode;        // 枚举代码

    public Enumerant(String eaLabel, String eeLabel, int eaCode, int eeCode) {
        this.eaLabel = eaLabel;
        this.eeLabel = eeLabel;
        this.eaCode = eaCode;
        this.eeCode = eeCode;
    }




}