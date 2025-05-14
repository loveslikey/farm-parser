package com.onesaf.farm;

import com.onesaf.farm.model.*;
import com.onesaf.farm.model.datatype.*;
import com.onesaf.farm.util.BinaryReader;
import com.onesaf.farm.util.DirectoryUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.*;

/**
 * FARM数据解析类，对应C++中的FeatureAttributeMapping类
 */
@Slf4j
public class FeatureAttributeMapping {
    private static final String FARM_FILE_LABEL = "farm.dat";
    private static final Version EXPECTED_VERSION = new Version(8, 0, 0);

    // 表示是否已初始化
    private boolean initialized = false;

    // 特征标签和几何类型到特征类别的映射
    @Getter
    private final Map<FeatureLabelAndGeometry, Integer> featureLabelsAndGeometriesToCategories = new HashMap<>();

    // 特征类别到特征的映射
    @Getter
    private final List<Feature> featureCategoriesToFeatures = new ArrayList<>();

    // 属性代码到属性的映射
    @Getter
    private final List<Attribute> attributeCodesToAttributes = new ArrayList<>();

    // 缓存的属性标签到属性的映射
    private final Map<String, Attribute> attributeLabelsToAttributes = new HashMap<>();

    // FARM表 - 二维数组，特征到属性的映射
    @Getter
    private final ArrayList<List<DataType>> farm = new ArrayList<>();

    /**
     * 从给定目录读取FARM数据
     *
     * @param databaseDirectory 数据库目录
     * @param configDirectory   配置目录
     * @param failureReason     失败原因（输出参数）
     * @return 是否成功
     */
    public boolean read(String databaseDirectory, String configDirectory, StringBuilder failureReason) {
        if (isInitialized()) {
            return true;
        }

        try {
            String farmFilePath = databaseDirectory + "/otf/" + FARM_FILE_LABEL;

            if (!DirectoryUtil.isFileReadable(farmFilePath)) {
                failureReason.append("无法打开文件: ").append(farmFilePath);
                log.error(failureReason.toString());
                return false;
            }

            try (FileInputStream fileStream = new FileInputStream(farmFilePath);
                 BinaryReader reader = new BinaryReader(fileStream, ByteOrder.LITTLE_ENDIAN)) {

                // 读取并检查字节序
                int littleEndian = reader.readUInt16();
                if (littleEndian != (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)) {
                    failureReason.append("FARM文件的字节序不正确");
                    log.error(failureReason.toString());
                    return false;
                }

                // 读取并检查版本
                Version fileVersion = readVersion(reader);
                if (!fileVersion.equals(EXPECTED_VERSION)) {
                    log.warn("FARM文件版本 {} 与软件版本 {} 不同", fileVersion, EXPECTED_VERSION);
                }

                // 读取FARM表
                readFarmTable(reader);

                // 读取特征标签和几何类型到特征类别的映射
                readFeatureLabelsAndGeometriesToCategories(reader);

                // 读取特征类别到特征的映射
                readFeatureCategoriesToFeatures(reader);

                // 读取属性代码到属性的映射
                readAttributeCodesToAttributes(reader);

                initialized = true;
                return true;
            }
        } catch (IOException e) {
            failureReason.append("读取FARM文件时出错: ").append(e.getMessage());
            log.error("读取FARM文件时出错", e);
            return false;
        }
    }

    /**
     * 读取版本信息
     */
    private Version readVersion(BinaryReader reader) throws IOException {
        int versionNumber = reader.readUInt16();
        int formatNumber = reader.readUInt16();
        int updateNumber = reader.readUInt16();
        return new Version(versionNumber, formatNumber, updateNumber);
    }

    /**
     * 读取FARM表
     */
    private void readFarmTable(BinaryReader reader) throws IOException {
        // 读取FARM表的维度
        int numRows = reader.readUInt16();
        int numColumns = reader.readUInt16();

        // 读取属性代码
        List<Integer> codes = new ArrayList<>();
        for (int column = 0; column < numColumns; column++) {
            int code = reader.readUInt16();
            codes.add(code);
        }

        // 重置FARM表
        farm.clear();
        farm.ensureCapacity(numRows);

        // 读取FARM表的条目
        for (int row = 0; row < numRows; row++) {
            List<DataType> rowData = new ArrayList<>(Collections.nCopies(Collections.max(codes) + 1, null));
            farm.add(rowData);

            for (int column = 0; column < numColumns; column++) {
                int code = codes.get(column);
                int dataType = reader.readUInt16();

                switch (dataType) {
                    case 0: // no_data_type
                        // 特征不包含该属性
                        rowData.set(code, null);
                        break;
                    case 1: // int32
                        InstantiatedDataType<Integer> int32Type = new InstantiatedDataType<>();
                        int32Type.setOffset(reader.readInt32());
                        int32Type.setDefaultValue(reader.readInt32());
                        int32Type.setMinValue(reader.readInt32());
                        int32Type.setMaxValue(reader.readInt32());
                        rowData.set(code, int32Type);
                        break;
                    case 2: // float64
                        InstantiatedDataType<Double> float64Type = new InstantiatedDataType<>();
                        float64Type.setOffset(reader.readInt32());
                        float64Type.setDefaultValue(reader.readFloat64());
                        float64Type.setMinValue(reader.readFloat64());
                        float64Type.setMaxValue(reader.readFloat64());
                        rowData.set(code, float64Type);
                        break;
                    case 3: // string
                        StringDataType stringType = new StringDataType();
                        stringType.setOffset(reader.readInt32());
                        rowData.set(code, stringType);
                        break;
                    case 4: // enumeration
                        EnumerantDataType enumType = new EnumerantDataType();
                        enumType.setOffset(reader.readInt32());

                        // 读取默认枚举值
                        Enumerant defaultEnum = new Enumerant();
                        defaultEnum.setEaCode(reader.readInt32());
                        defaultEnum.setEeCode(reader.readInt32());
                        enumType.setDefaultEnum(defaultEnum);

                        // 读取有效的枚举值列表
                        int numValidEnums = reader.readInt32();
                        Set<Enumerant> validEnums = new HashSet<>();
                        for (int i = 0; i < numValidEnums; i++) {
                            Enumerant enum1 = new Enumerant();
                            enum1.setEaCode(reader.readInt32());
                            enum1.setEeCode(reader.readInt32());
                            validEnums.add(enum1);
                        }
                        enumType.setValidEnums(validEnums);

                        rowData.set(code, enumType);
                        break;
                    case 5: // boolean
                        BooleanDataType boolType = new BooleanDataType();
                        boolType.setOffset(reader.readInt32());
                        boolType.setDefaultValue(reader.readInt32() != 0);
                        rowData.set(code, boolType);
                        break;
                    case 6: // uuid
                        UUIDDataType uuidType = new UUIDDataType();
                        uuidType.setOffset(reader.readInt32());
                        rowData.set(code, uuidType);
                        break;
                    default:
                        throw new IOException("在FARM表中发现不支持的数据类型: " + dataType);
                }
            }
        }
    }

    /**
     * 读取特征标签和几何类型到特征类别的映射
     */
    private void readFeatureLabelsAndGeometriesToCategories(BinaryReader reader) throws IOException {
        int mapSize = reader.readUInt16();
        featureLabelsAndGeometriesToCategories.clear();

        for (int i = 0; i < mapSize; i++) {
            String label = reader.readString();
            FeatureGeometry geometry = FeatureGeometry.fromValue(reader.readUInt16());
            int category = reader.readUInt16();

            FeatureLabelAndGeometry key = new FeatureLabelAndGeometry(label, geometry);
            featureLabelsAndGeometriesToCategories.put(key, category);
        }
    }

    /**
     * 读取特征类别到特征的映射
     */
    private void readFeatureCategoriesToFeatures(BinaryReader reader) throws IOException {
        featureCategoriesToFeatures.clear();

        int mapSize = reader.readUInt16();

        for (int i = 0; i < mapSize; i++) {
            int category = reader.readUInt16();
            Feature feature = readFeature(reader);

            // 确保列表大小足够
            while (featureCategoriesToFeatures.size() <= category) {
                featureCategoriesToFeatures.add(null);
            }

            featureCategoriesToFeatures.set(category, feature);
        }
    }

    /**
     * 读取属性代码到属性的映射
     */
    private void readAttributeCodesToAttributes(BinaryReader reader) throws IOException {
        attributeCodesToAttributes.clear();

        int mapSize = reader.readUInt16();

        for (int i = 0; i < mapSize; i++) {
            int code = reader.readInt32();
            Attribute attribute = readAttribute(reader);

            // 确保列表大小足够
            while (attributeCodesToAttributes.size() <= code) {
                attributeCodesToAttributes.add(null);
            }

            attributeCodesToAttributes.set(code, attribute);
        }
    }

    /**
     * 读取特征对象
     */
    private Feature readFeature(BinaryReader reader) throws IOException {
        Feature feature = new Feature();

        feature.setCategory(reader.readInt32());
        feature.setLabel(reader.readString());
        feature.setCode(reader.readInt32());
        feature.setGeometry(FeatureGeometry.fromValue(reader.readInt32()));
        feature.setUsageBitmask(reader.readInt32());
        feature.setPrecedence(reader.readInt32());
        feature.setAttributesOverlaySize(reader.readInt32());

        return feature;
    }

    /**
     * 读取属性对象
     */
    private Attribute readAttribute(BinaryReader reader) throws IOException {
        Attribute attribute = new Attribute();

        attribute.setLabel(reader.readString());
        attribute.setCode(reader.readInt32());
        attribute.setDataType(AttributeDataType.fromValue(reader.readInt32()));
        attribute.setUnits(AttributeUnits.fromValue(reader.readInt32()));
        attribute.setEditability(reader.readInt32() != 0);

        return attribute;
    }

    /**
     * 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取特征几何类型
     */
    public boolean getFeatureGeometry(int featureCategory, FeatureGeometry[] geometry) {
        if (featureCategory < 0 || featureCategory >= featureCategoriesToFeatures.size()) {
            return false;
        }

        Feature feature = featureCategoriesToFeatures.get(featureCategory);
        if (feature == null) {
            return false;
        }

        geometry[0] = feature.getGeometry();
        return true;
    }

    /**
     * 获取属性
     */
    public boolean getAttribute(int attributeCategory, Attribute[] attribute) {
        if (attributeCategory < 0 || attributeCategory >= attributeCodesToAttributes.size()) {
            return false;
        }

        Attribute attr = attributeCodesToAttributes.get(attributeCategory);
        if (attr == null) {
            return false;
        }

        attribute[0] = attr;
        return true;
    }

    /**
     * 获取特征
     */
    public boolean getFeature(String featureLabel, FeatureGeometry featureGeometry, Feature[] feature) {
        FeatureLabelAndGeometry key = new FeatureLabelAndGeometry(featureLabel, featureGeometry);
        Integer category = featureLabelsAndGeometriesToCategories.get(key);

        if (category == null) {
            return false;
        }

        if (category < 0 || category >= featureCategoriesToFeatures.size()) {
            return false;
        }

        Feature feat = featureCategoriesToFeatures.get(category);
        if (feat == null) {
            return false;
        }

        feature[0] = feat;
        return true;
    }

    /**
     * 释放资源
     */
    public void destroy() {
        if (initialized) {
            // 清除所有数据
            featureLabelsAndGeometriesToCategories.clear();
            featureCategoriesToFeatures.clear();
            attributeCodesToAttributes.clear();
            attributeLabelsToAttributes.clear();

            for (List<DataType> row : farm) {
                row.clear();
            }
            farm.clear();

            initialized = false;
        }
    }
}