package com.onesaf.farm.parser;

import com.onesaf.farm.io.EnhancedBinaryReader;
import com.onesaf.farm.model.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FARM文件解析器
 */
@Log4j2
public class FarmParser {
    // AttributeDataType枚举值定义
    private static final int ATTRIBUTE_DATA_TYPE_NO_DATA = 0;
    private static final int ATTRIBUTE_DATA_TYPE_INT32 = 1;
    private static final int ATTRIBUTE_DATA_TYPE_FLOAT64 = 2;
    private static final int ATTRIBUTE_DATA_TYPE_STRING = 3;
    private static final int ATTRIBUTE_DATA_TYPE_ENUMERATION = 4;
    private static final int ATTRIBUTE_DATA_TYPE_BOOLEAN = 5;
    private static final int ATTRIBUTE_DATA_TYPE_UUID = 6;

    /**
     * 解析FARM文件
     * @param farmFilePath FARM文件路径
     * @return 解析后的FARM文件对象
     * @throws IOException 如果解析出错
     */
    public FarmFile parse(Path farmFilePath) throws IOException {
        log.info("开始解析FARM文件: {}", farmFilePath);

        try (EnhancedBinaryReader reader = new EnhancedBinaryReader(farmFilePath)) {
            FarmFile farmFile = new FarmFile();

            // 第一步：检测文件字节序
            ByteOrder detectedOrder = reader.detectAndSetByteOrder();

            // 直接读取字节序标识（已知格式）
            short endianness = (short)reader.readUInt16();
            farmFile.setEndianness(endianness);

            if (endianness == 1) {
                log.info("文件使用小端字节序 (标识值: 1)");
                reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            } else {
                log.info("文件使用大端字节序 (标识值: 0)");
                reader.setByteOrder(ByteOrder.BIG_ENDIAN);
            }

            // 解析版本
            Version version = parseVersion(reader);
            farmFile.setVersion(version);
            log.info("FARM文件版本: {}.{}.{}",
                    version.getVersion(), version.getFormat(),
                    version.getUpdate());

            // 解析FARM表
            FarmTable farmTable = parseFarmTable(reader);
            farmFile.setFarmTable(farmTable);
            log.info("FARM表: 特征数量={}, 属性数量={}",
                    farmTable.getFeatureCount(), farmTable.getAttributeCount());

            // 解析FeatureLabelAndGeometry到FeatureCategory映射
            int flagToFeatMapSize = reader.readUInt16();
            Map<FeatureLabelAndGeometry, Integer> flagToFeatMap =
                    parseFlagToFeatCategoryMap(reader, flagToFeatMapSize);
            farmFile.setFlagToFeatCategoryMap(flagToFeatMap);
            log.info("FeatureLabelAndGeometry到FeatureCategory映射: 条目数量={}", flagToFeatMap.size());

            // 解析FeatureCategory到Feature映射
            int featCatToFeatMapSize = reader.readUInt16();
            Map<Integer, Feature> featCatToFeatMap =
                    parseFeatureCategoryToFeatureMap(reader, featCatToFeatMapSize);
            farmFile.setFeatCategoryToFeatureMap(featCatToFeatMap);
            log.info("FeatureCategory到Feature映射: 条目数量={}", featCatToFeatMap.size());

            // 解析AttributeCode到Attribute映射
            int attrCodeToAttrMapSize = reader.readUInt16();
            Map<Integer, Attribute> attrCodeToAttrMap =
                    parseAttributeCodeToAttributeMap(reader, attrCodeToAttrMapSize);
            farmFile.setAttributeCodeToAttributeMap(attrCodeToAttrMap);
            log.info("AttributeCode到Attribute映射: 条目数量={}", attrCodeToAttrMap.size());

            log.info("FARM文件解析完成");

            return farmFile;
        }
    }

    /**
     * 解析版本信息
     */
    private Version parseVersion(EnhancedBinaryReader reader) throws IOException {
        Version version = new Version();
        version.setVersion((short)reader.readUInt16());
        version.setFormat((short)reader.readUInt16());
        version.setUpdate((short)reader.readUInt16());
        return version;
    }

    /**
     * 解析FARM表
     */
    private FarmTable parseFarmTable(EnhancedBinaryReader reader) throws IOException {
        FarmTable farmTable = new FarmTable();

        // 解析特征和属性数量
        farmTable.setFeatureCount((short)reader.readUInt16());
        farmTable.setAttributeCount((short)reader.readUInt16());

        // 解析属性代码
        List<Integer> attributeCodes = new ArrayList<>(farmTable.getAttributeCount());
        for (int i = 0; i < farmTable.getAttributeCount(); i++) {
            attributeCodes.add(reader.readInt32());
        }
        farmTable.setAttributeCodes(attributeCodes);

        // 解析数据类型
        List<SpecifiedDataType> dataTypes = new ArrayList<>();
        for (int i = 0; i < farmTable.getFeatureCount() * farmTable.getAttributeCount(); i++) {
            SpecifiedDataType dataType = parseSpecifiedDataType(reader);
            dataTypes.add(dataType);
        }
        farmTable.setDataTypes(dataTypes);

        return farmTable;
    }

    /**
     * 解析指定数据类型
     */
    private SpecifiedDataType parseSpecifiedDataType(EnhancedBinaryReader reader) throws IOException {
        SpecifiedDataType specifiedDataType = new SpecifiedDataType();

        // 解析属性数据类型
        int attributeDataType = reader.readUInt16();
        specifiedDataType.setAttributeDataType((short)attributeDataType);

        // 根据数据类型解析具体的数据类型
        switch (attributeDataType) {
            case ATTRIBUTE_DATA_TYPE_NO_DATA:
                // 无数据类型，忽略
                break;
            case ATTRIBUTE_DATA_TYPE_INT32:
                InstantiatedDataType<Integer> intDataType = new InstantiatedDataType<>();
                intDataType.setAttributeOffset(reader.readInt32());
                intDataType.setDefaultValue(reader.readInt32());
                intDataType.setMinValue(reader.readInt32());
                intDataType.setMaxValue(reader.readInt32());
                specifiedDataType.setDataType(intDataType);
                break;
            case ATTRIBUTE_DATA_TYPE_FLOAT64:
                InstantiatedDataType<Double> floatDataType = new InstantiatedDataType<>();
                floatDataType.setAttributeOffset(reader.readInt32());
                floatDataType.setDefaultValue(reader.readFloat64());
                floatDataType.setMinValue(reader.readFloat64());
                floatDataType.setMaxValue(reader.readFloat64());
                specifiedDataType.setDataType(floatDataType);
                break;
            case ATTRIBUTE_DATA_TYPE_STRING:
                StringDataType stringDataType = new StringDataType();
                stringDataType.setAttributeOffset(reader.readInt32());
                specifiedDataType.setDataType(stringDataType);
                break;
            case ATTRIBUTE_DATA_TYPE_ENUMERATION:
                EnumerantDataType enumDataType = new EnumerantDataType();
                enumDataType.setAttributeOffset(reader.readInt32());

                // 解析默认枚举值
                Enumerant defaultEnum = new Enumerant();
                defaultEnum.setAttributeCode(reader.readInt32());
                defaultEnum.setEnumerantCode(reader.readInt32());
                enumDataType.setDefaultEnum(defaultEnum);

                // 解析有效枚举值列表
                int enumCount = reader.readInt32();
                List<Enumerant> validEnums = new ArrayList<>(enumCount);
                for (int i = 0; i < enumCount; i++) {
                    Enumerant enumerant = new Enumerant();
                    enumerant.setAttributeCode(reader.readInt32());
                    enumerant.setEnumerantCode(reader.readInt32());
                    validEnums.add(enumerant);
                }
                enumDataType.setValidEnums(validEnums);

                specifiedDataType.setDataType(enumDataType);
                break;
            case ATTRIBUTE_DATA_TYPE_BOOLEAN:
                BooleanDataType booleanDataType = new BooleanDataType();
                booleanDataType.setAttributeOffset(reader.readInt32());
                booleanDataType.setDefaultValue(reader.readInt32());
                specifiedDataType.setDataType(booleanDataType);
                break;
            case ATTRIBUTE_DATA_TYPE_UUID:
                UUIDDataType uuidDataType = new UUIDDataType();
                uuidDataType.setAttributeOffset(reader.readInt32());
                specifiedDataType.setDataType(uuidDataType);
                break;
            default:
                log.warn("未知的属性数据类型: {}", attributeDataType);
                // 跳过未知数据
                reader.skip(4); // 假设至少有一个属性偏移量
                break;
        }

        return specifiedDataType;
    }

    /**
     * 解析FeatureLabelAndGeometry到FeatureCategory映射
     */
    private Map<FeatureLabelAndGeometry, Integer> parseFlagToFeatCategoryMap(
            EnhancedBinaryReader reader, int mapSize) throws IOException {
        Map<FeatureLabelAndGeometry, Integer> map = new HashMap<>();

        for (int i = 0; i < mapSize; i++) {
            // 解析标签长度
            int labelLength = reader.readUInt16();

            // 解析标签字符串
            String label = reader.readString(labelLength);

            // 如果字符串长度是奇数，需要添加一个字节的填充
            if (labelLength % 2 != 0) {
                reader.skip(1);
            }

            // 解析几何类型
            int geometryEnum = reader.readUInt16();

            // 创建FeatureLabelAndGeometry对象
            FeatureLabelAndGeometry key = new FeatureLabelAndGeometry();
            key.setLabel(label);
            key.setGeometryEnum((short)geometryEnum);

            // 添加到映射
            map.put(key, i);
        }

        return map;
    }

    /**
     * 解析FeatureCategory到Feature映射
     */
    private Map<Integer, Feature> parseFeatureCategoryToFeatureMap(
            EnhancedBinaryReader reader, int mapSize) throws IOException {
        Map<Integer, Feature> map = new HashMap<>();

        for (int i = 0; i < mapSize; i++) {
            // 解析特征类别
            int featureCategory = reader.readUInt16();

            // 添加2字节的填充，以保证Feature对象从4字节边界开始
            reader.skip(2);

            // 解析Feature对象
            Feature feature = parseFeature(reader);

            // 添加到映射
            map.put(featureCategory, feature);
        }

        return map;
    }

    /**
     * 解析Feature对象
     */
    private Feature parseFeature(EnhancedBinaryReader reader) throws IOException {
        Feature feature = new Feature();
        feature.setCategory(reader.readInt32());
        feature.setCode(reader.readInt32());
        feature.setGeometryEnum(reader.readInt32());
        feature.setUsageBitmask(reader.readInt32());
        feature.setPrecedence(reader.readInt32());
        feature.setAttributeOverlaySize(reader.readInt32());
        return feature;
    }

    /**
     * 解析AttributeCode到Attribute映射
     */
    private Map<Integer, Attribute> parseAttributeCodeToAttributeMap(
            EnhancedBinaryReader reader, int mapSize) throws IOException {
        Map<Integer, Attribute> map = new HashMap<>();

        for (int i = 0; i < mapSize; i++) {
            // 解析属性代码
            int attributeCode = reader.readInt32();

            // 解析Attribute对象
            Attribute attribute = parseAttribute(reader);

            // 添加到映射
            map.put(attributeCode, attribute);
        }

        return map;
    }

    /**
     * 解析Attribute对象
     */
    private Attribute parseAttribute(EnhancedBinaryReader reader) throws IOException {
        Attribute attribute = new Attribute();
        attribute.setCode(reader.readInt32());
        attribute.setDataTypeEnum(reader.readInt32());
        attribute.setUnitsEnum(reader.readInt32());
        attribute.setEditability(reader.readInt32());
        return attribute;
    }
}