package com.onesaf.farm.model.datatype;

import com.onesaf.farm.model.Enumerant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 表示EnumerantDataType类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EnumerantDataType extends DataType {
    private Enumerant defaultEnum;
    private Set<Enumerant> validEnums = new HashSet<>();

    public EnumerantDataType(int offset, Enumerant defaultEnum, Set<Enumerant> validEnums) {
        super(offset);
        this.defaultEnum = defaultEnum;
        this.validEnums = validEnums;

        if (!validEnums.contains(defaultEnum)) {
            throw new IllegalArgumentException("Default enum must be in valid enums set");
        }
    }

    public int getDefault() {
        return defaultEnum.getEeCode();
    }
}