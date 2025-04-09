package com.wazixwx.launcher.core;

import com.wazixwx.launcher.service.AccountService;
import com.wazixwx.launcher.service.MinecraftVersionService;
import com.wazixwx.launcher.service.SkinService;
import com.wazixwx.launcher.service.VersionManager;
import com.wazixwx.launcher.service.ModService;
import com.wazixwx.launcher.ui.MainWindow;
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
    /**
     * 启动器版本号
     * Launcher version
     */
    public static final String VERSION = "1.0.0";
    
    private static LauncherCore instance;
    private final ConfigurationManager configManager;
    private final VersionManager versionManager;
    private final AccountService accountService;
    private final SkinService skinService;
    private final MinecraftVersionService minecraftVersionService;
    private final ModService modService;
    private MainWindow mainWindow; // 主窗口引用 | Main window reference
    
    /**
     * 私有构造函数
     * Private constructor
     */
    private LauncherCore() {
        this.configManager = new ConfigurationManager();
        
        // 初始化Minecraft版本服务
        // Initialize Minecraft version service
        this.minecraftVersionService = new MinecraftVersionService();
        
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
        
        // 初始化模组服务
        // Initialize mod service
        this.modService = new ModService();
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
     * 设置主窗口引用
     * Set main window reference
     * 
     * @param mainWindow 主窗口实例 | Main window instance
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        LogUtils.info("主窗口引用已设置 | Main window reference set");
    }
    
    /**
     * 获取主窗口引用
     * Get main window reference
     * 
     * @return MainWindow实例 | MainWindow instance
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }
    
    /**
     * 隐藏启动器窗口
     * Hide launcher window
     */
    public void hideWindow() {
        if (mainWindow != null) {
            LogUtils.info("正在隐藏启动器窗口 | Hiding launcher window");
            mainWindow.hide();
        }
    }
    
    /**
     * 显示启动器窗口
     * Show launcher window
     */
    public void showWindow() {
        if (mainWindow != null) {
            LogUtils.info("正在显示启动器窗口 | Showing launcher window");
            mainWindow.show();
        }
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
    
    /**
     * 获取Minecraft版本服务
     * Get the Minecraft version service
     * 
     * @return MinecraftVersionService实例 | MinecraftVersionService instance
     */
    public MinecraftVersionService getMinecraftVersionService() {
        return minecraftVersionService;
    }
    
    /**
     * 获取模组服务
     * Get the mod service
     * 
     * @return ModService实例 | ModService instance
     */
    public ModService getModService() {
        return modService;
    }
} 