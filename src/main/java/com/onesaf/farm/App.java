package com.onesaf.farm;

import com.onesaf.farm.model.Attribute;
import com.onesaf.farm.model.FarmFile;
import com.onesaf.farm.model.Feature;
import com.onesaf.farm.model.FarmTable;
import com.onesaf.farm.parser.FarmParser;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

/**
 * FARM解析器主应用程序
 */
@Log4j2
public class App {
    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        log.info("FARM解析器 - 启动");
        args = new String[]{"parse", "E:\\project\\idea\\farm-parser\\src\\main\\resources\\farm.dat"};
        try {
            if (args.length > 0) {
                // 使用命令行处理器
                CommandLineProcessor processor = new CommandLineProcessor();
                processor.process(args);
            } else {
                // 交互模式
                runInteractiveMode();
            }
        } catch (Exception e) {
            log.error("程序执行时发生错误: ", e);
            System.err.println("错误: " + e.getMessage());
        }

        log.info("FARM解析器 - 结束");
    }

    /**
     * 运行交互模式
     */
    private static void runInteractiveMode() {
        try {
            System.out.println("FARM解析器 - 交互模式");
            System.out.print("请输入FARM文件路径: ");
            Scanner scanner = new Scanner(System.in);
            String farmFilePath = scanner.nextLine();

            FarmParser parser = new FarmParser();
            FarmFile farmFile = parser.parse(Paths.get(farmFilePath));

            // 打印FARM文件的基本信息
            printFarmFileInfo(farmFile);

            // 打印FARM表的信息
            printFarmTableInfo(farmFile.getFarmTable());

            // 打印特征信息
            printFeatureInfo(farmFile);

            // 打印属性信息
            printAttributeInfo(farmFile);

        } catch (IOException e) {
            log.error("解析FARM文件时发生错误: ", e);
            System.err.println("错误: " + e.getMessage());
        }
    }

    /**
     * 打印FARM文件基本信息
     */
    public static void printFarmFileInfo(FarmFile farmFile) {
        System.out.println("\n===== FARM文件信息 =====");
        System.out.println("字节序: " + (farmFile.getEndianness() == 1 ? "小端" : "大端"));
        System.out.println("版本: " + farmFile.getVersion().getVersion() + "." +
                farmFile.getVersion().getFormat() + "." +
                farmFile.getVersion().getUpdate());
        System.out.println("特征标签和几何到类别映射: " + farmFile.getFlagToFeatCategoryMap().size() + "个条目");
        System.out.println("特征类别到特征映射: " + farmFile.getFeatCategoryToFeatureMap().size() + "个条目");
        System.out.println("属性代码到属性映射: " + farmFile.getAttributeCodeToAttributeMap().size() + "个条目");
    }
    /**
     * 打印FARM表信息
     */
    public static void printFarmTableInfo(FarmTable farmTable) {
        System.out.println("\n===== FARM表信息 =====");
        System.out.println("特征数量: " + farmTable.getFeatureCount());
        System.out.println("属性数量: " + farmTable.getAttributeCount());
        System.out.println("数据类型数量: " + farmTable.getDataTypes().size());

        // 打印部分属性代码
        System.out.println("\n属性代码示例: ");
        int displayCount = Math.min(10, farmTable.getAttributeCodes().size());
        for (int i = 0; i < displayCount; i++) {
            System.out.println("  - 属性代码 #" + i + ": " + farmTable.getAttributeCodes().get(i));
        }
    }

    /**
     * 打印特征信息
     */
    public static void printFeatureInfo(FarmFile farmFile) {
        System.out.println("\n===== 特征信息 =====");

        Map<Integer, Feature> featMap = farmFile.getFeatCategoryToFeatureMap();

        // 打印部分特征信息
        System.out.println("特征示例: ");
        int count = 0;
        for (Map.Entry<Integer, Feature> entry : featMap.entrySet()) {
            if (count >= 5) break;

            Feature feature = entry.getValue();
            System.out.println("  - 特征类别 #" + entry.getKey());
            System.out.println("    代码: " + feature.getCode());
            System.out.println("    几何类型: " + getGeometryType(feature.getGeometryEnum()));
            System.out.println("    优先级: " + feature.getPrecedence());
            System.out.println("    属性覆盖大小: " + feature.getAttributeOverlaySize());
            System.out.println();

            count++;
        }
    }

    /**
     * 打印属性信息
     */
    public static void printAttributeInfo(FarmFile farmFile) {
        System.out.println("\n===== 属性信息 =====");

        Map<Integer, Attribute> attrMap = farmFile.getAttributeCodeToAttributeMap();

        // 打印部分属性信息
        System.out.println("属性示例: ");
        int count = 0;
        for (Map.Entry<Integer, Attribute> entry : attrMap.entrySet()) {
            if (count >= 5) break;

            Attribute attribute = entry.getValue();
            System.out.println("  - 属性代码 #" + entry.getKey());
            System.out.println("    数据类型: " + getDataTypeName(attribute.getDataTypeEnum()));
            System.out.println("    单位类型: " + getUnitTypeName(attribute.getUnitsEnum()));
            System.out.println("    可编辑性: " + (attribute.getEditability() == 1 ? "可编辑" : "不可编辑"));
            System.out.println();

            count++;
        }
    }

    /**
     * 获取几何类型名称
     */
    private static String getGeometryType(int geometryEnum) {
        switch (geometryEnum) {
            case 0:
                return "无";
            case 1:
                return "点";
            case 2:
                return "线";
            case 3:
                return "面";
            default:
                return "未知(" + geometryEnum + ")";
        }
    }

    /**
     * 获取数据类型名称
     */
    private static String getDataTypeName(int dataTypeEnum) {
        switch (dataTypeEnum) {
            case 0:
                return "无数据类型";
            case 1:
                return "Int32";
            case 2:
                return "Float64";
            case 3:
                return "字符串";
            case 4:
                return "枚举";
            case 5:
                return "布尔";
            case 6:
                return "UUID";
            default:
                return "未知(" + dataTypeEnum + ")";
        }
    }

    /**
     * 获取单位类型名称
     */
    private static String getUnitTypeName(int unitsEnum) {
        switch (unitsEnum) {
            case 0:
                return "无单位";
            case 1:
                return "米";
            case 2:
                return "米/秒";
            case 3:
                return "平方米";
            case 4:
                return "度";
            case 5:
                return "千克";
            case 6:
                return "千克/立方米";
            case 7:
                return "摄氏度";
            case 8:
                return "升";
            case 9:
                return "勒克斯";
            case 10:
                return "帕斯卡";
            case 11:
                return "枚举";
            case 12:
                return "毫秒";
            default:
                return "未知(" + unitsEnum + ")";
        }
    }
}