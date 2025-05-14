package com.onesaf.farm.util;

import com.onesaf.farm.model.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * FARM数据结构可视化工具
 */
@Log4j2
public class FarmVisualizer {

    /**
     * 生成FARM数据结构的DOT格式图
     * @param farmFile FARM文件对象
     * @param outputPath 输出DOT文件路径
     * @throws IOException 如果生成失败
     */
    public void generateDotGraph(FarmFile farmFile, String outputPath) throws IOException {
        log.info("开始生成FARM数据结构DOT图: {}", outputPath);

        StringBuilder dot = new StringBuilder();
        dot.append("digraph FarmStructure {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=box, style=filled, fillcolor=lightblue];\n");
        dot.append("  edge [color=navy];\n\n");

        // 添加FARM文件节点
        dot.append("  farm [label=\"FARM文件\\n");
        dot.append("字节序: ").append(farmFile.getEndianness() == 1 ? "小端" : "大端").append("\\n");
        dot.append("版本: ").append(farmFile.getVersion().getVersion()).append(".");
        dot.append(farmFile.getVersion().getFormat()).append(".");
        dot.append(farmFile.getVersion().getUpdate()).append("\", ");
        dot.append("shape=ellipse, fillcolor=lightgreen];\n\n");

        // 添加FARM表节点
        FarmTable farmTable = farmFile.getFarmTable();
        dot.append("  farmTable [label=\"FARM表\\n");
        dot.append("特征数量: ").append(farmTable.getFeatureCount()).append("\\n");
        dot.append("属性数量: ").append(farmTable.getAttributeCount()).append("\"];\n\n");

        // 添加映射节点
        dot.append("  flagToFeatMap [label=\"特征标签和几何到类别映射\\n");
        dot.append("条目数量: ").append(farmFile.getFlagToFeatCategoryMap().size()).append("\"];\n");

        dot.append("  featCatToFeatMap [label=\"特征类别到特征映射\\n");
        dot.append("条目数量: ").append(farmFile.getFeatCategoryToFeatureMap().size()).append("\"];\n");

        dot.append("  attrCodeToAttrMap [label=\"属性代码到属性映射\\n");
        dot.append("条目数量: ").append(farmFile.getAttributeCodeToAttributeMap().size()).append("\"];\n\n");

        // 添加特征和属性节点
        Map<Integer, String> featureNodes = new HashMap<>();
        int featureCount = 0;
        for (Map.Entry<Integer, Feature> entry : farmFile.getFeatCategoryToFeatureMap().entrySet()) {
            if (featureCount >= 5) break; // 限制数量，避免图过大

            Integer category = entry.getKey();
            Feature feature = entry.getValue();
            String nodeId = "feature" + category;

            dot.append("  ").append(nodeId).append(" [label=\"特征 #").append(category).append("\\n");
            dot.append("代码: ").append(feature.getCode()).append("\\n");
            dot.append("几何类型: ").append(getGeometryType(feature.getGeometryEnum())).append("\\n");
            dot.append("优先级: ").append(feature.getPrecedence()).append("\"];\n");

            featureNodes.put(category, nodeId);
            featureCount++;
        }

        Map<Integer, String> attributeNodes = new HashMap<>();
        int attributeCount = 0;
        for (Map.Entry<Integer, Attribute> entry : farmFile.getAttributeCodeToAttributeMap().entrySet()) {
            if (attributeCount >= 5) break; // 限制数量，避免图过大

            Integer code = entry.getKey();
            Attribute attribute = entry.getValue();
            String nodeId = "attribute" + code;

            dot.append("  ").append(nodeId).append(" [label=\"属性 #").append(code).append("\\n");
            dot.append("数据类型: ").append(getDataTypeName(attribute.getDataTypeEnum())).append("\\n");
            dot.append("单位: ").append(getUnitTypeName(attribute.getUnitsEnum())).append("\", ");
            dot.append("fillcolor=lightyellow];\n");

            attributeNodes.put(code, nodeId);
            attributeCount++;
        }
        dot.append("\n");

        // 添加关系
        dot.append("  farm -> farmTable;\n");
        dot.append("  farm -> flagToFeatMap;\n");
        dot.append("  farm -> featCatToFeatMap;\n");
        dot.append("  farm -> attrCodeToAttrMap;\n\n");

        // 特征关系
        for (Map.Entry<Integer, String> entry : featureNodes.entrySet()) {
            dot.append("  featCatToFeatMap -> ").append(entry.getValue()).append(";\n");
        }
        dot.append("\n");

        // 属性关系
        for (Map.Entry<Integer, String> entry : attributeNodes.entrySet()) {
            dot.append("  attrCodeToAttrMap -> ").append(entry.getValue()).append(";\n");
        }
        dot.append("\n");

        // 特征和属性的关联关系（示例）
        dot.append("  subgraph cluster_relationships {\n");
        dot.append("    label=\"特征-属性关系示例\";\n");
        dot.append("    style=dashed;\n");
        dot.append("    node [fillcolor=lightpink];\n");

        if (!featureNodes.isEmpty() && !attributeNodes.isEmpty()) {
            String featureNode = featureNodes.values().iterator().next();
            String attributeNode = attributeNodes.values().iterator().next();

            dot.append("    relationship [label=\"关系\\n特征具有属性\", shape=diamond];\n");
            dot.append("    ").append(featureNode).append(" -> relationship;\n");
            dot.append("    relationship -> ").append(attributeNode).append(";\n");
        }

        dot.append("  }\n\n");

        dot.append("}\n");

        // 写入文件
        FileUtils.writeStringToFile(new File(outputPath), dot.toString(), StandardCharsets.UTF_8);

        log.info("FARM数据结构DOT图已成功生成: {}", outputPath);
        System.out.println("DOT图已生成，可以使用Graphviz工具转换为图像，例如:");
        System.out.println("  dot -Tpng " + outputPath + " -o farm-structure.png");
    }

    /**
     * 获取几何类型名称
     */
    private String getGeometryType(int geometryEnum) {
        switch (geometryEnum) {
            case 0: return "无";
            case 1: return "点";
            case 2: return "线";
            case 3: return "面";
            default: return "未知(" + geometryEnum + ")";
        }
    }

    /**
     * 获取数据类型名称
     */
    private String getDataTypeName(int dataTypeEnum) {
        switch (dataTypeEnum) {
            case 0: return "无数据类型";
            case 1: return "Int32";
            case 2: return "Float64";
            case 3: return "字符串";
            case 4: return "枚举";
            case 5: return "布尔";
            case 6: return "UUID";
            default: return "未知(" + dataTypeEnum + ")";
        }
    }

    /**
     * 获取单位类型名称
     */
    private String getUnitTypeName(int unitsEnum) {
        switch (unitsEnum) {
            case 0: return "无单位";
            case 1: return "米";
            case 2: return "米/秒";
            case 3: return "平方米";
            case 4: return "度";
            case 5: return "千克";
            case 6: return "千克/立方米";
            case 7: return "摄氏度";
            case 8: return "升";
            case 9: return "勒克斯";
            case 10: return "帕斯卡";
            case 11: return "枚举";
            case 12: return "毫秒";
            default: return "未知(" + unitsEnum + ")";
        }
    }
}