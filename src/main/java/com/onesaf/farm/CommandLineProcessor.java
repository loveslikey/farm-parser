package com.onesaf.farm;

import com.onesaf.farm.io.FarmExporter;
import com.onesaf.farm.model.FarmFile;
import com.onesaf.farm.parser.FarmParser;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 命令行参数处理类
 */
@Log4j2
public class CommandLineProcessor {

    private static final String COMMAND_PARSE = "parse";
    private static final String COMMAND_EXPORT = "export";
    private static final String COMMAND_HELP = "help";

    private static final String OPTION_OUTPUT = "-o";
    private static final String OPTION_VERBOSE = "-v";

    /**
     * 处理命令行参数
     * @param args 命令行参数
     */
    public void process(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();
        switch (command) {
            case COMMAND_PARSE:
                processParse(Arrays.copyOfRange(args, 1, args.length));
                break;
            case COMMAND_EXPORT:
                processExport(Arrays.copyOfRange(args, 1, args.length));
                break;
            case COMMAND_HELP:
                printUsage();
                break;
            default:
                System.err.println("错误: 未知命令 '" + command + "'");
                printUsage();
                break;
        }
    }

    /**
     * 处理parse命令
     * @param args 命令参数
     */
    private void processParse(String[] args) {
        if (args.length < 1) {
            System.err.println("错误: 解析命令需要指定FARM文件路径");
            printUsage();
            return;
        }

        String inputFile = args[0];
        boolean verbose = containsOption(args, OPTION_VERBOSE);

        Path inputPath = Paths.get(inputFile);
        if (!Files.exists(inputPath)) {
            System.err.println("错误: 文件不存在 '" + inputFile + "'");
            return;
        }

        try {
            log.info("开始解析FARM文件: {}", inputPath);
            FarmParser parser = new FarmParser();
            FarmFile farmFile = parser.parse(inputPath);

            // 打印FARM文件信息
            printFarmFileInfo(farmFile, verbose);

            log.info("FARM文件解析完成");
        } catch (IOException e) {
            log.error("解析FARM文件时发生错误: ", e);
            System.err.println("错误: 解析文件失败 - " + e.getMessage());
        }
    }

    /**
     * 处理export命令
     * @param args 命令参数
     */
    private void processExport(String[] args) {
        if (args.length < 1) {
            System.err.println("错误: 导出命令需要指定FARM文件路径");
            printUsage();
            return;
        }

        String inputFile = args[0];
        String outputFile = getOptionValue(args, OPTION_OUTPUT);

        // 如果未指定输出文件，则使用输入文件名加.json后缀
        if (outputFile == null) {
            outputFile = inputFile + ".json";
        }

        Path inputPath = Paths.get(inputFile);
        if (!Files.exists(inputPath)) {
            System.err.println("错误: 文件不存在 '" + inputFile + "'");
            return;
        }

        try {
            log.info("开始解析FARM文件: {}", inputPath);
            FarmParser parser = new FarmParser();
            FarmFile farmFile = parser.parse(inputPath);

            log.info("开始导出FARM数据: {}", outputFile);
            FarmExporter exporter = new FarmExporter();
            exporter.exportToJson(farmFile, outputFile);

            System.out.println("FARM数据已成功导出到: " + outputFile);
            log.info("FARM数据导出完成");
        } catch (IOException e) {
            log.error("处理FARM文件时发生错误: ", e);
            System.err.println("错误: 处理文件失败 - " + e.getMessage());
        }
    }

    /**
     * 打印FARM文件信息
     * @param farmFile FARM文件对象
     * @param verbose 是否打印详细信息
     */
    private void printFarmFileInfo(FarmFile farmFile, boolean verbose) {
        App.printFarmFileInfo(farmFile);
        App.printFarmTableInfo(farmFile.getFarmTable());

        if (verbose) {
            App.printFeatureInfo(farmFile);
            App.printAttributeInfo(farmFile);
        }
    }

    /**
     * 检查参数中是否包含指定选项
     * @param args 参数数组
     * @param option 要检查的选项
     * @return 如果包含选项则返回true，否则返回false
     */
    private boolean containsOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取选项的值
     * @param args 参数数组
     * @param option 选项名称
     * @return 选项值，如果选项不存在则返回null
     */
    private String getOptionValue(String[] args, String option) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(option)) {
                return args[i + 1];
            }
        }
        return null;
    }

    /**
     * 打印使用说明
     */
    private void printUsage() {
        System.out.println("FARM解析器 - OneSAF EDM FARM数据格式解析工具");
        System.out.println();
        System.out.println("用法:");
        System.out.println("  java -jar farm-parser.jar <命令> [选项] <文件路径>");
        System.out.println();
        System.out.println("命令:");
        System.out.println("  parse   解析FARM文件并打印信息摘要");
        System.out.println("  export  解析FARM文件并导出为JSON格式");
        System.out.println("  help    显示此帮助信息");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -o <文件路径>  指定输出文件路径 (仅用于export命令)");
        System.out.println("  -v             显示详细信息 (仅用于parse命令)");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar farm-parser.jar parse farm.dat");
        System.out.println("  java -jar farm-parser.jar parse -v farm.dat");
        System.out.println("  java -jar farm-parser.jar export farm.dat");
        System.out.println("  java -jar farm-parser.jar export -o output.json farm.dat");
    }
}