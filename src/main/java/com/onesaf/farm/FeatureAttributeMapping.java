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
    private final List<List<DataType>> farm = new ArrayList<>();

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
            log.info("开始读取FARM文件: {}", farmFilePath);

            if (!DirectoryUtil.isFileReadable(farmFilePath)) {
                failureReason.append("无法打开文件: ").append(farmFilePath);
                log.error(failureReason.toString());
                return false;
            }

            try (FileInputStream fileStream = new FileInputStream(farmFilePath);
                 BinaryReader reader = new BinaryReader(fileStream, ByteOrder.LITTLE_ENDIAN)) {

                // 读取并检查字节序
                int littleEndian = reader.readUInt16();
                log.debug("文件字节序标记: {} (0=大端, 1=小端)", littleEndian);

                ByteOrder actualByteOrder;
                if (littleEndian == 1) {
                    actualByteOrder = ByteOrder.LITTLE_ENDIAN;
                } else if (littleEndian == 0) {
                    actualByteOrder = ByteOrder.BIG_ENDIAN;
                } else {
                    failureReason.append("FARM文件的字节序标记无效: ").append(littleEndian);
                    log.error(failureReason.toString());
                    return false;
                }

                // 如果需要，创建一个新的reader使用正确的字节序
                BinaryReader properReader;
                if (reader.getByteOrder() != actualByteOrder) {
                    log.debug("切换到正确的字节序: {}", actualByteOrder);
                    fileStream.getChannel().position(2); // 重置到字节序之后
                    properReader = new BinaryReader(fileStream, actualByteOrder);
                } else {
                    properReader = reader;
                }

                // 读取并检查版本
                Version fileVersion = readVersion(properReader);
                log.debug("FARM文件版本: {}", fileVersion);

                if (!fileVersion.equals(EXPECTED_VERSION)) {
                    log.warn("FARM文件版本 {} 与预期版本 {} 不同", fileVersion, EXPECTED_VERSION);
                }

                // 读取FARM表
                readFarmTable(properReader);

                // 读取特征标签和几何类型到特征类别的映射
                readFeatureLabelsAndGeometriesToCategories(properReader);

                // 读取特征类别到特征的映射
                readFeatureCategoriesToFeatures(properReader);

                // 读取属性代码到属性的映射
              //  readAttributeCodesToAttributes(properReader);

                log.info("FARM文件读取成功");
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
        try {
            log.debug("开始读取FARM表");

            // 读取FARM表的维度
            int numRows = reader.readUInt16();
            int numColumns = reader.readUInt16();

            log.debug("FARM表维度: {}行 x {}列", numRows, numColumns);

            // 读取属性代码
            List<Integer> codes = new ArrayList<>();
            for (int column = 0; column < numColumns; column++) {
                int code = reader.readUInt16();
                codes.add(code);
            }

            log.debug("读取到属性代码: {}", codes);

            // 重置FARM表
            farm.clear();

            // 如果numRows或numColumns为0，直接返回
            if (numRows == 0 || numColumns == 0) {
                log.warn("FARM表为空");
                return;
            }

            // 找出最大的属性代码，为每行分配足够的空间
            int maxCode = codes.stream().max(Integer::compareTo).orElse(0);
            log.debug("最大属性代码: {}", maxCode);

            // 读取FARM表的条目
            for (int row = 0; row < numRows; row++) {
                List<DataType> rowData = new ArrayList<>(Collections.nCopies(maxCode + 1, null));
                farm.add(rowData);

                for (int column = 0; column < numColumns; column++) {
                    int code = codes.get(column);
                    int dataType = reader.readUInt16();

                    log.debug("读取FARM表条目 [行{}][列{}]: 代码={}, 数据类型={}",
                            row, column, code, dataType);

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
                            validEnums.add(defaultEnum); // 确保默认值在集合中

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

            log.debug("FARM表读取完成，共{}行", farm.size());

        } catch (Exception e) {
            log.error("读取FARM表时出错", e);
            throw new IOException("读取FARM表时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 读取特征标签和几何类型到特征类别的映射
     */
    private void readFeatureLabelsAndGeometriesToCategories(BinaryReader reader) throws IOException {
        try {
            log.debug("开始读取特征标签和几何类型到特征类别的映射");
            featureLabelsAndGeometriesToCategories.clear();

            // 读取映射大小
            int mapSize = reader.readUInt16();
            log.debug("特征标签和几何类型映射大小: {}", mapSize);

            for (int i = 0; i < mapSize; i++) {
                // 读取特征标签
                String label = reader.readString();

                // 读取几何类型
                int geometryValue = reader.readUInt16();

                // 验证几何类型值合法性
                if (geometryValue < 0 || geometryValue > 3) {
                    log.warn("特征[{}]几何类型值异常: {}, 使用NULL替代", label, geometryValue);
                    geometryValue = 0; // 使用NULL作为默认值
                }

                FeatureGeometry geometry = FeatureGeometry.fromValue(geometryValue);

                // 读取特征类别
                int category = reader.readUInt16();

                log.debug("读取特征映射: {}({}) -> 类别{}", label, geometry, category);

                FeatureLabelAndGeometry key = new FeatureLabelAndGeometry(label, geometry);
                featureLabelsAndGeometriesToCategories.put(key, category);
            }

            log.debug("特征标签和几何类型到特征类别的映射读取完成，共{}个映射", featureLabelsAndGeometriesToCategories.size());
        } catch (Exception e) {
            log.error("读取特征标签和几何类型到特征类别的映射时出错", e);
            throw new IOException("读取特征标签和几何类型到特征类别的映射时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 读取特征类别到特征的映射
     */
    private void readFeatureCategoriesToFeatures(BinaryReader reader) throws IOException {
        featureCategoriesToFeatures.clear();

        // 首先读取map的条目数
        int mapSize = reader.readUInt16();
        log.debug("读取特征类别到特征的映射，大小: {}", mapSize);

        // 创建一个临时的Map来存储读取的数据
        Map<Integer, Feature> tempMap = new HashMap<>();

        for (int i = 0; i < mapSize; i++) {
            try {
                // 读取category key
                int category = reader.readUInt16();
                log.debug("读取特征类别: {}", category);

                // 接下来读取Feature值
                Feature feature = readFeature(reader);
                log.debug("读取特征: {} - {}", feature.getLabel(), feature.getGeometry());

                tempMap.put(category, feature);
            } catch (Exception e) {
                log.error("读取特征映射的第{}个条目时出错", i, e);
                throw e;
            }
        }

        // 确定最大的category值
        int maxCategory = tempMap.keySet().stream().max(Integer::compareTo).orElse(0);

        // 初始化featureCategoriesToFeatures列表
        for (int i = 0; i <= maxCategory; i++) {
            featureCategoriesToFeatures.add(null);
        }

        // 填充列表
        for (Map.Entry<Integer, Feature> entry : tempMap.entrySet()) {
            featureCategoriesToFeatures.set(entry.getKey(), entry.getValue());
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

        try {
            // 我们需要严格按照C++代码中Feature类的读取顺序
            // C++代码中Feature::read方法的顺序是：
            // category, code, geometry, usage_bitmask, precedence, attributes_overlay_size
            feature.setCategory(reader.readInt32());
            feature.setCode(reader.readInt32());

            // 读取几何类型 - 这是关键部分
            int geometryValue = reader.readInt32();
            log.debug("读取到几何类型原始值: {}", geometryValue);

            // 确保几何类型值在合理范围内
            if (geometryValue < 0 || geometryValue > 3) {
                throw new IOException("无效的几何类型值: " + geometryValue +
                        "。期望范围: 0-3 (null, point, linear, areal)");
            }

            feature.setGeometry(FeatureGeometry.fromValue(geometryValue));

            feature.setUsageBitmask(reader.readInt32());
            feature.setPrecedence(reader.readInt32());
            feature.setAttributesOverlaySize(reader.readInt32());

            // 在Feature类中的label成员是单独从FeatureCode通过映射查询得到的
            // 我们需要从属性表中查找或在后续步骤中设置
            // 暂时设为空
            feature.setLabel("");

            return feature;
        } catch (Exception e) {
            log.error("读取特征时出错: 已读取的内容: {}", feature, e);
            throw e;
        }
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