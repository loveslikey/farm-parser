package com.onesaf.farm.model;

import lombok.Data;

/**
 * GdcSlice - 一个包含2D矩形地形区域的地理坐标切片
 */
@Data
public class GdcSlice {
    private double latitudeStart;  // 起始纬度(度)，有效值为-90(南90度)到90(北90度)
    private double latitudeStop;   // 终止纬度(度)，有效值为-90(南90度)到90(北90度)
    private double longitudeStart; // 起始经度(度)，有效值为-180(西180度)到180(东180度)
    private double longitudeStop;  // 终止经度(度)，有效值为-180(西180度)到180(东180度)

    // 总大小：32字节

    /**
     * 检查点是否在切片内
     * @param latitude 纬度
     * @param longitude 经度
     * @return 如果点在切片内则返回true
     */
    public boolean contains(double latitude, double longitude) {
        return latitude >= latitudeStart && latitude <= latitudeStop &&
                longitude >= longitudeStart && longitude <= longitudeStop;
    }

    /**
     * 计算切片的宽度(经度跨度)，以度为单位
     * @return 经度跨度
     */
    public double getLongitudeSpan() {
        return longitudeStop - longitudeStart;
    }

    /**
     * 计算切片的高度(纬度跨度)，以度为单位
     * @return 纬度跨度
     */
    public double getLatitudeSpan() {
        return latitudeStop - latitudeStart;
    }

    /**
     * 获取切片中心点纬度
     * @return 中心点纬度
     */
    public double getCenterLatitude() {
        return (latitudeStart + latitudeStop) / 2.0;
    }

    /**
     * 获取切片中心点经度
     * @return 中心点经度
     */
    public double getCenterLongitude() {
        return (longitudeStart + longitudeStop) / 2.0;
    }
}