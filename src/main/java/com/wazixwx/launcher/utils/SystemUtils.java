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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 系统工具类
 * System Utility Class
 * 
 * 用于处理系统相关的功能，获取系统信息、文件操作等
 * Used for handling system-related functions, obtaining system information, file operations, etc.
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    
    /**
     * 获取操作系统类型
     * Get operating system type
     * 
     * @return 操作系统类型 | Operating system type
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
     * Get operating system version
     * 
     * @return 操作系统版本 | Operating system version
     */
    public static String getOsVersion() {
        return System.getProperty("os.version");
    }
    
    /**
     * 获取操作系统架构
     * Get operating system architecture
     * 
     * @return 操作系统架构 | Operating system architecture
     */
    public static String getOsArch() {
        return System.getProperty("os.arch");
    }
    
    /**
     * 获取Java版本
     * Get Java version
     * 
     * @return Java版本 | Java version
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    /**
     * 获取Java运行时版本信息
     * Get Java runtime version information
     * 
     * @return Java运行时版本 | Java runtime version
     */
    public static String getJavaRuntimeVersion() {
        return System.getProperty("java.runtime.version");
    }
    
    /**
     * 获取Java供应商
     * Get Java vendor
     * 
     * @return Java供应商 | Java vendor
     */
    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }
    
    /**
     * 获取Java路径
     * Get Java home path
     * 
     * @return Java路径 | Java home path
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }
    
    /**
     * 获取系统内存大小（MB）
     * Get total system memory (MB)
     * 
     * @return 系统内存大小 | Total system memory
     */
    public static long getTotalMemory() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 使用Java 9+接口
            if (osBean instanceof java.lang.management.PlatformManagedObject) {
                try {
                    // 反射调用新接口方法
                    java.lang.reflect.Method getTotalMemoryMethod = osBean.getClass().getMethod("getTotalMemorySize");
                    getTotalMemoryMethod.setAccessible(true);
                    long totalMemory = (long) getTotalMemoryMethod.invoke(osBean);
                    return totalMemory / (1024 * 1024);
                } catch (Exception e) {
                    // 忽略反射调用错误，继续尝试其他方法
                    logger.debug("无法使用Java 9+接口获取内存信息", e);
                }
            }
            
            // 尝试使用旧接口
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getTotalPhysicalMemorySize() / (1024 * 1024);
            }
            
            // 如果无法获取，使用运行时内存作为近似值
            return Runtime.getRuntime().maxMemory() / (1024 * 1024);
        } catch (Exception e) {
            logger.warn("获取系统内存大小失败", e);
            return -1;
        }
    }
    
    /**
     * 获取可用内存大小（MB）
     * Get free memory size (MB)
     * 
     * @return 可用内存大小 | Free memory size
     */
    public static long getFreeMemory() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 使用Java 9+接口
            if (osBean instanceof java.lang.management.PlatformManagedObject) {
                try {
                    // 反射调用新接口方法
                    java.lang.reflect.Method getFreeMemoryMethod = osBean.getClass().getMethod("getFreeMemorySize");
                    getFreeMemoryMethod.setAccessible(true);
                    long freeMemory = (long) getFreeMemoryMethod.invoke(osBean);
                    return freeMemory / (1024 * 1024);
                } catch (Exception e) {
                    // 忽略反射调用错误，继续尝试其他方法
                    logger.debug("无法使用Java 9+接口获取内存信息", e);
                }
            }
            
            // 尝试使用旧接口
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getFreePhysicalMemorySize() / (1024 * 1024);
            }
            
            // 如果无法获取，使用运行时内存作为近似值
            return Runtime.getRuntime().freeMemory() / (1024 * 1024);
        } catch (Exception e) {
            logger.warn("获取系统可用内存大小失败", e);
            return -1;
        }
    }
    
    /**
     * 获取系统CPU核心数
     * Get CPU cores count
     * 
     * @return CPU核心数 | CPU cores count
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * 获取系统CPU使用率
     * Get CPU usage percentage
     * 
     * @return CPU使用率（百分比）| CPU usage percentage
     */
    public static double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 尝试使用Java 9+接口
            if (osBean instanceof java.lang.management.PlatformManagedObject) {
                try {
                    // 反射调用新接口方法
                    java.lang.reflect.Method getCpuLoadMethod = osBean.getClass().getMethod("getCpuLoad");
                    getCpuLoadMethod.setAccessible(true);
                    double cpuLoad = (double) getCpuLoadMethod.invoke(osBean);
                    return cpuLoad * 100; // 转换为百分比
                } catch (Exception e) {
                    // 忽略反射调用错误，继续尝试其他方法
                    logger.debug("无法使用Java 9+接口获取CPU使用率", e);
                }
            }
            
            // 尝试使用旧接口
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
            }
            
            // 如果无法获取，返回-1
            return -1;
        } catch (Exception e) {
            logger.warn("获取CPU使用率失败", e);
            return -1;
        }
    }
    
    /**
     * 获取JVM内存使用情况
     * Get JVM memory usage
     * 
     * @return JVM内存使用情况描述 | JVM memory usage description
     */
    public static String getJvmMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
        
        return String.format("堆内存: %s MB / %s MB, 非堆内存: %s MB | Heap: %s MB / %s MB, Non-Heap: %s MB", 
            heapUsed, heapMax, nonHeapUsed, heapUsed, heapMax, nonHeapUsed);
    }
    
    /**
     * 获取系统用户名
     * Get system username
     * 
     * @return 系统用户名 | System username
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    
    /**
     * 获取用户主目录
     * Get user home directory
     * 
     * @return 用户主目录 | User home directory
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }
    
    /**
     * 获取系统临时目录
     * Get system temp directory
     * 
     * @return 系统临时目录 | System temp directory
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
    
    /**
     * 获取系统属性
     * Get system properties
     * 
     * @return 系统属性 | System properties
     */
    public static Properties getSystemProperties() {
        return System.getProperties();
    }
    
    /**
     * 检查文件是否存在
     * Check if file exists
     * 
     * @param path 文件路径 | File path
     * @return 是否存在 | Whether exists
     */
    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
    
    /**
     * 检查目录是否存在
     * Check if directory exists
     * 
     * @param path 目录路径 | Directory path
     * @return 是否存在 | Whether exists
     */
    public static boolean directoryExists(String path) {
        Path dirPath = Paths.get(path);
        return Files.exists(dirPath) && Files.isDirectory(dirPath);
    }
    
    /**
     * 创建目录
     * Create directory
     * 
     * @param path 目录路径 | Directory path
     * @return 是否成功 | Whether successful
     */
    public static boolean createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (IOException e) {
            logger.error("创建目录失败 | Failed to create directory: " + path, e);
            return false;
        }
    }
    
    /**
     * 删除文件
     * Delete file
     * 
     * @param path 文件路径 | File path
     * @return 是否成功 | Whether successful
     */
    public static boolean deleteFile(String path) {
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            logger.error("删除文件失败 | Failed to delete file: " + path, e);
            return false;
        }
    }
    
    /**
     * 获取文件大小
     * Get file size
     * 
     * @param path 文件路径 | File path
     * @return 文件大小（字节） | File size (bytes)
     */
    public static long getFileSize(String path) {
        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            logger.error("获取文件大小失败 | Failed to get file size: " + path, e);
            return -1;
        }
    }
    
    /**
     * 获取文件最后修改时间
     * Get file last modified time
     * 
     * @param path 文件路径 | File path
     * @return 最后修改时间（毫秒） | Last modified time (milliseconds)
     */
    public static long getFileLastModified(String path) {
        try {
            return Files.getLastModifiedTime(Paths.get(path)).toMillis();
        } catch (IOException e) {
            logger.error("获取文件最后修改时间失败 | Failed to get file last modified time: " + path, e);
            return -1;
        }
    }
    
    /**
     * 获取目录中的所有文件
     * Get all files in directory
     * 
     * @param path 目录路径 | Directory path
     * @return 文件列表 | File list
     */
    public static List<File> listFiles(String path) {
        List<File> files = new ArrayList<>();
        try {
            Files.list(Paths.get(path))
                .map(Path::toFile)
                .forEach(files::add);
        } catch (IOException e) {
            logger.error("获取目录文件列表失败 | Failed to list directory files: " + path, e);
        }
        return files;
    }
    
    /**
     * 获取目录中的所有文件（递归）
     * Get all files in directory (recursive)
     * 
     * @param path 目录路径 | Directory path
     * @return 文件列表 | File list
     */
    public static List<File> listFilesRecursive(String path) {
        List<File> files = new ArrayList<>();
        try {
            Files.walk(Paths.get(path))
                .map(Path::toFile)
                .filter(File::isFile)
                .forEach(files::add);
        } catch (IOException e) {
            logger.error("递归获取目录文件列表失败 | Failed to recursively list directory files: " + path, e);
        }
        return files;
    }
    
    /**
     * 执行系统命令
     * Execute system command
     * 
     * @param command 命令 | Command
     * @return 命令输出 | Command output
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
            
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            logger.error("执行系统命令失败 | Failed to execute system command: " + command, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "错误: " + e.getMessage();
        }
    }
    
    /**
     * 检查磁盘空间是否足够
     * Check if disk space is sufficient
     * 
     * @param path 路径 | Path
     * @param requiredSpace 所需空间（字节） | Required space (bytes)
     * @return 是否足够 | Whether sufficient
     */
    public static boolean hasEnoughDiskSpace(String path, long requiredSpace) {
        try {
            File file = new File(path);
            return file.getFreeSpace() >= requiredSpace;
        } catch (Exception e) {
            logger.error("检查磁盘空间失败 | Failed to check disk space", e);
            return false;
        }
    }
    
    /**
     * 获取系统信息摘要
     * Get system information summary
     * 
     * @return 系统信息摘要 | System information summary
     */
    public static String getSystemInfoSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("操作系统 | OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        summary.append("架构 | Architecture: ").append(System.getProperty("os.arch")).append("\n");
        summary.append("Java版本 | Java Version: ").append(System.getProperty("java.version")).append("\n");
        summary.append("Java供应商 | Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        summary.append("CPU核心数 | CPU Cores: ").append(getCpuCores()).append("\n");
        
        long totalMemory = getTotalMemory();
        if (totalMemory > 0) {
            summary.append("总内存 | Total Memory: ").append(formatMemorySize(totalMemory)).append("\n");
        }
        
        long freeMemory = getFreeMemory();
        if (freeMemory > 0) {
            summary.append("可用内存 | Free Memory: ").append(formatMemorySize(freeMemory)).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * 获取格式化的内存大小字符串
     * Get formatted memory size string
     * 
     * @param sizeInMB 内存大小（MB） | Memory size (MB)
     * @return 格式化后的字符串 | Formatted string
     */
    public static String formatMemorySize(long sizeInMB) {
        if (sizeInMB < 1024) {
            return sizeInMB + " MB";
        } else {
            double sizeInGB = sizeInMB / 1024.0;
            return DECIMAL_FORMAT.format(sizeInGB) + " GB";
        }
    }
    
    /**
     * 获取当前工作目录
     * Get current working directory
     * 
     * @return 当前工作目录 | Current working directory
     */
    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * 是否为Windows系统
     * Check if is Windows system
     * 
     * @return 是否为Windows | Whether is Windows
     */
    public static boolean isWindows() {
        return getOsType().equals("windows");
    }
    
    /**
     * 是否为macOS系统
     * Check if is macOS system
     * 
     * @return 是否为macOS | Whether is macOS
     */
    public static boolean isMacOS() {
        return getOsType().equals("macos");
    }
    
    /**
     * 是否为Linux系统
     * Check if is Linux system
     * 
     * @return 是否为Linux | Whether is Linux
     */
    public static boolean isLinux() {
        return getOsType().equals("linux");
    }
} 