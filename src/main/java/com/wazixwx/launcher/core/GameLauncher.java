package com.wazixwx.launcher.core;

import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.model.Skin;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.service.SkinService;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 游戏启动器类
 * Game Launcher Class
 * 
 * 负责构建启动参数并启动Minecraft游戏
 * Responsible for building launch parameters and launching Minecraft game
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class GameLauncher {
    private final ConfigurationManager configManager;
    private final SkinService skinService;
    
    /**
     * 构造函数
     * Constructor
     */
    public GameLauncher() {
        this.configManager = LauncherCore.getInstance().getConfigManager();
        this.skinService = LauncherCore.getInstance().getSkinService();
    }
    
    /**
     * 启动游戏
     * Launch the game
     * 
     * @param version 游戏版本 | Game version
     * @param account 玩家账号（可选，离线模式下为null）| Player account (optional, null in offline mode)
     * @return CompletableFuture<Process> 游戏进程 | Game process
     */
    public CompletableFuture<Process> launch(VersionMetadata version, Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 如果是离线模式，处理皮肤
                // If in offline mode, handle skin
                if (configManager.isOfflineMode()) {
                    prepareSkin();
                }
                
                // 构建启动命令
                // Build launch command
                List<String> commands = buildLaunchCommand(version, account);
                
                // 创建进程构建器
                // Create process builder
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                
                // 设置工作目录
                // Set working directory
                processBuilder.directory(configManager.getMinecraftDirectory().toFile());
                
                // 合并标准输出和错误输出
                // Merge standard output and error output
                processBuilder.redirectErrorStream(true);
                
                // 添加自定义JVM参数
                // Add custom JVM arguments
                String customJvmArgs = configManager.getCustomJvmArgs();
                if (!customJvmArgs.isEmpty()) {
                    commands.addAll(Arrays.asList(customJvmArgs.split(" ")));
                }
                
                // 启动进程
                // Start process
                Process process = processBuilder.start();
                
                // 记录启动信息
                // Log launch information
                LogUtils.info("游戏已启动 | Game launched: " + version.getId() + 
                    (configManager.isOfflineMode() ? " (离线模式 | Offline mode)" : ""));
                
                return process;
            } catch (IOException e) {
                LogUtils.error("启动游戏失败 | Failed to launch game", e);
                throw new RuntimeException("启动游戏失败 | Failed to launch game", e);
            }
        });
    }
    
    /**
     * 准备离线模式皮肤
     * Prepare offline mode skin
     * 
     * @throws IOException 如果文件操作失败 | If file operation fails
     */
    private void prepareSkin() throws IOException {
        Skin selectedSkin = skinService.getSelectedSkin();
        if (selectedSkin == null) {
            return;
        }
        
        // 创建离线皮肤目录
        // Create offline skin directory
        Path offlineSkinDir = configManager.getMinecraftDirectory().resolve("assets").resolve("skins");
        Files.createDirectories(offlineSkinDir);
        
        // 根据UUID生成皮肤文件名
        // Generate skin filename based on UUID
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + configManager.getOfflineUsername())
            .getBytes()).toString().replace("-", "");
        
        // 复制选中的皮肤到离线皮肤目录
        // Copy selected skin to offline skin directory
        Path skinPath = offlineSkinDir.resolve(uuid + ".png");
        Files.copy(selectedSkin.getFilePath(), skinPath, StandardCopyOption.REPLACE_EXISTING);
        
        LogUtils.info("准备离线模式皮肤完成 | Prepared offline mode skin: " + selectedSkin.getName());
    }
    
    /**
     * 构建启动命令
     * Build launch command
     * 
     * @param version 游戏版本 | Game version
     * @param account 玩家账号（可选，离线模式下为null）| Player account (optional, null in offline mode)
     * @return 启动命令列表 | Launch command list
     */
    private List<String> buildLaunchCommand(VersionMetadata version, Account account) {
        List<String> commands = new ArrayList<>();
        
        // Java可执行文件路径
        // Java executable path
        commands.add(configManager.getJavaPath().resolve("bin").resolve(getJavaExecutable()).toString());
        
        // 内存设置
        // Memory settings
        commands.add("-Xmx" + configManager.getMaxMemory() + "M");
        commands.add("-Xms" + configManager.getMinMemory() + "M");
        
        // 添加JVM参数
        // Add JVM arguments
        commands.addAll(getJvmArguments());
        
        // 如果是离线模式，添加皮肤支持
        // If in offline mode, add skin support
        if (configManager.isOfflineMode() && skinService.getSelectedSkin() != null) {
            commands.add("-Dminecraft.skin=" + skinService.getSelectedSkin().getFilePath().toString());
        }
        
        // 添加主类
        // Add main class
        commands.add(version.getMainClass());
        
        // 添加游戏参数
        // Add game arguments
        commands.addAll(getGameArguments(version, account));
        
        return commands;
    }
    
    /**
     * 获取Java可执行文件名
     * Get Java executable name
     * 
     * @return Java可执行文件名 | Java executable name
     */
    private String getJavaExecutable() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
    }
    
    /**
     * 获取JVM参数
     * Get JVM arguments
     * 
     * @return JVM参数列表 | JVM argument list
     */
    private List<String> getJvmArguments() {
        // 这里只提供基础参数，可以根据需要扩展
        // Only basic parameters are provided here, can be extended as needed
        return Arrays.asList(
            // 禁用Java更新检查
            // Disable Java update check
            "-Djava.net.preferIPv4Stack=true",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1"
        );
    }
    
    /**
     * 获取游戏参数
     * Get game arguments
     * 
     * @param version 游戏版本 | Game version
     * @param account 玩家账号（可选，离线模式下为null）| Player account (optional, null in offline mode)
     * @return 游戏参数列表 | Game argument list
     */
    private List<String> getGameArguments(VersionMetadata version, Account account) {
        List<String> args = new ArrayList<>();
        
        // 用户名和UUID
        // Username and UUID
        args.add("--username");
        if (configManager.isOfflineMode()) {
            args.add(configManager.getOfflineUsername());
            args.add("--uuid");
            
            // 生成离线UUID
            // Generate offline UUID
            String offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + configManager.getOfflineUsername())
                .getBytes()).toString().replace("-", "");
            args.add(offlineUuid);
            
            // 如果有选择的皮肤，添加皮肤相关参数
            // If there's a selected skin, add skin-related parameters
            Skin selectedSkin = skinService.getSelectedSkin();
            if (selectedSkin != null) {
                args.add("--skinType");
                args.add(selectedSkin.getType());
            }
        } else {
            args.add(account.getUsername());
            args.add("--uuid");
            args.add(account.getUuid().toString());
        }
        
        // 访问令牌（离线模式使用空令牌）
        // Access token (empty token for offline mode)
        args.add("--accessToken");
        args.add(configManager.isOfflineMode() ? "0" : account.getAccessToken());
        
        // 游戏目录
        // Game directory
        args.add("--gameDir");
        args.add(configManager.getMinecraftDirectory().toString());
        
        // 资源目录
        // Assets directory
        args.add("--assetsDir");
        args.add(configManager.getMinecraftDirectory().resolve("assets").toString());
        
        // 资源索引
        // Assets index
        args.add("--assetsIndex");
        args.add(version.getAssetsIndex());
        
        // 版本
        // Version
        args.add("--version");
        args.add(version.getId());
        
        // 离线模式标记
        // Offline mode flag
        if (configManager.isOfflineMode()) {
            args.add("--offline");
        }
        
        return args;
    }
    
    /**
     * 启动进度回调接口
     * Launch progress callback interface
     */
    public interface LaunchProgressCallback {
        /**
         * 进度更新
         * Progress update
         * 
         * @param status 状态信息 | Status information
         * @param progress 进度(0-100) | Progress(0-100)
         */
        void onProgressUpdate(String status, int progress);
    }
} 