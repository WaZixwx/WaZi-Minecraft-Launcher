package com.wazixwx.launcher.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 系统工具类
 * 用于处理系统相关的功能
 */
public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);
    
    /**
     * 获取操作系统类型
     * @return 操作系统类型
     */
    public static String getOsType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return "windows";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return "macos";
        } else if (os.contains("linux")) {
            return "linux";
        } else {
            return "unknown";
        }
    }
    
    /**
     * 获取操作系统版本
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        return System.getProperty("os.version");
    }
    
    /**
     * 获取操作系统架构
     * @return 操作系统架构
     */
    public static String getOsArch() {
        return System.getProperty("os.arch");
    }
    
    /**
     * 获取Java版本
     * @return Java版本
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    /**
     * 获取Java供应商
     * @return Java供应商
     */
    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }
    
    /**
     * 获取Java路径
     * @return Java路径
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }
    
    /**
     * 获取系统内存大小（MB）
     * @return 系统内存大小
     */
    public static long getTotalMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getTotalPhysicalMemorySize() / (1024 * 1024);
        }
        return -1;
    }
    
    /**
     * 获取可用内存大小（MB）
     * @return 可用内存大小
     */
    public static long getFreeMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getFreePhysicalMemorySize() / (1024 * 1024);
        }
        return -1;
    }
    
    /**
     * 获取系统CPU核心数
     * @return CPU核心数
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * 获取系统用户名
     * @return 系统用户名
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    
    /**
     * 获取用户主目录
     * @return 用户主目录
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }
    
    /**
     * 获取系统临时目录
     * @return 系统临时目录
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
    
    /**
     * 获取系统属性
     * @return 系统属性
     */
    public static Properties getSystemProperties() {
        return System.getProperties();
    }
    
    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
    
    /**
     * 检查目录是否存在
     * @param path 目录路径
     * @return 是否存在
     */
    public static boolean directoryExists(String path) {
        Path dirPath = Paths.get(path);
        return Files.exists(dirPath) && Files.isDirectory(dirPath);
    }
    
    /**
     * 创建目录
     * @param path 目录路径
     * @return 是否成功
     */
    public static boolean createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (IOException e) {
            logger.error("创建目录失败: " + path, e);
            return false;
        }
    }
    
    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否成功
     */
    public static boolean deleteFile(String path) {
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            logger.error("删除文件失败: " + path, e);
            return false;
        }
    }
    
    /**
     * 获取文件大小
     * @param path 文件路径
     * @return 文件大小（字节）
     */
    public static long getFileSize(String path) {
        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            logger.error("获取文件大小失败: " + path, e);
            return -1;
        }
    }
    
    /**
     * 获取文件最后修改时间
     * @param path 文件路径
     * @return 最后修改时间（毫秒）
     */
    public static long getFileLastModified(String path) {
        try {
            return Files.getLastModifiedTime(Paths.get(path)).toMillis();
        } catch (IOException e) {
            logger.error("获取文件最后修改时间失败: " + path, e);
            return -1;
        }
    }
    
    /**
     * 获取目录中的所有文件
     * @param path 目录路径
     * @return 文件列表
     */
    public static List<File> listFiles(String path) {
        List<File> files = new ArrayList<>();
        try {
            Files.list(Paths.get(path))
                .map(Path::toFile)
                .forEach(files::add);
        } catch (IOException e) {
            logger.error("获取目录文件列表失败: " + path, e);
        }
        return files;
    }
    
    /**
     * 获取目录中的所有文件（递归）
     * @param path 目录路径
     * @return 文件列表
     */
    public static List<File> listFilesRecursive(String path) {
        List<File> files = new ArrayList<>();
        try {
            Files.walk(Paths.get(path))
                .map(Path::toFile)
                .forEach(files::add);
        } catch (IOException e) {
            logger.error("获取目录文件列表失败: " + path, e);
        }
        return files;
    }
    
    /**
     * 执行系统命令
     * @param command 命令
     * @return 命令输出
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("执行命令失败: " + command, e);
        }
        return output.toString();
    }
    
    /**
     * 检查是否有足够的磁盘空间
     * @param path 路径
     * @param requiredSpace 所需空间（字节）
     * @return 是否有足够空间
     */
    public static boolean hasEnoughDiskSpace(String path, long requiredSpace) {
        File file = new File(path);
        return file.getFreeSpace() >= requiredSpace;
    }
    
    /**
     * 获取系统信息摘要
     * @return 系统信息摘要
     */
    public static String getSystemInfoSummary() {
        StringBuilder info = new StringBuilder();
        info.append("操作系统: ").append(getOsType()).append(" ").append(getOsVersion()).append(" (").append(getOsArch()).append(")\n");
        info.append("Java版本: ").append(getJavaVersion()).append(" (").append(getJavaVendor()).append(")\n");
        info.append("CPU核心数: ").append(getCpuCores()).append("\n");
        
        long totalMemory = getTotalMemory();
        long freeMemory = getFreeMemory();
        if (totalMemory > 0) {
            info.append("系统内存: ").append(totalMemory).append("MB (可用: ").append(freeMemory).append("MB)\n");
        }
        
        info.append("用户名: ").append(getUserName()).append("\n");
        info.append("用户主目录: ").append(getUserHome()).append("\n");
        info.append("临时目录: ").append(getTempDir());
        
        return info.toString();
    }
} 