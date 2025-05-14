package com.onesaf.farm.model;

/**
 * 属性数据类型枚举，对应FARM中的AttributeDataType
 */
public enum AttributeDataType {
    NO_DATA_TYPE(0),
    INT32(1),
    FLOAT64(2),
    STRING(3),
    ENUMERATION(4),
    BOOLEAN(5),
    UUID(6),
    DELETED(7);

    private final int value;

    AttributeDataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AttributeDataType fromValue(int value) {
        for (AttributeDataType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AttributeDataType value: " + value);
    }
}