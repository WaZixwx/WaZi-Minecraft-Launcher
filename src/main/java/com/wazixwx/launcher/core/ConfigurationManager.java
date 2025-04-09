package com.wazixwx.launcher.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 * Configuration Manager
 * 
 * 负责管理启动器的所有配置项，包括游戏目录、Java路径、内存设置等
 * Responsible for managing all configuration items of the launcher, including game directory, Java path, memory settings, etc.
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ConfigurationManager {
    private static final String CONFIG_FILE = "launcher_config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 配置映射
    // Configuration mapping
    private Map<String, Object> configMap;
    
    // 默认配置项
    // Default configuration items
    private static final String KEY_MINECRAFT_DIR = "minecraft.directory";
    private static final String KEY_JAVA_PATH = "java.path";
    private static final String KEY_MAX_MEMORY = "memory.max";
    private static final String KEY_MIN_MEMORY = "memory.min";
    private static final String KEY_WINDOW_WIDTH = "window.width";
    private static final String KEY_WINDOW_HEIGHT = "window.height";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";
    
    // 离线模式配置项
    // Offline mode configuration items
    private static final String KEY_OFFLINE_MODE = "game.offline_mode";
    private static final String KEY_OFFLINE_USERNAME = "game.offline_username";
    private static final String KEY_LAST_VERSION = "game.last_version";
    private static final String KEY_CUSTOM_JVM_ARGS = "game.custom_jvm_args";
    
    // 服务器直连配置项
    // Direct server connection configuration items
    private static final String KEY_SERVER_ADDRESS = "game.server_address";
    private static final String KEY_SERVER_PORT = "game.server_port";
    private static final String KEY_DIRECT_SERVER_CONNECTION = "game.direct_server_connection";
    
    // 窗口相关配置项
    // Window related configuration items
    private static final String KEY_AUTO_HIDE = "launcher.auto_hide";
    private static final String KEY_CLOSE_AFTER_LAUNCH = "launcher.close_after_launch";
    
    /**
     * 构造函数
     * Constructor
     */
    public ConfigurationManager() {
        this.configMap = new HashMap<>();
    }
    
    /**
     * 初始化配置管理器
     * Initialize the configuration manager
     */
    public void initialize() {
        loadConfig();
        setDefaultsIfMissing();
    }
    
    /**
     * 加载配置
     * Load configuration
     */
    private void loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                configMap = GSON.fromJson(json, Map.class);
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败 | Failed to load configuration file: " + e.getMessage());
            // 使用默认配置
            // Use default configuration
            configMap = new HashMap<>();
        }
    }
    
    /**
     * 设置缺失的默认值
     * Set missing default values
     */
    private void setDefaultsIfMissing() {
        // 设置默认Minecraft目录
        // Set default Minecraft directory
        if (!configMap.containsKey(KEY_MINECRAFT_DIR)) {
            String os = System.getProperty("os.name").toLowerCase();
            Path minecraftDirectory;
            
            if (os.contains("win")) {
                minecraftDirectory = Paths.get(System.getenv("APPDATA"), ".minecraft");
            } else if (os.contains("mac")) {
                minecraftDirectory = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
            } else {
                minecraftDirectory = Paths.get(System.getProperty("user.home"), ".minecraft");
            }
            
            configMap.put(KEY_MINECRAFT_DIR, minecraftDirectory.toString());
        }
        
        // 设置默认Java路径
        // Set default Java path
        if (!configMap.containsKey(KEY_JAVA_PATH)) {
            configMap.put(KEY_JAVA_PATH, System.getProperty("java.home"));
        }
        
        // 设置默认内存大小
        // Set default memory size
        if (!configMap.containsKey(KEY_MAX_MEMORY)) {
            configMap.put(KEY_MAX_MEMORY, 2048); // 2GB
        }
        
        if (!configMap.containsKey(KEY_MIN_MEMORY)) {
            configMap.put(KEY_MIN_MEMORY, 1024); // 1GB
        }
        
        // 设置默认窗口大小
        // Set default window size
        if (!configMap.containsKey(KEY_WINDOW_WIDTH)) {
            configMap.put(KEY_WINDOW_WIDTH, 900);
        }
        
        if (!configMap.containsKey(KEY_WINDOW_HEIGHT)) {
            configMap.put(KEY_WINDOW_HEIGHT, 600);
        }
        
        // 设置默认语言
        // Set default language
        if (!configMap.containsKey(KEY_LANGUAGE)) {
            configMap.put(KEY_LANGUAGE, "zh_CN");
        }
        
        // 设置默认主题
        // Set default theme
        if (!configMap.containsKey(KEY_THEME)) {
            configMap.put(KEY_THEME, "light");
        }
        
        // 设置离线模式默认值
        // Set offline mode default values
        if (!configMap.containsKey(KEY_OFFLINE_MODE)) {
            configMap.put(KEY_OFFLINE_MODE, false);
        }
        
        if (!configMap.containsKey(KEY_OFFLINE_USERNAME)) {
            configMap.put(KEY_OFFLINE_USERNAME, "Player");
        }
        
        if (!configMap.containsKey(KEY_LAST_VERSION)) {
            configMap.put(KEY_LAST_VERSION, "");
        }
        
        if (!configMap.containsKey(KEY_CUSTOM_JVM_ARGS)) {
            configMap.put(KEY_CUSTOM_JVM_ARGS, "");
        }
        
        // 设置启动器窗口自动隐藏默认值（默认启用）
        // Set launcher auto hide default value (enabled by default)
        if (!configMap.containsKey(KEY_AUTO_HIDE)) {
            configMap.put(KEY_AUTO_HIDE, true);
        }
        
        // 设置启动器窗口游戏启动后关闭默认值（默认禁用）
        // Set launcher close after launch default value (disabled by default)
        if (!configMap.containsKey(KEY_CLOSE_AFTER_LAUNCH)) {
            configMap.put(KEY_CLOSE_AFTER_LAUNCH, false);
        }
        
        // 设置服务器直连默认值
        // Set direct server connection default values
        if (!configMap.containsKey(KEY_SERVER_ADDRESS)) {
            configMap.put(KEY_SERVER_ADDRESS, "");
        }
        
        if (!configMap.containsKey(KEY_SERVER_PORT)) {
            configMap.put(KEY_SERVER_PORT, 25565); // Minecraft默认端口 | Minecraft default port
        }
        
        if (!configMap.containsKey(KEY_DIRECT_SERVER_CONNECTION)) {
            configMap.put(KEY_DIRECT_SERVER_CONNECTION, false); // 默认不启用服务器直连 | Direct server connection disabled by default
        }
    }
    
    /**
     * 保存配置
     * Save configuration
     */
    public void save() {
        try {
            String json = GSON.toJson(configMap);
            FileUtils.writeStringToFile(new File(CONFIG_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("保存配置文件失败 | Failed to save configuration file: " + e.getMessage());
        }
    }
    
    /**
     * 获取Minecraft目录
     * Get Minecraft directory
     * 
     * @return Minecraft目录路径 | Minecraft directory path
     */
    public Path getMinecraftDirectory() {
        return Paths.get((String) configMap.get(KEY_MINECRAFT_DIR));
    }
    
    /**
     * 设置Minecraft目录
     * Set Minecraft directory
     * 
     * @param directory 目录路径 | Directory path
     */
    public void setMinecraftDirectory(Path directory) {
        configMap.put(KEY_MINECRAFT_DIR, directory.toString());
    }
    
    /**
     * 获取Java路径
     * Get Java path
     * 
     * @return Java路径 | Java path
     */
    public Path getJavaPath() {
        return Paths.get((String) configMap.get(KEY_JAVA_PATH));
    }
    
    /**
     * 设置Java路径
     * Set Java path
     * 
     * @param path Java路径 | Java path
     */
    public void setJavaPath(Path path) {
        configMap.put(KEY_JAVA_PATH, path.toString());
    }
    
    /**
     * 获取最大内存
     * Get maximum memory
     * 
     * @return 最大内存(MB) | Maximum memory(MB)
     */
    public int getMaxMemory() {
        return ((Number) configMap.get(KEY_MAX_MEMORY)).intValue();
    }
    
    /**
     * 设置最大内存
     * Set maximum memory
     * 
     * @param memory 最大内存(MB) | Maximum memory(MB)
     */
    public void setMaxMemory(int memory) {
        configMap.put(KEY_MAX_MEMORY, memory);
    }
    
    /**
     * 获取最小内存
     * Get minimum memory
     * 
     * @return 最小内存(MB) | Minimum memory(MB)
     */
    public int getMinMemory() {
        return ((Number) configMap.get(KEY_MIN_MEMORY)).intValue();
    }
    
    /**
     * 设置最小内存
     * Set minimum memory
     * 
     * @param memory 最小内存(MB) | Minimum memory(MB)
     */
    public void setMinMemory(int memory) {
        configMap.put(KEY_MIN_MEMORY, memory);
    }
    
    /**
     * 获取窗口宽度
     * Get window width
     * 
     * @return 窗口宽度 | Window width
     */
    public int getWindowWidth() {
        return ((Number) configMap.get(KEY_WINDOW_WIDTH)).intValue();
    }
    
    /**
     * 获取窗口高度
     * Get window height
     * 
     * @return 窗口高度 | Window height
     */
    public int getWindowHeight() {
        return ((Number) configMap.get(KEY_WINDOW_HEIGHT)).intValue();
    }
    
    /**
     * 获取语言
     * Get language
     * 
     * @return 语言代码 | Language code
     */
    public String getLanguage() {
        return (String) configMap.get(KEY_LANGUAGE);
    }
    
    /**
     * 设置语言
     * Set language
     * 
     * @param language 语言代码 | Language code
     */
    public void setLanguage(String language) {
        configMap.put(KEY_LANGUAGE, language);
    }
    
    /**
     * 获取主题
     * Get theme
     * 
     * @return 主题名称 | Theme name
     */
    public String getTheme() {
        return (String) configMap.get(KEY_THEME);
    }
    
    /**
     * 设置主题
     * Set theme
     * 
     * @param theme 主题名称 | Theme name
     */
    public void setTheme(String theme) {
        configMap.put(KEY_THEME, theme);
    }
    
    /**
     * 获取通用配置项
     * Get general configuration item
     * 
     * @param key 配置键 | Configuration key
     * @return 配置值 | Configuration value
     */
    public Object get(String key) {
        return configMap.get(key);
    }
    
    /**
     * 获取通用配置项，如果不存在则返回默认值
     * Get general configuration item, return default value if not exists
     * 
     * @param key 配置键 | Configuration key
     * @param defaultValue 默认值 | Default value
     * @return 配置值 | Configuration value
     */
    public Object get(String key, Object defaultValue) {
        return configMap.getOrDefault(key, defaultValue);
    }
    
    /**
     * 设置通用配置项
     * Set general configuration item
     * 
     * @param key 配置键 | Configuration key
     * @param value 配置值 | Configuration value
     */
    public void set(String key, Object value) {
        configMap.put(key, value);
    }
    
    /**
     * 获取是否为离线模式
     * Get whether it is offline mode
     * 
     * @return 是否为离线模式 | Whether it is offline mode
     */
    public boolean isOfflineMode() {
        return (boolean) configMap.getOrDefault(KEY_OFFLINE_MODE, false);
    }
    
    /**
     * 设置离线模式
     * Set offline mode
     * 
     * @param enabled 是否启用离线模式 | Whether to enable offline mode
     */
    public void setOfflineMode(boolean enabled) {
        configMap.put(KEY_OFFLINE_MODE, enabled);
        save();
    }
    
    /**
     * 获取离线模式用户名
     * Get offline mode username
     * 
     * @return 用户名 | Username
     */
    public String getOfflineUsername() {
        return (String) configMap.getOrDefault(KEY_OFFLINE_USERNAME, "Player");
    }
    
    /**
     * 设置离线模式用户名
     * Set offline mode username
     * 
     * @param username 用户名 | Username
     */
    public void setOfflineUsername(String username) {
        configMap.put(KEY_OFFLINE_USERNAME, username);
        save();
    }
    
    /**
     * 获取最后使用的版本
     * Get last used version
     * 
     * @return 版本ID | Version ID
     */
    public String getLastVersion() {
        return (String) configMap.getOrDefault(KEY_LAST_VERSION, "");
    }
    
    /**
     * 设置最后使用的版本
     * Set last used version
     * 
     * @param version 版本ID | Version ID
     */
    public void setLastVersion(String version) {
        configMap.put(KEY_LAST_VERSION, version);
        save();
    }
    
    /**
     * 获取自定义JVM参数
     * Get custom JVM arguments
     * 
     * @return 自定义JVM参数 | Custom JVM arguments
     */
    public String getCustomJvmArgs() {
        return (String) configMap.getOrDefault(KEY_CUSTOM_JVM_ARGS, "");
    }
    
    /**
     * 设置自定义JVM参数
     * Set custom JVM arguments
     * 
     * @param args 自定义JVM参数 | Custom JVM arguments
     */
    public void setCustomJvmArgs(String args) {
        configMap.put(KEY_CUSTOM_JVM_ARGS, args);
    }
    
    /**
     * 获取启动器自动隐藏选项
     * Get launcher auto hide option
     * 
     * @return 是否自动隐藏 | Whether to auto hide
     */
    public boolean isAutoHideEnabled() {
        return (Boolean) configMap.getOrDefault(KEY_AUTO_HIDE, true);
    }
    
    /**
     * 设置启动器自动隐藏选项
     * Set launcher auto hide option
     * 
     * @param enabled 是否启用 | Whether to enable
     */
    public void setAutoHideEnabled(boolean enabled) {
        configMap.put(KEY_AUTO_HIDE, enabled);
    }
    
    /**
     * 获取启动游戏后关闭启动器选项
     * Get close launcher after game launch option
     * 
     * @return 是否关闭 | Whether to close
     */
    public boolean isCloseAfterLaunch() {
        return (Boolean) configMap.getOrDefault(KEY_CLOSE_AFTER_LAUNCH, false);
    }
    
    /**
     * 设置启动游戏后关闭启动器选项
     * Set close launcher after game launch option
     * 
     * @param enabled 是否启用 | Whether to enable
     */
    public void setCloseAfterLaunch(boolean enabled) {
        configMap.put(KEY_CLOSE_AFTER_LAUNCH, enabled);
    }
    
    /**
     * 获取服务器地址
     * Get server address
     * 
     * @return 服务器地址 | Server address
     */
    public String getServerAddress() {
        return (String) configMap.getOrDefault(KEY_SERVER_ADDRESS, "");
    }
    
    /**
     * 设置服务器地址
     * Set server address
     * 
     * @param address 服务器地址 | Server address
     */
    public void setServerAddress(String address) {
        configMap.put(KEY_SERVER_ADDRESS, address);
        save();
    }
    
    /**
     * 获取服务器端口
     * Get server port
     * 
     * @return 服务器端口 | Server port
     */
    public int getServerPort() {
        return ((Number) configMap.getOrDefault(KEY_SERVER_PORT, 25565)).intValue();
    }
    
    /**
     * 设置服务器端口
     * Set server port
     * 
     * @param port 服务器端口 | Server port
     */
    public void setServerPort(int port) {
        configMap.put(KEY_SERVER_PORT, port);
        save();
    }
    
    /**
     * 获取服务器直连启用状态
     * Get direct server connection enabled status
     * 
     * @return 是否启用服务器直连 | Whether direct server connection is enabled
     */
    public boolean isDirectServerConnectionEnabled() {
        return (Boolean) configMap.getOrDefault(KEY_DIRECT_SERVER_CONNECTION, false);
    }
    
    /**
     * 设置服务器直连启用状态
     * Set direct server connection enabled status
     * 
     * @param enabled 是否启用 | Whether to enable
     */
    public void setDirectServerConnectionEnabled(boolean enabled) {
        configMap.put(KEY_DIRECT_SERVER_CONNECTION, enabled);
        save();
    }
} 