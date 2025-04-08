package com.wazixwx.launcher.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志工具类
 * 用于处理日志记录
 */
public class LogUtils {
    private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);
    private static final String LOG_DIR = "logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private static File logFile;
    private static PrintWriter logWriter;
    private static boolean initialized = false;
    
    /**
     * 初始化日志系统
     */
    public static void init() {
        if (initialized) {
            return;
        }
        
        try {
            // 确保日志目录存在
            Path logDir = getLogDir();
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // 创建日志文件
            String fileName = "launcher-" + FILE_DATE_FORMAT.format(new Date()) + ".log";
            logFile = logDir.resolve(fileName).toFile();
            
            // 创建日志写入器
            logWriter = new PrintWriter(new FileWriter(logFile, true), true);
            
            // 写入日志头
            logWriter.println("==================================================");
            logWriter.println("WaZi Minecraft Launcher 日志 - " + DATE_FORMAT.format(new Date()));
            logWriter.println("==================================================");
            
            initialized = true;
            info("日志系统初始化完成");
        } catch (IOException e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭日志系统
     */
    public static void close() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
        
        initialized = false;
    }
    
    /**
     * 记录调试日志
     * @param message 日志消息
     */
    public static void debug(String message) {
        logger.debug(message);
        writeLog("DEBUG", message);
    }
    
    /**
     * 记录调试日志
     * @param message 日志消息
     * @param t 异常
     */
    public static void debug(String message, Throwable t) {
        logger.debug(message, t);
        writeLog("DEBUG", message, t);
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    public static void info(String message) {
        logger.info(message);
        writeLog("INFO", message);
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     * @param t 异常
     */
    public static void info(String message, Throwable t) {
        logger.info(message, t);
        writeLog("INFO", message, t);
    }
    
    /**
     * 记录警告日志
     * @param message 日志消息
     */
    public static void warn(String message) {
        logger.warn(message);
        writeLog("WARN", message);
    }
    
    /**
     * 记录警告日志
     * @param message 日志消息
     * @param t 异常
     */
    public static void warn(String message, Throwable t) {
        logger.warn(message, t);
        writeLog("WARN", message, t);
    }
    
    /**
     * 记录错误日志
     * @param message 日志消息
     */
    public static void error(String message) {
        logger.error(message);
        writeLog("ERROR", message);
    }
    
    /**
     * 记录错误日志
     * @param message 日志消息
     * @param t 异常
     */
    public static void error(String message, Throwable t) {
        logger.error(message, t);
        writeLog("ERROR", message, t);
    }
    
    /**
     * 写入日志
     * @param level 日志级别
     * @param message 日志消息
     */
    private static void writeLog(String level, String message) {
        if (!initialized) {
            init();
        }
        
        if (logWriter != null) {
            String timestamp = DATE_FORMAT.format(new Date());
            String threadName = Thread.currentThread().getName();
            String userId = MDC.get("userId");
            String userIdStr = userId != null ? "[" + userId + "] " : "";
            
            logWriter.printf("%s [%s] %s%s - %s%n", timestamp, level, userIdStr, threadName, message);
        }
    }
    
    /**
     * 写入日志
     * @param level 日志级别
     * @param message 日志消息
     * @param t 异常
     */
    private static void writeLog(String level, String message, Throwable t) {
        if (!initialized) {
            init();
        }
        
        if (logWriter != null) {
            String timestamp = DATE_FORMAT.format(new Date());
            String threadName = Thread.currentThread().getName();
            String userId = MDC.get("userId");
            String userIdStr = userId != null ? "[" + userId + "] " : "";
            
            logWriter.printf("%s [%s] %s%s - %s%n", timestamp, level, userIdStr, threadName, message);
            t.printStackTrace(logWriter);
        }
    }
    
    /**
     * 获取日志目录
     * @return 日志目录路径
     */
    public static Path getLogDir() {
        return ConfigUtils.getConfigDir().resolve(LOG_DIR);
    }
    
    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put("userId", userId);
        } else {
            MDC.remove("userId");
        }
    }
    
    /**
     * 清除用户ID
     */
    public static void clearUserId() {
        MDC.remove("userId");
    }
    
    /**
     * 获取当前日志文件
     * @return 当前日志文件
     */
    public static File getCurrentLogFile() {
        return logFile;
    }
    
    /**
     * 清理旧日志文件
     * @param days 保留天数
     */
    public static void cleanOldLogs(int days) {
        try {
            Path logDir = getLogDir();
            if (!Files.exists(logDir)) {
                return;
            }
            
            long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
            
            Files.list(logDir)
                .filter(path -> path.toString().endsWith(".log"))
                .forEach(path -> {
                    try {
                        if (path.toFile().lastModified() < cutoffTime) {
                            Files.delete(path);
                            info("已删除旧日志文件: " + path);
                        }
                    } catch (IOException e) {
                        warn("删除旧日志文件失败: " + path, e);
                    }
                });
        } catch (IOException e) {
            error("清理旧日志文件失败", e);
        }
    }
} 