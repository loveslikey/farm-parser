package com.onesaf.farm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示FARM中的Version结构
 */
@Data
@NoArgsConstructor
public class Version {
    private int versionNumber;  // 当前为8
    private int formatNumber;   // 当前为0
    private int updateNumber;   // 当前为0

    public Version(int versionNumber, int formatNumber, int updateNumber) {
        this.versionNumber = versionNumber;
        this.formatNumber = formatNumber;
        this.updateNumber = updateNumber;
    }

    @Override
    public String toString() {
        return versionNumber + "." + formatNumber + "." + updateNumber;
    }
}