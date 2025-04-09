package com.wazixwx.launcher.core;

import com.wazixwx.launcher.service.AccountService;
import com.wazixwx.launcher.service.SkinService;
import com.wazixwx.launcher.service.VersionManager;
import com.wazixwx.launcher.utils.LogUtils;
import java.util.concurrent.CompletableFuture;

/**
 * 启动器核心类
 * The core class of the launcher
 * 
 * 负责协调启动器的各个组件和服务
 * Responsible for coordinating all components and services of the launcher
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class LauncherCore {
    private static LauncherCore instance;
    private final ConfigurationManager configManager;
    private final VersionManager versionManager;
    private final AccountService accountService;
    private final SkinService skinService;
    
    /**
     * 私有构造函数
     * Private constructor
     */
    private LauncherCore() {
        this.configManager = new ConfigurationManager();
        
        // 使用配置管理器的Minecraft目录初始化版本管理器
        // Initialize version manager with Minecraft directory from configuration manager
        this.versionManager = new VersionManager(
                this.configManager.getMinecraftDirectory().toString());
        
        // 初始化账号服务
        // Initialize account service
        this.accountService = new AccountService();
        
        // 初始化皮肤服务
        // Initialize skin service
        this.skinService = new SkinService();
    }
    
    /**
     * 获取LauncherCore实例
     * Get the LauncherCore instance
     * 
     * @return LauncherCore实例 | LauncherCore instance
     */
    public static synchronized LauncherCore getInstance() {
        if (instance == null) {
            instance = new LauncherCore();
        }
        return instance;
    }
    
    /**
     * 初始化启动器
     * Initialize the launcher
     * 
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                LogUtils.info("正在初始化启动器 | Initializing launcher");
                
                // 初始化配置
                // Initialize configuration
                configManager.initialize();
                LogUtils.info("配置初始化完成 | Configuration initialized");
                
                // 加载版本列表
                // Load version list
                versionManager.getAllVersions();
                LogUtils.info("版本列表加载完成 | Version list loaded");
                
                // 离线模式配置
                // Offline mode configuration
                if (configManager.isOfflineMode()) {
                    LogUtils.info("当前为离线模式 | Current mode: Offline");
                    LogUtils.info("离线用户名: " + configManager.getOfflineUsername() + 
                            " | Offline username: " + configManager.getOfflineUsername());
                    
                    if (skinService.getSelectedSkin() != null) {
                        LogUtils.info("已选择皮肤: " + skinService.getSelectedSkin().getName() + 
                                " | Selected skin: " + skinService.getSelectedSkin().getName());
                    } else {
                        LogUtils.info("未选择皮肤 | No skin selected");
                    }
                } else {
                    LogUtils.info("当前为在线模式 | Current mode: Online");
                }
                
                LogUtils.info("启动器初始化完成 | Launcher initialization completed");
            } catch (Exception e) {
                LogUtils.error("启动器初始化失败 | Launcher initialization failed", e);
                throw e;
            }
        });
    }
    
    /**
     * 获取配置管理器
     * Get the configuration manager
     * 
     * @return ConfigurationManager实例 | ConfigurationManager instance
     */
    public ConfigurationManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取版本管理器
     * Get the version manager
     * 
     * @return VersionManager实例 | VersionManager instance
     */
    public VersionManager getVersionManager() {
        return versionManager;
    }
    
    /**
     * 获取账号服务
     * Get the account service
     * 
     * @return AccountService实例 | AccountService instance
     */
    public AccountService getAccountService() {
        return accountService;
    }
    
    /**
     * 获取皮肤服务
     * Get the skin service
     * 
     * @return SkinService实例 | SkinService instance
     */
    public SkinService getSkinService() {
        return skinService;
    }
} 