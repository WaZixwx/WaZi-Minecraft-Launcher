package com.wazixwx.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置工具类
 * 用于处理窗口的配置和设置
 */
public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private static final String CONFIG_FILE = "config.json";
    private static final String CONFIG_DIR = ".wazimc";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static JsonObject config;
    private static final Map<String, Object> defaultConfig = new HashMap<>();
    
    static {
        // 设置默认配置
        defaultConfig.put("theme", "light");
        defaultConfig.put("language", "zh_CN");
        defaultConfig.put("gamePath", getDefaultGamePath());
        defaultConfig.put("javaPath", getDefaultJavaPath());
        defaultConfig.put("memory", 2048);
        defaultConfig.put("username", "");
        defaultConfig.put("uuid", "");
        defaultConfig.put("accessToken", "");
        defaultConfig.put("lastVersion", "");
        defaultConfig.put("windowWidth", 1280);
        defaultConfig.put("windowHeight", 720);
        defaultConfig.put("windowX", -1);
        defaultConfig.put("windowY", -1);
        defaultConfig.put("maximized", false);
        defaultConfig.put("showFPS", false);
        defaultConfig.put("enableBlur", true);
        defaultConfig.put("blurRadius", 5.0);
        defaultConfig.put("blurIterations", 3);
    }
    
    /**
     * 初始化配置
     */
    public static void init() {
        try {
            // 确保配置目录存在
            Path configDir = getConfigDir();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            // 加载配置文件
            Path configPath = configDir.resolve(CONFIG_FILE);
            if (Files.exists(configPath)) {
                String json = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
                config = JsonParser.parseString(json).getAsJsonObject();
                
                // 检查并添加缺失的配置项
                boolean modified = false;
                for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
                    if (!config.has(entry.getKey())) {
                        config.addProperty(entry.getKey(), entry.getValue().toString());
                        modified = true;
                    }
                }
                
                // 如果有修改，保存配置
                if (modified) {
                    saveConfig();
                }
            } else {
                // 创建默认配置
                config = new JsonObject();
                for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
                    config.addProperty(entry.getKey(), entry.getValue().toString());
                }
                saveConfig();
            }
            
            logger.info("配置初始化完成");
        } catch (Exception e) {
            logger.error("初始化配置失败", e);
            // 创建默认配置
            config = new JsonObject();
            for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
                config.addProperty(entry.getKey(), entry.getValue().toString());
            }
        }
    }
    
    /**
     * 保存配置
     */
    public static void saveConfig() {
        try {
            Path configPath = getConfigDir().resolve(CONFIG_FILE);
            String json = GSON.toJson(config);
            Files.write(configPath, json.getBytes(StandardCharsets.UTF_8));
            logger.info("配置已保存");
        } catch (Exception e) {
            logger.error("保存配置失败", e);
        }
    }
    
    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        if (config == null) {
            init();
        }
        
        if (config.has(key)) {
            return config.get(key).getAsString();
        }
        
        return defaultValue;
    }
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    public static String getString(String key) {
        return getString(key, defaultConfig.get(key).toString());
    }
    
    /**
     * 获取整数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static int getInt(String key, int defaultValue) {
        if (config == null) {
            init();
        }
        
        if (config.has(key)) {
            return config.get(key).getAsInt();
        }
        
        return defaultValue;
    }
    
    /**
     * 获取整数配置值
     * @param key 配置键
     * @return 配置值
     */
    public static int getInt(String key) {
        return getInt(key, Integer.parseInt(defaultConfig.get(key).toString()));
    }
    
    /**
     * 获取布尔配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (config == null) {
            init();
        }
        
        if (config.has(key)) {
            return config.get(key).getAsBoolean();
        }
        
        return defaultValue;
    }
    
    /**
     * 获取布尔配置值
     * @param key 配置键
     * @return 配置值
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, Boolean.parseBoolean(defaultConfig.get(key).toString()));
    }
    
    /**
     * 获取双精度浮点数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static double getDouble(String key, double defaultValue) {
        if (config == null) {
            init();
        }
        
        if (config.has(key)) {
            return config.get(key).getAsDouble();
        }
        
        return defaultValue;
    }
    
    /**
     * 获取双精度浮点数配置值
     * @param key 配置键
     * @return 配置值
     */
    public static double getDouble(String key) {
        return getDouble(key, Double.parseDouble(defaultConfig.get(key).toString()));
    }
    
    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setString(String key, String value) {
        if (config == null) {
            init();
        }
        
        config.addProperty(key, value);
        saveConfig();
    }
    
    /**
     * 设置整数配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setInt(String key, int value) {
        if (config == null) {
            init();
        }
        
        config.addProperty(key, value);
        saveConfig();
    }
    
    /**
     * 设置布尔配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setBoolean(String key, boolean value) {
        if (config == null) {
            init();
        }
        
        config.addProperty(key, value);
        saveConfig();
    }
    
    /**
     * 设置双精度浮点数配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setDouble(String key, double value) {
        if (config == null) {
            init();
        }
        
        config.addProperty(key, value);
        saveConfig();
    }
    
    /**
     * 获取配置目录
     * @return 配置目录路径
     */
    public static Path getConfigDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR);
    }
    
    /**
     * 获取默认游戏路径
     * @return 默认游戏路径
     */
    private static String getDefaultGamePath() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows")) {
            return Paths.get(userHome, "AppData", "Roaming", ".minecraft").toString();
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", "minecraft").toString();
        } else {
            return Paths.get(userHome, ".minecraft").toString();
        }
    }
    
    /**
     * 获取默认Java路径
     * @return 默认Java路径
     */
    private static String getDefaultJavaPath() {
        String javaHome = System.getProperty("java.home");
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows")) {
            return Paths.get(javaHome, "bin", "java.exe").toString();
        } else {
            return Paths.get(javaHome, "bin", "java").toString();
        }
    }
    
    /**
     * 导出配置到文件
     * @param file 目标文件
     */
    public static void exportConfig(File file) {
        try {
            String json = GSON.toJson(config);
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
            logger.info("配置已导出到: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("导出配置失败", e);
        }
    }
    
    /**
     * 从文件导入配置
     * @param file 源文件
     */
    public static void importConfig(File file) {
        try {
            String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            config = JsonParser.parseString(json).getAsJsonObject();
            saveConfig();
            logger.info("配置已从 {} 导入", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("导入配置失败", e);
        }
    }
    
    /**
     * 重置配置为默认值
     */
    public static void resetConfig() {
        config = new JsonObject();
        for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
            config.addProperty(entry.getKey(), entry.getValue().toString());
        }
        saveConfig();
        logger.info("配置已重置为默认值");
    }
} 