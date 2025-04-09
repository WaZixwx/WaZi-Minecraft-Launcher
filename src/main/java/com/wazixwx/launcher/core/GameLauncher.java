package com.wazixwx.launcher.core;

import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.model.Skin;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.service.SkinService;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

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
     * @param version 版本元数据 | Version metadata
     * @param account 用户账号 | User account
     * @return 游戏进程的CompletableFuture | CompletableFuture of the game process
     */
    public CompletableFuture<Process> launch(VersionMetadata version, Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 记录游戏启动信息
                // Log game launch information
                LogUtils.info("正在启动Minecraft | Launching Minecraft: " + version.getId());
                
                // 如果是离线模式，准备皮肤
                // If in offline mode, prepare skin
                if (LauncherCore.getInstance().getConfigManager().isOfflineMode()) {
                    prepareSkin();
                }
                
                // 构建启动命令
                // Build launch command
                List<String> command = buildLaunchCommand(version, account);
                LogUtils.info("启动命令 | Launch command: " + String.join(" ", command));
                
                // 启动游戏进程
                // Start game process
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(version.getGameDirectory().toFile());
                Process process = processBuilder.start();
                
                // 记录游戏日志
                // Log game output
                logGameOutput(process);
                
                // 监控游戏进程状态
                // Monitor game process status
                monitorGameProcess(process);
                
                // 检查是否需要自动隐藏启动器
                // Check if launcher should be auto-hidden
                if (LauncherCore.getInstance().getConfigManager().isAutoHideEnabled()) {
                    LogUtils.info("自动隐藏启动器 | Auto-hiding launcher");
                    LauncherCore.getInstance().hideWindow();
                }
                
                // 检查是否需要关闭启动器
                // Check if launcher should be closed
                if (LauncherCore.getInstance().getConfigManager().isCloseAfterLaunch()) {
                    LogUtils.info("游戏启动后关闭启动器 | Closing launcher after game launch");
                    Platform.runLater(() -> {
                        // 延迟一秒后关闭，确保游戏已经正常启动
                        // Delay 1 second to ensure game has started properly
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.exit();
                    });
                }
                
                return process;
                
            } catch (Exception e) {
                LogUtils.error("启动游戏失败 | Failed to launch game", e);
                throw new RuntimeException(e);
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
    
    /**
     * 启动指定版本的游戏
     * Launch game with specified version
     * 
     * @param versionId 版本ID | Version ID
     * @return CompletableFuture<Process> 游戏进程 | Game process
     */
    public CompletableFuture<Process> launchGame(String versionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LogUtils.info("准备启动游戏: " + versionId + " | Preparing to launch game: " + versionId);
                
                // 获取版本信息
                // Get version information
                VersionMetadata version = LauncherCore.getInstance().getVersionManager()
                        .getAllVersions().get()
                        .stream()
                        .filter(v -> v.getId().equals(versionId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("找不到版本: " + versionId + " | Version not found: " + versionId));
                
                // 获取账号信息（如果是在线模式）
                // Get account information (if in online mode)
                Account account = null;
                if (!configManager.isOfflineMode()) {
                    account = LauncherCore.getInstance().getAccountService().getSelectedAccount();
                    if (account == null) {
                        throw new RuntimeException("在线模式下未找到选中的账号 | No selected account found in online mode");
                    }
                }
                
                // 调用launch方法启动游戏
                // Call launch method to start the game
                return launch(version, account).get();
            } catch (Exception e) {
                LogUtils.error("启动游戏失败: " + versionId + " | Failed to launch game: " + versionId, e);
                throw new RuntimeException("启动游戏失败 | Failed to launch game", e);
            }
        });
    }
    
    /**
     * 记录游戏输出
     * Log game output
     * 
     * @param process 游戏进程 | Game process
     */
    private void logGameOutput(Process process) {
        // 创建线程处理游戏输出
        // Create thread to handle game output
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LogUtils.debug("游戏输出 | Game output: " + line);
                }
            } catch (IOException e) {
                LogUtils.error("读取游戏输出失败 | Failed to read game output", e);
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
        
        // 创建线程处理游戏错误输出
        // Create thread to handle game error output
        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LogUtils.error("游戏错误输出 | Game error output: " + line);
                }
            } catch (IOException e) {
                LogUtils.error("读取游戏错误输出失败 | Failed to read game error output", e);
            }
        });
        errorThread.setDaemon(true);
        errorThread.start();
    }
    
    /**
     * 监控游戏进程
     * Monitor game process
     * 
     * @param process 游戏进程 | Game process
     */
    private void monitorGameProcess(Process process) {
        // 创建线程监控游戏进程
        // Create thread to monitor game process
        Thread monitorThread = new Thread(() -> {
            try {
                // 等待游戏进程结束
                // Wait for game process to end
                int exitCode = process.waitFor();
                LogUtils.info("游戏已结束，退出代码 | Game ended, exit code: " + exitCode);
                
                // 如果启动器处于自动隐藏状态，则重新显示启动器
                // If launcher is auto-hidden, show it again
                if (LauncherCore.getInstance().getConfigManager().isAutoHideEnabled() && 
                    !LauncherCore.getInstance().getConfigManager().isCloseAfterLaunch()) {
                    LogUtils.info("游戏结束，重新显示启动器 | Game ended, showing launcher again");
                    Platform.runLater(() -> {
                        LauncherCore.getInstance().showWindow();
                    });
                }
            } catch (InterruptedException e) {
                LogUtils.error("监控游戏进程被中断 | Monitoring game process was interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
} 