package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.utils.ConfigManager;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SystemUtils;
import com.wazixwx.launcher.utils.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * 设置控制器类
 * Settings Controller Class
 * 
 * 处理设置页面的用户交互逻辑
 * Handles user interaction logic for settings page
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SettingsController implements Initializable {
    
    @FXML private Label statusLabel;
    
    // 通用设置
    // General settings
    @FXML private TextField gameDirectoryField;
    @FXML private Button browseGameDirectoryButton;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private CheckBox closeAfterLaunchCheckBox;
    @FXML private CheckBox autoHideLauncherCheckBox;
    @FXML private CheckBox checkUpdatesCheckBox;
    
    // 下载设置
    // Download settings
    @FXML private ComboBox<String> downloadSourceComboBox;
    @FXML private Slider downloadThreadsSlider;
    @FXML private Label downloadThreadsLabel;
    @FXML private CheckBox useProxyCheckBox;
    @FXML private TextField proxyHostField;
    @FXML private TextField proxyPortField;
    
    // 界面设置
    // Interface settings
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider opacitySlider;
    @FXML private Label opacityLabel;
    @FXML private CheckBox enableAnimationsCheckBox;
    @FXML private CheckBox enableBlurEffectsCheckBox;
    
    // 高级设置
    // Advanced settings
    @FXML private ComboBox<String> logLevelComboBox;
    @FXML private Button clearCacheButton;
    @FXML private Label cacheInfoLabel;
    @FXML private Button resetSettingsButton;
    
    // 关于
    // About
    @FXML private Label versionLabel;
    @FXML private Hyperlink websiteLink;
    @FXML private Button checkUpdateButton;
    @FXML private Hyperlink reportIssueLink;
    
    // 底部按钮
    // Bottom buttons
    @FXML private Button applyButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private final ConfigManager configManager;
    private boolean settingsChanged = false;
    
    /**
     * 构造函数
     * Constructor
     */
    public SettingsController() {
        this.configManager = LauncherCore.getInstance().getConfigManager();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeControls();
        setupEventHandlers();
        loadCurrentSettings();
        calculateCacheSize();
    }
    
    /**
     * 初始化控件
     * Initialize controls
     */
    private void initializeControls() {
        // 初始化语言下拉框
        // Initialize language combo box
        languageComboBox.getItems().addAll("简体中文 | Chinese Simplified", "English | 英语");
        
        // 初始化下载源下拉框
        // Initialize download source combo box
        downloadSourceComboBox.getItems().addAll(
            "官方源 | Official Source",
            "BMCLAPI (国内加速) | BMCLAPI (China Accelerated)",
            "MCBBS源 (国内加速) | MCBBS Source (China Accelerated)"
        );
        
        // 初始化主题下拉框
        // Initialize theme combo box
        themeComboBox.getItems().addAll(
            "亮色 | Light", 
            "暗色 | Dark", 
            "跟随系统 | System"
        );
        
        // 初始化日志级别下拉框
        // Initialize log level combo box
        logLevelComboBox.getItems().addAll(
            "DEBUG", 
            "INFO", 
            "WARN", 
            "ERROR"
        );
        
        // 设置下载线程数滑块监听
        // Set download threads slider listener
        downloadThreadsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            downloadThreadsLabel.setText(String.valueOf(value));
            settingsChanged = true;
        });
        
        // 设置不透明度滑块监听
        // Set opacity slider listener
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percent = (int)(newVal.doubleValue() * 100);
            opacityLabel.setText(percent + "%");
            settingsChanged = true;
        });
        
        // 设置版本号
        // Set version number
        versionLabel.setText("版本 | Version: " + LauncherCore.VERSION);
    }
    
    /**
     * 设置事件处理器
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // 游戏目录浏览按钮
        // Game directory browse button
        browseGameDirectoryButton.setOnAction(e -> browseGameDirectory());
        
        // 清理缓存按钮
        // Clear cache button
        clearCacheButton.setOnAction(e -> clearCache());
        
        // 重置设置按钮
        // Reset settings button
        resetSettingsButton.setOnAction(e -> confirmAndResetSettings());
        
        // 官网链接
        // Website link
        websiteLink.setOnAction(e -> openWebsite("https://mc.wazixwx.com"));
        
        // 报告问题链接
        // Report issue link
        reportIssueLink.setOnAction(e -> openWebsite("https://github.com/WaZixwx/WaZi-Minecraft/issues"));
        
        // 检查更新按钮
        // Check update button
        checkUpdateButton.setOnAction(e -> checkForUpdates());
        
        // 应用按钮
        // Apply button
        applyButton.setOnAction(e -> saveSettings(false));
        
        // 保存按钮
        // Save button
        saveButton.setOnAction(e -> saveSettings(true));
        
        // 取消按钮
        // Cancel button
        cancelButton.setOnAction(e -> cancelChanges());
        
        // 监听控件变化
        // Listen to control changes
        setupChangeListeners();
    }
    
    /**
     * 设置变更监听器
     * Setup change listeners
     */
    private void setupChangeListeners() {
        // 为控件添加变更监听器
        // Add change listeners to controls
        gameDirectoryField.textProperty().addListener((obs, old, n) -> settingsChanged = true);
        languageComboBox.valueProperty().addListener((obs, old, n) -> settingsChanged = true);
        closeAfterLaunchCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        autoHideLauncherCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        checkUpdatesCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        downloadSourceComboBox.valueProperty().addListener((obs, old, n) -> settingsChanged = true);
        useProxyCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        proxyHostField.textProperty().addListener((obs, old, n) -> settingsChanged = true);
        proxyPortField.textProperty().addListener((obs, old, n) -> settingsChanged = true);
        themeComboBox.valueProperty().addListener((obs, old, n) -> settingsChanged = true);
        enableAnimationsCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        enableBlurEffectsCheckBox.selectedProperty().addListener((obs, old, n) -> settingsChanged = true);
        logLevelComboBox.valueProperty().addListener((obs, old, n) -> settingsChanged = true);
    }
    
    /**
     * 加载当前设置
     * Load current settings
     */
    private void loadCurrentSettings() {
        // 加载通用设置
        // Load general settings
        gameDirectoryField.setText(configManager.getMinecraftDirectory().toString());
        languageComboBox.setValue(configManager.getLanguage().equals("zh_CN") ? 
                                 "简体中文 | Chinese Simplified" : "English | 英语");
        closeAfterLaunchCheckBox.setSelected(configManager.isCloseAfterLaunch());
        autoHideLauncherCheckBox.setSelected(configManager.isAutoHideEnabled());
        checkUpdatesCheckBox.setSelected(configManager.isCheckUpdatesAtStartup());
        
        // 加载下载设置
        // Load download settings
        String downloadSource = configManager.getDownloadSource();
        switch (downloadSource) {
            case "bmclapi":
                downloadSourceComboBox.setValue("BMCLAPI (国内加速) | BMCLAPI (China Accelerated)");
                break;
            case "mcbbs":
                downloadSourceComboBox.setValue("MCBBS源 (国内加速) | MCBBS Source (China Accelerated)");
                break;
            default:
                downloadSourceComboBox.setValue("官方源 | Official Source");
        }
        
        downloadThreadsSlider.setValue(configManager.getDownloadThreads());
        useProxyCheckBox.setSelected(configManager.isUseProxy());
        proxyHostField.setText(configManager.getProxyHost());
        proxyPortField.setText(String.valueOf(configManager.getProxyPort()));
        
        // 加载界面设置
        // Load interface settings
        String theme = configManager.getTheme();
        switch (theme) {
            case "light":
                themeComboBox.setValue("亮色 | Light");
                break;
            case "dark":
                themeComboBox.setValue("暗色 | Dark");
                break;
            default:
                themeComboBox.setValue("跟随系统 | System");
        }
        
        opacitySlider.setValue(configManager.getLauncherOpacity());
        enableAnimationsCheckBox.setSelected(configManager.isEnableAnimations());
        enableBlurEffectsCheckBox.setSelected(configManager.isEnableBlurEffects());
        
        // 加载高级设置
        // Load advanced settings
        logLevelComboBox.setValue(configManager.getLogLevel().toUpperCase());
        
        // 重置修改标志
        // Reset change flag
        settingsChanged = false;
        updateButtonStates();
    }
    
    /**
     * 保存设置
     * Save settings
     * 
     * @param close 是否关闭窗口 Whether to close the window
     */
    private void saveSettings(boolean close) {
        // 保存通用设置
        // Save general settings
        configManager.setMinecraftDirectory(Paths.get(gameDirectoryField.getText()));
        configManager.setLanguage(languageComboBox.getValue().startsWith("简体中文") ? "zh_CN" : "en_US");
        configManager.setCloseAfterLaunch(closeAfterLaunchCheckBox.isSelected());
        configManager.setAutoHideEnabled(autoHideLauncherCheckBox.isSelected());
        configManager.setCheckUpdatesAtStartup(checkUpdatesCheckBox.isSelected());
        
        // 保存下载设置
        // Save download settings
        String downloadSource = "official";
        if (downloadSourceComboBox.getValue().contains("BMCLAPI")) {
            downloadSource = "bmclapi";
        } else if (downloadSourceComboBox.getValue().contains("MCBBS")) {
            downloadSource = "mcbbs";
        }
        configManager.setDownloadSource(downloadSource);
        
        configManager.setDownloadThreads((int) downloadThreadsSlider.getValue());
        configManager.setUseProxy(useProxyCheckBox.isSelected());
        configManager.setProxyHost(proxyHostField.getText());
        
        try {
            int port = Integer.parseInt(proxyPortField.getText());
            configManager.setProxyPort(port);
        } catch (NumberFormatException e) {
            // 端口格式错误，使用默认端口
            // Port format error, use default port
            configManager.setProxyPort(8080);
            proxyPortField.setText("8080");
        }
        
        // 保存界面设置
        // Save interface settings
        String theme = "system";
        if (themeComboBox.getValue().contains("亮色") || themeComboBox.getValue().contains("Light")) {
            theme = "light";
        } else if (themeComboBox.getValue().contains("暗色") || themeComboBox.getValue().contains("Dark")) {
            theme = "dark";
        }
        configManager.setTheme(theme);
        
        configManager.setLauncherOpacity(opacitySlider.getValue());
        configManager.setEnableAnimations(enableAnimationsCheckBox.isSelected());
        configManager.setEnableBlurEffects(enableBlurEffectsCheckBox.isSelected());
        
        // 保存高级设置
        // Save advanced settings
        configManager.setLogLevel(logLevelComboBox.getValue().toLowerCase());
        
        // 将设置写入配置文件
        // Write settings to config file
        configManager.saveConfig();
        
        // 显示保存成功消息
        // Show save success message
        statusLabel.setText("设置已保存 | Settings saved");
        AnimationUtils.shakeNode(statusLabel, 5, 3);
        
        // 重置修改标志
        // Reset change flag
        settingsChanged = false;
        updateButtonStates();
        
        // 如果需要关闭窗口，则延迟关闭
        // If need to close window, delay closing
        if (close) {
            // 延迟关闭窗口，以便用户看到保存成功的消息
            // Delay closing the window to let user see the success message
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    /**
     * 取消更改
     * Cancel changes
     */
    private void cancelChanges() {
        if (settingsChanged) {
            // 显示确认对话框
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认 | Confirmation");
            alert.setHeaderText("放弃更改 | Discard Changes");
            alert.setContentText("您有未保存的更改，确定要放弃吗？ | You have unsaved changes, are you sure you want to discard them?");
            
            ButtonType discardButton = new ButtonType("放弃更改 | Discard Changes");
            ButtonType saveButton = new ButtonType("保存更改 | Save Changes");
            ButtonType cancelButton = new ButtonType("取消 | Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(discardButton, saveButton, cancelButton);
            
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == discardButton) {
                    // 关闭窗口
                    // Close window
                    Stage stage = (Stage) this.cancelButton.getScene().getWindow();
                    stage.close();
                } else if (buttonType == saveButton) {
                    // 保存设置并关闭窗口
                    // Save settings and close window
                    saveSettings(true);
                }
                // 如果是取消按钮，则不做任何操作
                // If cancel button, do nothing
            });
        } else {
            // 没有更改，直接关闭窗口
            // No changes, directly close window
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }
    
    /**
     * 浏览游戏目录
     * Browse game directory
     */
    private void browseGameDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择游戏目录 | Select Game Directory");
        
        // 设置初始目录
        // Set initial directory
        File currentDirectory = new File(gameDirectoryField.getText());
        if (currentDirectory.exists() && currentDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(currentDirectory);
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        // 显示目录选择器
        // Show directory chooser
        File selectedDirectory = directoryChooser.showDialog(gameDirectoryField.getScene().getWindow());
        if (selectedDirectory != null) {
            gameDirectoryField.setText(selectedDirectory.getAbsolutePath());
            settingsChanged = true;
            updateButtonStates();
        }
    }
    
    /**
     * 清理缓存
     * Clear cache
     */
    private void clearCache() {
        // 显示确认对话框
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认 | Confirmation");
        alert.setHeaderText("清理缓存 | Clear Cache");
        alert.setContentText("确定要清理启动器缓存吗？这不会影响您的游戏数据。 | Are you sure you want to clear launcher cache? This will not affect your game data.");
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // 执行清理操作
                // Perform cleaning operation
                statusLabel.setText("正在清理缓存... | Cleaning cache...");
                
                // 异步清理缓存
                // Asynchronously clean cache
                CompletableFuture.runAsync(() -> {
                    try {
                        Path cacheDir = Paths.get(configManager.getLauncherDirectory().toString(), "cache");
                        if (Files.exists(cacheDir)) {
                            // 递归删除缓存目录中的所有文件
                            // Recursively delete all files in cache directory
                            Files.walk(cacheDir)
                                .sorted(java.util.Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                            
                            // 重新创建缓存目录
                            // Recreate cache directory
                            Files.createDirectories(cacheDir);
                        }
                        
                        // 更新界面
                        // Update UI
                        Platform.runLater(() -> {
                            statusLabel.setText("缓存已清理 | Cache cleared");
                            calculateCacheSize();
                            AnimationUtils.shakeNode(statusLabel, 5, 3);
                        });
                    } catch (IOException e) {
                        LogUtils.error("清理缓存失败 | Failed to clear cache", e);
                        Platform.runLater(() -> {
                            statusLabel.setText("清理缓存失败 | Cache clearing failed");
                            AnimationUtils.shakeNode(statusLabel, 5, 3);
                            
                            // 显示错误对话框
                            // Show error dialog
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("错误 | Error");
                            errorAlert.setHeaderText("清理缓存失败 | Cache Clearing Failed");
                            errorAlert.setContentText("错误信息: " + e.getMessage() + "\nError message: " + e.getMessage());
                            errorAlert.showAndWait();
                        });
                    }
                });
            }
        });
    }
    
    /**
     * 确认并重置设置
     * Confirm and reset settings
     */
    private void confirmAndResetSettings() {
        // 显示确认对话框
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("警告 | Warning");
        alert.setHeaderText("重置设置 | Reset Settings");
        alert.setContentText("确定要将所有设置恢复为默认值吗？此操作不可撤销。 | Are you sure you want to reset all settings to default? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // 执行重置操作
                // Perform reset operation
                configManager.resetToDefault();
                
                // 重新加载设置
                // Reload settings
                loadCurrentSettings();
                
                // 更新界面
                // Update UI
                statusLabel.setText("设置已重置 | Settings reset to default");
                AnimationUtils.shakeNode(statusLabel, 5, 3);
            }
        });
    }
    
    /**
     * 计算缓存大小
     * Calculate cache size
     */
    private void calculateCacheSize() {
        // 异步计算缓存大小
        // Asynchronously calculate cache size
        CompletableFuture.supplyAsync(() -> {
            try {
                Path cacheDir = Paths.get(configManager.getLauncherDirectory().toString(), "cache");
                if (!Files.exists(cacheDir)) {
                    return 0L;
                }
                
                // 计算目录大小
                // Calculate directory size
                return Files.walk(cacheDir)
                    .filter(p -> Files.isRegularFile(p))
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
            } catch (IOException e) {
                LogUtils.error("计算缓存大小失败 | Failed to calculate cache size", e);
                return 0L;
            }
        }).thenAccept(size -> {
            // 更新界面
            // Update UI
            Platform.runLater(() -> {
                // 转换为MB
                // Convert to MB
                double sizeInMB = size / (1024.0 * 1024.0);
                cacheInfoLabel.setText(String.format("当前缓存大小: %.2f MB | Current cache size: %.2f MB", sizeInMB, sizeInMB));
            });
        });
    }
    
    /**
     * 检查更新
     * Check for updates
     */
    private void checkForUpdates() {
        // 设置检查中状态
        // Set checking status
        checkUpdateButton.setDisable(true);
        statusLabel.setText("正在检查更新... | Checking for updates...");
        
        // 异步检查更新
        // Asynchronously check for updates
        CompletableFuture.supplyAsync(() -> {
            // 这里应该实现实际的更新检查逻辑
            // Actual update checking logic should be implemented here
            
            // 暂时模拟一个检查过程
            // Temporarily simulate a checking process
            try {
                Thread.sleep(1500);
                return false; // 模拟没有更新 Simulate no update
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }).thenAccept(hasUpdate -> {
            // 更新界面
            // Update UI
            Platform.runLater(() -> {
                checkUpdateButton.setDisable(false);
                
                if (hasUpdate) {
                    statusLabel.setText("发现新版本 | New version available");
                    
                    // 显示更新对话框
                    // Show update dialog
                    Alert updateAlert = new Alert(Alert.AlertType.INFORMATION);
                    updateAlert.setTitle("更新 | Update");
                    updateAlert.setHeaderText("发现新版本 | New Version Available");
                    updateAlert.setContentText("有新版本可用，请前往官网下载。 | A new version is available. Please visit the official website to download.");
                    
                    ButtonType websiteButton = new ButtonType("前往官网 | Visit Website");
                    ButtonType cancelButton = new ButtonType("稍后 | Later", ButtonBar.ButtonData.CANCEL_CLOSE);
                    
                    updateAlert.getButtonTypes().setAll(websiteButton, cancelButton);
                    
                    updateAlert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType == websiteButton) {
                            openWebsite("https://mc.wazixwx.com");
                        }
                    });
                } else {
                    statusLabel.setText("当前已是最新版本 | You are using the latest version");
                    AnimationUtils.shakeNode(statusLabel, 5, 3);
                }
            });
        }).exceptionally(e -> {
            // 处理异常
            // Handle exception
            Platform.runLater(() -> {
                checkUpdateButton.setDisable(false);
                statusLabel.setText("检查更新失败 | Failed to check for updates");
                AnimationUtils.shakeNode(statusLabel, 5, 3);
                
                // 显示错误对话框
                // Show error dialog
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("错误 | Error");
                errorAlert.setHeaderText("检查更新失败 | Update Check Failed");
                errorAlert.setContentText("错误信息: " + e.getMessage() + "\nError message: " + e.getMessage());
                errorAlert.showAndWait();
            });
            return null;
        });
    }
    
    /**
     * 打开网页
     * Open website
     * 
     * @param url 网址 Website URL
     */
    private void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            LogUtils.error("打开网页失败 | Failed to open website", e);
            
            // 显示错误对话框
            // Show error dialog
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("错误 | Error");
            errorAlert.setHeaderText("打开网页失败 | Open Website Failed");
            errorAlert.setContentText("无法打开网页: " + url + "\nCannot open website: " + url);
            errorAlert.showAndWait();
        }
    }
    
    /**
     * 更新按钮状态
     * Update button states
     */
    private void updateButtonStates() {
        applyButton.setDisable(!settingsChanged);
        saveButton.setDisable(!settingsChanged);
    }
} 