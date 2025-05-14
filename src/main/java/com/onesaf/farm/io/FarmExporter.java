package com.onesaf.farm.io;

import com.onesaf.farm.model.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * FARM数据导出工具
 */
@Log4j2
public class FarmExporter {

    /**
     * 将FARM数据导出为JSON格式
     * @param farmFile FARM文件对象
     * @param outputPath 输出文件路径
     * @throws IOException 如果导出失败
     */
    public void exportToJson(FarmFile farmFile, String outputPath) throws IOException {
        log.info("开始导出FARM数据到JSON文件: {}", outputPath);

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // 导出基本信息
        json.append("  \"endianness\": \"").append(farmFile.getEndianness() == 1 ? "LITTLE_ENDIAN" : "BIG_ENDIAN").append("\",\n");

        // 导出版本信息
        Version version = farmFile.getVersion();
        json.append("  \"version\": {\n");
        json.append("    \"version\": ").append(version.getVersion()).append(",\n");
        json.append("    \"format\": ").append(version.getFormat()).append(",\n");
        json.append("    \"update\": ").append(version.getUpdate()).append("\n");
        json.append("  },\n");

        // 导出FARM表信息
        FarmTable farmTable = farmFile.getFarmTable();
        json.append("  \"farmTable\": {\n");
        json.append("    \"featureCount\": ").append(farmTable.getFeatureCount()).append(",\n");
        json.append("    \"attributeCount\": ").append(farmTable.getAttributeCount()).append(",\n");

        // 导出属性代码
        json.append("    \"attributeCodes\": [\n");
        for (int i = 0; i < farmTable.getAttributeCodes().size(); i++) {
            json.append("      ").append(farmTable.getAttributeCodes().get(i));
            if (i < farmTable.getAttributeCodes().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("    ]\n");
        json.append("  },\n");

        // 导出特征信息
        json.append("  \"features\": [\n");
        Map<Integer, Feature> featureMap = farmFile.getFeatCategoryToFeatureMap();
        int featureCount = 0;
        for (Map.Entry<Integer, Feature> entry : featureMap.entrySet()) {
            Integer category = entry.getKey();
            Feature feature = entry.getValue();

            json.append("    {\n");
            json.append("      \"category\": ").append(category).append(",\n");
            json.append("      \"code\": ").append(feature.getCode()).append(",\n");
            json.append("      \"geometryEnum\": ").append(feature.getGeometryEnum()).append(",\n");
            json.append("      \"geometryType\": \"").append(getGeometryType(feature.getGeometryEnum())).append("\",\n");
            json.append("      \"usageBitmask\": ").append(feature.getUsageBitmask()).append(",\n");
            json.append("      \"precedence\": ").append(feature.getPrecedence()).append(",\n");
            json.append("      \"attributeOverlaySize\": ").append(feature.getAttributeOverlaySize()).append("\n");
            json.append("    }");

            if (featureCount < featureMap.size() - 1) {
                json.append(",");
            }
            json.append("\n");
            featureCount++;
        }
        json.append("  ],\n");

        // 导出属性信息
        json.append("  \"attributes\": [\n");
        Map<Integer, Attribute> attributeMap = farmFile.getAttributeCodeToAttributeMap();
        int attributeCount = 0;
        for (Map.Entry<Integer, Attribute> entry : attributeMap.entrySet()) {
            Integer code = entry.getKey();
            Attribute attribute = entry.getValue();

            json.append("    {\n");
            json.append("      \"code\": ").append(code).append(",\n");
            json.append("      \"dataTypeEnum\": ").append(attribute.getDataTypeEnum()).append(",\n");
            json.append("      \"dataType\": \"").append(getDataTypeName(attribute.getDataTypeEnum())).append("\",\n");
            json.append("      \"unitsEnum\": ").append(attribute.getUnitsEnum()).append(",\n");
            json.append("      \"units\": \"").append(getUnitTypeName(attribute.getUnitsEnum())).append("\",\n");
            json.append("      \"editability\": ").append(attribute.getEditability()).append("\n");
            json.append("    }");

            if (attributeCount < attributeMap.size() - 1) {
                json.append(",");
            }
            json.append("\n");
            attributeCount++;
        }
        json.append("  ],\n");

        // 导出FeatureLabelAndGeometry到FeatureCategory映射
        json.append("  \"featureLabelAndGeometryMap\": [\n");
        Map<FeatureLabelAndGeometry, Integer> flagMap = farmFile.getFlagToFeatCategoryMap();
        int flagCount = 0;
        for (Map.Entry<FeatureLabelAndGeometry, Integer> entry : flagMap.entrySet()) {
            FeatureLabelAndGeometry flag = entry.getKey();
            Integer categoryId = entry.getValue();

            json.append("    {\n");
            json.append("      \"label\": \"").append(flag.getLabel()).append("\",\n");
            json.append("      \"geometryEnum\": ").append(flag.getGeometryEnum()).append(",\n");
            json.append("      \"geometryType\": \"").append(getGeometryType(flag.getGeometryEnum())).append("\",\n");
            json.append("      \"categoryId\": ").append(categoryId).append("\n");
            json.append("    }");

            if (flagCount < flagMap.size() - 1) {
                json.append(",");
            }
            json.append("\n");
            flagCount++;
        }
        json.append("  ]\n");

        json.append("}");

        // 写入文件
        FileUtils.writeStringToFile(new File(outputPath), json.toString(), StandardCharsets.UTF_8);

        log.info("FARM数据已成功导出到: {}", outputPath);
    }

    /**
     * 获取几何类型名称
     */
    private String getGeometryType(int geometryEnum) {
        switch (geometryEnum) {
            case 0: return "NULL";
            case 1: return "POINT";
            case 2: return "LINEAR";
            case 3: return "AREAL";
            default: return "UNKNOWN(" + geometryEnum + ")";
        }
    }

    /**
     * 获取数据类型名称
     */
    private String getDataTypeName(int dataTypeEnum) {
        switch (dataTypeEnum) {
            case 0: return "NO_DATA_TYPE";
            case 1: return "INT32";
            case 2: return "FLOAT64";
            case 3: return "STRING";
            case 4: return "ENUMERATION";
            case 5: return "BOOLEAN";
            case 6: return "UUID";
            default: return "UNKNOWN(" + dataTypeEnum + ")";
        }
    }

    /**
     * 获取单位类型名称
     */
    private String getUnitTypeName(int unitsEnum) {
        switch (unitsEnum) {
            case 0: return "UNITLESS";
            case 1: return "METERS";
            case 2: return "METERS_PER_SECOND";
            case 3: return "SQUARE_METERS";
            case 4: return "DEGREES";
            case 5: return "KILOGRAMS";
            case 6: return "KILOGRAMS_PER_CUBIC_METER";
            case 7: return "CELSIUS";
            case 8: return "LITERS";
            case 9: return "LUX";
            case 10: return "PASCALS";
            case 11: return "ENUMERATION";
            case 12: return "MILLISECONDS";
            default: return "UNKNOWN(" + unitsEnum + ")";
        }
    }
}