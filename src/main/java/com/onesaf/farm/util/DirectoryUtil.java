package com.onesaf.farm.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录和文件工具类
 */
@Slf4j
public class DirectoryUtil {

    /**
     * 验证目录是否存在，如果不存在则创建
     *
     * @param directory 需要验证的目录
     * @return 目录是否有效
     */
    public static boolean verifyDirectory(String directory) {
        Path path = Paths.get(directory);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建目录: {}", directory);
            }
            return Files.isDirectory(path) && Files.isReadable(path);
        } catch (Exception e) {
            log.error("验证或创建目录失败: {}", directory, e);
            return false;
        }
    }

    /**
     * 确保文件路径存在
     *
     * @param filePath 文件路径
     * @return 文件是否有效
     */
    public static boolean ensureFilePathExists(String filePath) {
        File file = new File(filePath);

        // 如果是目录，确保目录存在
        if (!file.getName().contains(".")) {
            return verifyDirectory(filePath);
        }

        // 确保父目录存在
        String parentDir = file.getParent();
        if (parentDir != null) {
            return verifyDirectory(parentDir);
        }

        return true;
    }

    /**
     * 获取文件名(不包含路径)
     *
     * @param filePath 文件路径
     * @return 文件名
     */
    public static String getFileName(String filePath) {
        return new File(filePath).getName();
    }

    /**
     * 检查文件是否存在且可读
     *
     * @param filePath 文件路径
     * @return 文件是否存在且可读
     */
    public static boolean isFileReadable(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.canRead();
    }
}