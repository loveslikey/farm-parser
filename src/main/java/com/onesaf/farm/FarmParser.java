package com.onesaf.farm;

import com.onesaf.farm.model.Feature;
import com.onesaf.farm.model.FeatureGeometry;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FARM解析工具主类
 */
@Slf4j
public class FarmParser {
    private static final FeatureAttributeMapping farmMapping = new FeatureAttributeMapping();

    public static void main(String[] args) {
        args=new String[]{"read","E:\\project\\onesaf-v9.0\\support\\terrain\\build65\\v1\\taiwan_demo","E:\\project\\onesaf-v9.0\\support\\terrain\\build65\\v1\\config"};

        if (args.length < 1) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        try {
            switch (command) {
                case "read":
                    if (args.length < 2) {
                        System.err.println("错误: 缺少数据库目录参数");
                        printUsage();
                        return;
                    }
                    readFarm(args[1], args.length > 2 ? args[2] : null);
                    break;

                case "list-features":
                    if (args.length < 2) {
                        System.err.println("错误: 缺少数据库目录参数");
                        printUsage();
                        return;
                    }
                    listFeatures(args[1], args.length > 2 ? args[2] : null);
                    break;

                case "feature-info":
                    if (args.length < 4) {
                        System.err.println("错误: 缺少参数。需要数据库目录、特征标签和几何类型");
                        printUsage();
                        return;
                    }
                    getFeatureInfo(args[1], args.length > 2 ? args[2] : null, args[3], parseGeometryType(args[3]));
                    break;

                default:
                    System.err.println("错误: 未知命令 '" + command + "'");
                    printUsage();
            }
        } catch (Exception e) {
            log.error("执行命令时出错: {}", command, e);
            System.err.println("执行命令时出错: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("OneSAF FARM 解析工具");
        System.out.println("用法:");
        System.out.println("  java -jar farm-parser.jar read <database-dir> [config-dir]");
        System.out.println("    读取FARM数据并验证");
        System.out.println();
        System.out.println("  java -jar farm-parser.jar list-features <database-dir> [config-dir]");
        System.out.println("    列出所有特征");
        System.out.println();
        System.out.println("  java -jar farm-parser.jar feature-info <database-dir> [config-dir] <feature-label> <geometry-type>");
        System.out.println("    获取特定特征的详细信息");
        System.out.println("    <geometry-type> 可以是: point, linear, areal");
    }

    private static FeatureGeometry parseGeometryType(String geometryStr) {
        String geom = geometryStr.toLowerCase();

        switch (geom) {
            case "point":
                return FeatureGeometry.POINT;
            case "linear":
                return FeatureGeometry.LINEAR;
            case "areal":
                return FeatureGeometry.AREAL;
            default:
                throw new IllegalArgumentException("无效的几何类型: " + geometryStr + ". 有效类型: point, linear, areal");
        }
    }

    private static void readFarm(String databaseDir, String configDir) {
        if (configDir == null) {
            configDir = databaseDir + File.separator + "config";
        }

        StringBuilder failureReason = new StringBuilder();
        boolean success = farmMapping.read(databaseDir, configDir, failureReason);

        if (success) {
            System.out.println("FARM数据读取成功");
            printFarmStatistics();
        } else {
            System.err.println("FARM数据读取失败: " + failureReason);
            System.out.println("请检查farm-parser.log文件获取更多调试信息");
        }
    }

    private static void printFarmStatistics() {
        System.out.println("FARM统计:");
        System.out.println("  特征数量: " + farmMapping.getFeatureCategoriesToFeatures().stream().filter(f -> f != null).count());
        System.out.println("  属性数量: " + farmMapping.getAttributeCodesToAttributes().stream().filter(a -> a != null).count());
        System.out.println("  FARM表行数: " + farmMapping.getFarm().size());
    }

    private static void listFeatures(String databaseDir, String configDir) {
        if (configDir == null) {
            configDir = databaseDir + File.separator + "config";
        }

        StringBuilder failureReason = new StringBuilder();
        boolean success = farmMapping.read(databaseDir, configDir, failureReason);

        if (!success) {
            System.err.println("FARM数据读取失败: " + failureReason);
            return;
        }

        List<Feature> features = farmMapping.getFeatureCategoriesToFeatures().stream()
                .filter(f -> f != null)
                .collect(Collectors.toList());

        System.out.println("特征列表 (" + features.size() + "):");
        System.out.printf("%-5s %-30s %-10s %-6s%n", "类别", "标签", "几何", "代码");
        System.out.println("------------------------------------------------");

        features.forEach(f -> {
            System.out.printf("%-5d %-30s %-10s %-6d%n",
                    f.getCategory(),
                    f.getLabel(),
                    f.getGeometry(),
                    f.getCode());
        });
    }

    private static void getFeatureInfo(String databaseDir, String configDir, String featureLabel, FeatureGeometry geometry) {
        if (configDir == null) {
            configDir = databaseDir + File.separator + "config";
        }

        StringBuilder failureReason = new StringBuilder();
        boolean success = farmMapping.read(databaseDir, configDir, failureReason);

        if (!success) {
            System.err.println("FARM数据读取失败: " + failureReason);
            return;
        }

        Feature[] feature = new Feature[1];
        if (!farmMapping.getFeature(featureLabel, geometry, feature)) {
            System.err.println("找不到特征: " + featureLabel + " (" + geometry + ")");
            return;
        }

        System.out.println("特征信息:");
        System.out.println("  类别: " + feature[0].getCategory());
        System.out.println("  标签: " + feature[0].getLabel());
        System.out.println("  代码: " + feature[0].getCode());
        System.out.println("  几何: " + feature[0].getGeometry());
        System.out.println("  使用位掩码: " + feature[0].getUsageBitmask() + " (" +
                com.onesaf.farm.model.UsageBitmask.toString(feature[0].getUsageBitmask()) + ")");
        System.out.println("  优先级: " + feature[0].getPrecedence());
        System.out.println("  属性覆盖大小: " + feature[0].getAttributesOverlaySize());
    }
}