package com.onesaf.farm.model;

/**
 * 使用位掩码枚举，对应FARM中的UsageBitmask
 */
public class UsageBitmask {
    public static final int AVENUE = 0x0000001;
    public static final int APERTURE = 0x0000002;
    public static final int BUILDING = 0x0000004;
    public static final int AGRICULTURE_FARM = 0x0000008;
    public static final int FOREST = 0x0000010;
    public static final int FURNITURE = 0x0000020;
    public static final int RAISED_COMBAT_POS = 0x0000040;
    public static final int DUG_IN_COMBAT_POS = 0x0000080;
    public static final int LANE = 0x0000100;
    public static final int MULTI_BLDG = 0x0000200;
    public static final int LF_SML_VEH_OBSTACLE = 0x0000400;
    public static final int VEH_OBSTACLE = 0x0000800;
    public static final int AIR_VEH_OBSTACLE = 0x0001000;
    public static final int URBAN = 0x0002000;
    public static final int NBC = 0x0004000;
    public static final int BLOCKS_L_SML_VEH_LOS = 0x0008000;
    public static final int BLOCKS_VEH_LOS = 0x0010000;
    public static final int BLOCKS_LOS = 0x0020000;
    public static final int PROTECTS_L_SML_VEH = 0x0040000;
    public static final int PROTECTS_VEH = 0x0080000;
    public static final int BODY_OF_WATER = 0x0100000;

    // 将位掩码转换为可读字符串
    public static String toString(int bitmask) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        if ((bitmask & AVENUE) != 0) {
            sb.append("AVENUE");
            first = false;
        }
        if ((bitmask & APERTURE) != 0) {
            if (!first) sb.append(", ");
            sb.append("APERTURE");
            first = false;
        }
        // 添加其他位掩码检查...
        if ((bitmask & BUILDING) != 0) {
            if (!first) sb.append(", ");
            sb.append("BUILDING");
            first = false;
        }
        if ((bitmask & AGRICULTURE_FARM) != 0) {
            if (!first) sb.append(", ");
            sb.append("AGRICULTURE_FARM");
            first = false;
        }
        if ((bitmask & FOREST) != 0) {
            if (!first) sb.append(", ");
            sb.append("FOREST");
            first = false;
        }
        // 继续添加所有其他位掩码...

        return sb.toString();
    }
}