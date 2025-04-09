package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.core.ConfigurationManager;
import com.wazixwx.launcher.core.GameLauncher;
import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.core.VersionManager;
import com.wazixwx.launcher.model.Version;
import com.wazixwx.launcher.utils.AnimationUtils;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SystemUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 游戏启动页面控制器
 * Game Launch Page Controller
 * 
 * @author WaZixwx
 */
public class PlayController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private ComboBox<String> versionComboBox;
    @FXML private Slider memorySlider;
    @FXML private Label memoryValueLabel;
    @FXML private CheckBox autoMemoryCheckBox;
    @FXML private Label systemMemoryLabel;
    @FXML private ProgressBar memoryUsageBar;
    @FXML private Label memoryPercentLabel;
    @FXML private TextField javaPathField;
    @FXML private TextField jvmArgsField;
    @FXML private TextField gameWidthField;
    @FXML private TextField gameHeightField;
    @FXML private ComboBox<String> resolutionComboBox;
    @FXML private CheckBox fullscreenCheckbox;
    @FXML private RadioButton onlineModeRadio;
    @FXML private RadioButton offlineModeRadio;
    @FXML private ToggleGroup gameModeGroup;
    @FXML private TextField offlineUsernameField;
    @FXML private Button launchButton;
    @FXML private TextArea launchLogArea;
    @FXML private ProgressBar launchProgressBar;
    @FXML private Label progressLabel;
    @FXML private VBox contentContainer;

    private VersionManager versionManager;
    private ConfigurationManager configManager;
    private Version selectedVersion;
    
    // 系统总内存和推荐内存
    // System total memory and recommended memory
    private long totalSystemMemory;
    private long recommendedMemory;

    /**
     * 初始化控制器
     * Initialize the controller
     * 
     * @param location 位置 | Location
     * @param resources 资源 | Resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LogUtils.info("初始化游戏启动页面 | Initializing game launch page");
        
        // 初始化版本管理器
        // Initialize version manager
        versionManager = LauncherCore.getInstance().getVersionManager();
        
        // 获取系统内存信息
        // Get system memory information
        initializeMemoryInfo();
        
        // 初始化UI
        // Initialize UI
        initializeUI();
        
        // 加载配置
        // Load configurations
        loadConfigurations();
        
        // 加载版本列表
        // Load version list
        loadVersionList();
    }
    
    /**
     * 初始化内存信息
     * Initialize memory information
     */
    private void initializeMemoryInfo() {
        // 获取系统总内存（MB）
        // Get total system memory (MB)
        totalSystemMemory = SystemUtils.getTotalMemory();
        
        // 计算推荐内存（系统总内存的1/4，至少1GB，最多4GB）
        // Calculate recommended memory (1/4 of total system memory, at least 1GB, at most 4GB)
        recommendedMemory = Math.max(1024, Math.min(4096, totalSystemMemory / 4));
        
        // 如果无法获取系统内存，使用默认值2GB
        // If system memory cannot be obtained, use default value 2GB
        if (totalSystemMemory <= 0) {
            totalSystemMemory = 8192;
            recommendedMemory = 2048;
        }
        
        LogUtils.info("系统总内存: " + totalSystemMemory + "MB | System total memory: " + totalSystemMemory + "MB");
        LogUtils.info("推荐内存: " + recommendedMemory + "MB | Recommended memory: " + recommendedMemory + "MB");
    }

    /**
     * 初始化UI
     * Initialize UI
     */
    private void initializeUI() {
        // 设置标题
        // Set title
        titleLabel.setText("游戏启动 | Game Launch");
        
        // 设置状态标签
        // Set status label
        statusLabel.setText("就绪 | Ready");
        
        // 设置系统内存信息
        // Set system memory information
        systemMemoryLabel.setText(String.format("系统内存：%.1f GB | System Memory: %.1f GB", totalSystemMemory / 1024.0));
        
        // 设置内存滑块最大值为系统内存的3/4，最多8GB
        // Set memory slider maximum value to 3/4 of system memory, at most 8GB
        double maxMemoryValue = Math.min(8192, Math.round(totalSystemMemory * 0.75 / 1024) * 1024);
        memorySlider.setMax(maxMemoryValue);
        
        // 设置滑块初始值为推荐内存
        // Set slider initial value to recommended memory
        memorySlider.setValue(recommendedMemory);
        updateMemoryLabel();
        
        // 设置内存滑块监听器
        // Set memory slider listener
        memorySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // 四舍五入到最接近的128MB
            // Round to nearest 128MB
            int roundedValue = (int) (Math.round(newValue.doubleValue() / 128) * 128);
            memorySlider.setValue(roundedValue);
            updateMemoryLabel();
        });
        
        // 设置自适应内存复选框监听器
        // Set auto memory checkbox listener
        autoMemoryCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // 如果选中，设置为推荐内存
                // If selected, set to recommended memory
                memorySlider.setValue(recommendedMemory);
                memorySlider.setDisable(true);
                updateMemoryLabel();
            } else {
                // 如果未选中，启用滑块
                // If not selected, enable slider
                memorySlider.setDisable(false);
            }
        });
        
        // 设置分辨率下拉框选项
        // Set resolution combobox options
        ObservableList<String> resolutions = FXCollections.observableArrayList(
            "1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160"
        );
        resolutionComboBox.setItems(resolutions);
        resolutionComboBox.setValue("1280x720");
        
        // 设置全屏复选框监听器
        // Set fullscreen checkbox listener
        fullscreenCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            resolutionComboBox.setDisable(newValue);
        });
        
        // 设置游戏模式单选按钮监听器
        // Set game mode radio button listener
        offlineModeRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            offlineUsernameField.setDisable(!newValue);
        });
        
        onlineModeRadio.setSelected(true);
        offlineUsernameField.setDisable(true);
        
        // 设置版本下拉框选择监听器
        // Set version combobox selection listener
        versionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                loadVersionInfo(newValue);
            }
        });
        
        // 设置刷新按钮点击事件
        // Set refresh button click event
        refreshButton.setOnAction(e -> refreshVersions());
        
        // 设置启动按钮点击事件
        // Set launch button click event
        launchButton.setOnAction(e -> launchGame());
    }
    
    /**
     * 更新内存标签和进度条
     * Update memory label and progress bar
     */
    private void updateMemoryLabel() {
        // 获取当前选择的内存值
        // Get current selected memory value
        int memoryValue = (int) memorySlider.getValue();
        
        // 更新内存值标签
        // Update memory value label
        double memoryGB = Math.round(memoryValue / 102.4) / 10.0;
        memoryValueLabel.setText(memoryGB + " GB");
        
        // 计算内存占比
        // Calculate memory percentage
        double percentage = (double) memoryValue / totalSystemMemory;
        
        // 更新内存使用进度条
        // Update memory usage progress bar
        memoryUsageBar.setProgress(percentage);
        
        // 更新百分比标签
        // Update percentage label
        int percentInt = (int) (percentage * 100);
        memoryPercentLabel.setText(percentInt + "%");
        
        // 设置进度条颜色提示（绿色、黄色、红色）
        // Set progress bar color hint (green, yellow, red)
        if (percentage < 0.5) {
            memoryUsageBar.setStyle("-fx-accent: #44bd32;"); // 绿色 | Green
        } else if (percentage < 0.75) {
            memoryUsageBar.setStyle("-fx-accent: #e1b12c;"); // 黄色 | Yellow
        } else {
            memoryUsageBar.setStyle("-fx-accent: #c23616;"); // 红色 | Red
        }
    }

    /**
     * 加载配置
     * Load configurations
     */
    private void loadConfigurations() {
        // 获取配置管理器实例
        // Get configuration manager instance
        configManager = LauncherCore.getInstance().getConfigManager();
        
        // 加载最大内存设置
        // Load maximum memory setting
        int configMemory = configManager.getMaxMemory();
        if (configMemory > 0) {
            // 确保内存值在滑块范围内
            // Ensure memory value is within slider range
            configMemory = Math.max((int)memorySlider.getMin(), Math.min((int)memorySlider.getMax(), configMemory));
            memorySlider.setValue(configMemory);
        }
        
        // 加载自适应内存设置
        // Load auto memory setting
        boolean autoMemory = (Boolean) configManager.get("game.auto_memory", false);
        autoMemoryCheckBox.setSelected(autoMemory);
        memorySlider.setDisable(autoMemory);
        
        // 更新内存标签
        // Update memory label
        updateMemoryLabel();
        
        // 加载Java路径设置
        // Load Java path setting
        javaPathField.setText(configManager.getJavaPath().toString());
        
        // 加载自定义JVM参数
        // Load custom JVM arguments
        String customJvmArgs = (String) configManager.get("game.custom_jvm_args");
        if (customJvmArgs != null) {
            jvmArgsField.setText(customJvmArgs);
        }
        
        // 加载分辨率设置
        // Load resolution setting
        String resolution = (String) configManager.get("game.resolution", "1280x720");
        resolutionComboBox.setValue(resolution);
        
        // 加载全屏设置
        // Load fullscreen setting
        boolean fullscreen = (Boolean) configManager.get("game.fullscreen", false);
        fullscreenCheckbox.setSelected(fullscreen);
        resolutionComboBox.setDisable(fullscreen);
        
        // 加载游戏模式设置
        // Load game mode setting
        boolean offlineMode = configManager.isOfflineMode();
        if (offlineMode) {
            offlineModeRadio.setSelected(true);
        } else {
            onlineModeRadio.setSelected(true);
        }
        
        // 加载离线用户名
        // Load offline username
        offlineUsernameField.setText(configManager.getOfflineUsername());
        offlineUsernameField.setDisable(!offlineMode);
    }

    /**
     * 加载版本列表
     * Load version list
     */
    @FXML
    private void loadVersionList() {
        statusLabel.setText("正在加载版本列表... | Loading version list...");
        launchProgressBar.setVisible(true);
        progressLabel.setVisible(true);
        launchProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        
        // 禁用刷新按钮
        // Disable refresh button
        refreshButton.setDisable(true);
        
        versionManager.getVersions()
            .thenAccept(versions -> {
                Platform.runLater(() -> {
                    // 加载版本到下拉框
                    // Load versions to combo box
                    List<String> versionIds = versions.stream()
                            .map(Version::getId)
                            .collect(Collectors.toList());
                    
                    ObservableList<String> observableVersions = FXCollections.observableArrayList(versionIds);
                    versionComboBox.setItems(observableVersions);
                    
                    // 选择上次使用的版本或第一个
                    // Select last used version or first one
                    String lastVersion = (String) configManager.get("game.last_version");
                    if (lastVersion != null && versionIds.contains(lastVersion)) {
                        versionComboBox.setValue(lastVersion);
                    } else if (!versionIds.isEmpty()) {
                        versionComboBox.setValue(versionIds.get(0));
                    }
                    
                    // 更新状态
                    // Update status
                    statusLabel.setText("版本列表已加载 | Version list loaded");
                    launchProgressBar.setProgress(1.0);
                    
                    // 启用刷新按钮
                    // Enable refresh button
                    refreshButton.setDisable(false);
                    
                    // 应用淡入动画
                    // Apply fade-in animation
                    AnimationUtils.fadeIn(contentContainer, 300);
                    
                    // 延迟隐藏进度条
                    // Delay hiding progress bar
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Platform.runLater(() -> {
                                launchProgressBar.setVisible(false);
                                progressLabel.setVisible(false);
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            })
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    LogUtils.error("加载版本列表失败 | Failed to load version list", e);
                    statusLabel.setText("加载版本列表失败 | Failed to load version list");
                    launchProgressBar.setProgress(0);
                    launchProgressBar.setVisible(false);
                    progressLabel.setVisible(false);
                    refreshButton.setDisable(false);
                    
                    showAlert(Alert.AlertType.ERROR, "错误 | Error", 
                            "加载版本列表失败 | Failed to load version list", 
                            e.getMessage());
                });
                return null;
            });
    }

    /**
     * 加载版本信息
     * Load version information
     * 
     * @param versionId 版本ID | Version ID
     */
    private void loadVersionInfo(String versionId) {
        statusLabel.setText("正在加载版本信息... | Loading version information...");
        
        versionManager.getVersionById(versionId)
            .thenAccept(version -> {
                Platform.runLater(() -> {
                    selectedVersion = version;
                    
                    // 显示版本信息
                    // Display version information
                    StringBuilder info = new StringBuilder();
                    info.append("版本ID | Version ID: ").append(version.getId()).append("\n");
                    info.append("类型 | Type: ").append(formatType(version.getType())).append("\n");
                    info.append("发布时间 | Release Time: ").append(version.getReleaseTime()).append("\n");
                    info.append("主版本 | Main Version: ").append(version.getMainVersion()).append("\n");
                    info.append("资源索引 | Assets Index: ").append(version.getAssetsIndex()).append("\n");
                    info.append("已安装 | Installed: ").append(version.isInstalled() ? "是 | Yes" : "否 | No").append("\n");
                    
                    launchLogArea.setText(info.toString());
                    
                    // 更新启动按钮状态
                    // Update launch button state
                    launchButton.setDisable(!version.isInstalled());
                    
                    // 更新状态
                    // Update status
                    statusLabel.setText("就绪 | Ready");
                });
            })
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    LogUtils.error("加载版本信息失败 | Failed to load version information", e);
                    statusLabel.setText("加载版本信息失败 | Failed to load version information");
                    
                    showAlert(Alert.AlertType.ERROR, "错误 | Error", 
                            "加载版本信息失败 | Failed to load version information", 
                            e.getMessage());
                });
                return null;
            });
    }

    /**
     * 格式化版本类型
     * Format version type
     * 
     * @param type 版本类型 | Version type
     * @return 格式化后的类型 | Formatted type
     */
    private String formatType(String type) {
        if (type == null) {
            return "未知 | Unknown";
        }
        
        switch (type.toLowerCase()) {
            case "release":
                return "正式版 | Release";
            case "snapshot":
                return "快照版 | Snapshot";
            case "old_beta":
                return "旧Beta版 | Old Beta";
            case "old_alpha":
                return "旧Alpha版 | Old Alpha";
            default:
                return type;
        }
    }

    /**
     * 启动游戏
     * Launch game
     */
    @FXML
    private void launchGame() {
        if (selectedVersion == null) {
            showAlert(Alert.AlertType.WARNING, "警告 | Warning", 
                    "无法启动游戏 | Cannot launch the game", 
                    "请先选择一个版本 | Please select a version first");
            return;
        }
        
        // 保存当前配置
        // Save current configurations
        saveConfigurations();
        
        // 保存最后启动的版本
        // Save last launched version
        configManager.set("game.last_version", selectedVersion.getId());
        configManager.save();
        
        // 更新界面状态
        // Update UI state
        launchButton.setDisable(true);
        statusLabel.setText("正在启动游戏... | Launching the game...");
        launchProgressBar.setVisible(true);
        progressLabel.setVisible(true);
        launchProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        
        // 清空日志区域
        // Clear log area
        launchLogArea.clear();
        appendLog("正在准备启动游戏... | Preparing to launch the game...");
        appendLog("版本: " + selectedVersion.getId() + " | Version: " + selectedVersion.getId());
        
        // 显示内存分配信息
        // Show memory allocation information
        int memoryValue = (int) memorySlider.getValue();
        appendLog("内存分配: " + memoryValue + "MB | Memory allocation: " + memoryValue + "MB");
        
        // 创建游戏启动器实例
        // Create game launcher instance
        GameLauncher gameLauncher = new GameLauncher();
        
        // 启动游戏
        // Launch the game
        gameLauncher.launchGame(selectedVersion.getId())
            .thenAccept(process -> {
                Platform.runLater(() -> {
                    statusLabel.setText("游戏已启动 | Game launched");
                    appendLog("游戏已成功启动 | Game successfully launched");
                    launchProgressBar.setProgress(1.0);
                    launchButton.setDisable(false);
                    
                    // 根据用户设置决定是否隐藏启动器窗口
                    // Hide launcher window based on user settings
                    if (configManager.isAutoHideEnabled()) {
                        appendLog("正在隐藏启动器... | Hiding launcher...");
                        LauncherCore.getInstance().hideWindow();
                    } else {
                        appendLog("游戏已在后台运行 | Game is running in background");
                    }
                    
                    // 启动游戏监控线程
                    // Start game monitor thread
                    monitorGameProcess(process);
                });
            })
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    LogUtils.error("启动游戏失败 | Failed to launch the game", e);
                    statusLabel.setText("启动失败 | Launch failed");
                    appendLog("错误: " + e.getMessage() + " | Error: " + e.getMessage());
                    launchProgressBar.setProgress(0);
                    launchProgressBar.setVisible(false);
                    progressLabel.setVisible(false);
                    launchButton.setDisable(false);
                    
                    showAlert(Alert.AlertType.ERROR, "启动失败 | Launch Failed", 
                            "启动游戏失败 | Failed to launch the game", 
                            e.getMessage());
                });
                return null;
            });
    }

    /**
     * 监控游戏进程
     * Monitor game process
     * 
     * @param process 游戏进程 | Game process
     */
    private void monitorGameProcess(Process process) {
        Thread monitorThread = new Thread(() -> {
            try {
                // 等待游戏进程结束
                // Wait for the game process to end
                int exitCode = process.waitFor();
                
                Platform.runLater(() -> {
                    LogUtils.info("游戏已退出，退出码：" + exitCode + " | Game exited with code: " + exitCode);
                    statusLabel.setText("游戏已退出 | Game exited");
                    appendLog("游戏已退出，退出码：" + exitCode + " | Game exited with code: " + exitCode);
                    launchProgressBar.setProgress(0);
                    launchProgressBar.setVisible(false);
                    progressLabel.setVisible(false);
                    
                    // 如果启动器被隐藏，则显示启动器窗口
                    // Show launcher window if it was hidden
                    if (configManager.isAutoHideEnabled()) {
                        appendLog("正在显示启动器... | Showing launcher...");
                        LauncherCore.getInstance().showWindow();
                    }
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    LogUtils.error("监控游戏进程时出错 | Error when monitoring game process", e);
                    appendLog("监控游戏进程时出错: " + e.getMessage() + " | Error when monitoring game process: " + e.getMessage());
                    
                    // 出错时也要显示启动器窗口（如果它被隐藏）
                    // Show launcher window even when error occurs (if it was hidden)
                    if (configManager.isAutoHideEnabled()) {
                        LauncherCore.getInstance().showWindow();
                    }
                });
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 保存配置
     * Save configurations
     */
    private void saveConfigurations() {
        try {
            // 保存最大内存设置
            // Save maximum memory setting
            int maxMemory = (int) memorySlider.getValue();
            configManager.setMaxMemory(maxMemory);
            
            // 保存自适应内存设置
            // Save auto memory setting
            configManager.set("game.auto_memory", autoMemoryCheckBox.isSelected());
            
            // 保存Java路径设置
            // Save Java path setting
            configManager.setJavaPath(Paths.get(javaPathField.getText()));
            
            // 保存自定义JVM参数
            // Save custom JVM arguments
            configManager.set("game.custom_jvm_args", jvmArgsField.getText());
            
            // 保存全屏设置
            // Save fullscreen setting
            configManager.set("game.fullscreen", fullscreenCheckbox.isSelected());
            
            // 保存分辨率设置
            // Save resolution setting
            configManager.set("game.resolution", resolutionComboBox.getValue());
            
            // 保存离线模式设置
            // Save offline mode settings
            configManager.setOfflineMode(offlineModeRadio.isSelected());
            if (offlineModeRadio.isSelected()) {
                configManager.set("game.offline_username", offlineUsernameField.getText());
            }
            
            // 保存所有配置
            // Save all configurations
            configManager.save();
            
            LogUtils.info("配置已保存 | Configurations saved");
        } catch (NumberFormatException e) {
            LogUtils.error("保存配置失败：格式错误 | Failed to save configurations: format error", e);
            showAlert(Alert.AlertType.ERROR, "错误 | Error", 
                    "保存配置失败 | Failed to save configurations", 
                    "请检查数值格式是否正确 | Please check if the numeric formats are correct");
        } catch (Exception e) {
            LogUtils.error("保存配置失败 | Failed to save configurations", e);
            showAlert(Alert.AlertType.ERROR, "错误 | Error", 
                    "保存配置失败 | Failed to save configurations", 
                    e.getMessage());
        }
    }

    /**
     * 显示警告对话框
     * Show alert dialog
     * 
     * @param type 警告类型 | Alert type
     * @param title 标题 | Title
     * @param header 头信息 | Header
     * @param content 内容 | Content
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 添加日志
     * Append log
     * 
     * @param message 日志信息 | Log message
     */
    private void appendLog(String message) {
        Platform.runLater(() -> {
            launchLogArea.appendText(message + "\n");
            launchLogArea.setScrollTop(Double.MAX_VALUE); // 自动滚动到底部 | Auto-scroll to bottom
        });
    }
    
    /**
     * 刷新版本
     * Refresh versions
     */
    @FXML
    private void refreshVersions() {
        loadVersionList();
    }
    
    /**
     * 选择Java路径
     * Select Java path
     */
    @FXML
    private void selectJavaPath() {
        // TODO：实现Java路径选择
        // TODO: Implement Java path selection
    }
} 