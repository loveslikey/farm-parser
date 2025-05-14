package com.onesaf.farm.model;

/**
 * 属性单位枚举，对应FARM中的AttributeUnits
 */
public enum AttributeUnits {
    UNITLESS(0),
    METERS(1),
    METERS_PER_SECOND(2),
    SQUARE_METERS(3),
    DEGREES(4),
    KILOGRAMS(5),
    KILOGRAMS_PER_CUBIC_METER(6),
    CELSIUS(7),
    LITERS(8),
    LUX(9),
    PASCALS(10),
    ENUMERATION_UNITS(11),
    MILLISECONDS(12);

    private final int value;

    AttributeUnits(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AttributeUnits fromValue(int value) {
        for (AttributeUnits unit : values()) {
            if (unit.value == value) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown AttributeUnits value: " + value);
    }
}