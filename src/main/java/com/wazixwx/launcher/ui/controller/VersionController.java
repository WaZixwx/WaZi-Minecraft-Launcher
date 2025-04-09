package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.core.GameLauncher;
import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.model.VersionDetail;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.service.VersionManager;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SystemUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * 版本控制器类
 * Version Controller Class
 * 
 * 处理版本管理界面的用户交互逻辑
 * Handles user interaction logic for version management interface
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class VersionController implements Initializable {
    
    @FXML
    private TableView<VersionMetadata> versionTable;
    
    @FXML
    private TableColumn<VersionMetadata, String> versionIdColumn;
    
    @FXML
    private TableColumn<VersionMetadata, String> versionTypeColumn;
    
    @FXML
    private TableColumn<VersionMetadata, String> versionReleaseDateColumn;
    
    @FXML
    private TableColumn<VersionMetadata, Boolean> versionInstalledColumn;
    
    @FXML
    private ComboBox<String> versionTypeFilter;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private CheckBox showInstalledOnlyCheckbox;
    
    @FXML
    private Label versionTitleLabel;
    
    @FXML
    private Label versionIdLabel;
    
    @FXML
    private Label versionTypeLabel;
    
    @FXML
    private Label versionReleaseDateLabel;
    
    @FXML
    private Label versionJavaLabel;
    
    @FXML
    private Label versionStatusLabel;
    
    @FXML
    private ProgressBar downloadProgressBar;
    
    @FXML
    private Label downloadStatusLabel;
    
    @FXML
    private Button downloadButton;
    
    @FXML
    private Button validateButton;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button refreshButton;
    
    private final VersionManager versionManager;
    private final GameLauncher gameLauncher;
    private ObservableList<VersionMetadata> versionList = FXCollections.observableArrayList();
    private FilteredList<VersionMetadata> filteredVersionList;
    private VersionMetadata selectedVersion;
    private VersionDetail selectedVersionDetail;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 构造函数
     * Constructor
     */
    public VersionController() {
        this.versionManager = LauncherCore.getInstance().getVersionManager();
        this.gameLauncher = new GameLauncher();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
        setupFilters();
        setupEventHandlers();
        loadVersions();
    }
    
    /**
     * 初始化表格
     * Initialize table
     */
    private void initializeTable() {
        // 设置表格列
        // Set table columns
        versionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        versionTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatType(cellData.getValue().getType())));
        versionReleaseDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime releaseDate = cellData.getValue().getReleaseTime();
            return new SimpleStringProperty(releaseDate != null ? releaseDate.format(dateFormatter) : "-");
        });
        versionInstalledColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isInstalled()));
        
        // 为已安装列设置自定义单元格工厂
        // Set custom cell factory for installed column
        versionInstalledColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(new CheckBox() {{
                        setSelected(item);
                        setDisable(true);
                    }});
                }
            }
        });
        
        // 创建过滤列表
        // Create filtered list
        filteredVersionList = new FilteredList<>(versionList, p -> true);
        versionTable.setItems(filteredVersionList);
    }
    
    /**
     * 设置过滤器
     * Setup filters
     */
    private void setupFilters() {
        // 初始化版本类型过滤器
        // Initialize version type filter
        versionTypeFilter.getItems().addAll("全部 | All", "正式版 | Release", "快照 | Snapshot", "Beta", "Alpha");
        versionTypeFilter.getSelectionModel().selectFirst();
        
        // 设置过滤逻辑
        // Set filter logic
        versionTypeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        showInstalledOnlyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    /**
     * 设置事件处理器
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // 版本选择事件
        // Version selection event
        versionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedVersion = newVal;
            updateVersionDetails(newVal);
        });
        
        // 刷新按钮事件
        // Refresh button event
        refreshButton.setOnAction(event -> loadVersions());
        
        // 下载按钮事件
        // Download button event
        downloadButton.setOnAction(event -> downloadSelectedVersion());
        
        // 验证按钮事件
        // Validate button event
        validateButton.setOnAction(event -> validateSelectedVersion());
        
        // 启动按钮事件
        // Launch button event
        startButton.setOnAction(event -> launchGame());
    }
    
    /**
     * 加载版本列表
     * Load versions
     */
    private void loadVersions() {
        statusLabel.setText("正在加载版本列表... | Loading version list...");
        downloadButton.setDisable(true);
        validateButton.setDisable(true);
        startButton.setDisable(true);
        
        versionManager.getAllVersions()
                .thenAccept(versions -> {
                    Platform.runLater(() -> {
                        versionList.clear();
                        versionList.addAll(versions);
                        
                        statusLabel.setText(String.format("已加载 %d 个版本 | Loaded %d versions", versions.size(), versions.size()));
                        
                        if (!versionTable.getSelectionModel().isEmpty()) {
                            // 保持选中之前的版本
                            // Keep previously selected version
                            String previousId = selectedVersion != null ? selectedVersion.getId() : null;
                            if (previousId != null) {
                                for (VersionMetadata version : versionList) {
                                    if (version.getId().equals(previousId)) {
                                        versionTable.getSelectionModel().select(version);
                                        break;
                                    }
                                }
                            }
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        LogUtils.error("加载版本列表失败 | Failed to load version list", e);
                        statusLabel.setText("加载版本列表失败 | Failed to load version list");
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误 | Error");
                        alert.setHeaderText("加载版本列表失败 | Failed to load version list");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }
    
    /**
     * 应用过滤器
     * Apply filters
     */
    private void applyFilters() {
        filteredVersionList.setPredicate(createPredicate());
    }
    
    /**
     * 创建过滤谓词
     * Create filter predicate
     * 
     * @return 过滤谓词 Filter predicate
     */
    private Predicate<VersionMetadata> createPredicate() {
        return version -> {
            boolean matchesType = typeMatches(version);
            boolean matchesSearch = searchMatches(version);
            boolean matchesInstalled = installedMatches(version);
            
            return matchesType && matchesSearch && matchesInstalled;
        };
    }
    
    /**
     * 类型匹配
     * Type matches
     * 
     * @param version 版本元数据 Version metadata
     * @return 是否匹配 Whether matches
     */
    private boolean typeMatches(VersionMetadata version) {
        String filterType = versionTypeFilter.getValue();
        
        if (filterType.startsWith("全部") || filterType.startsWith("All")) {
            return true;
        }
        
        String type = version.getType().toLowerCase();
        
        if (filterType.startsWith("正式版") || filterType.startsWith("Release")) {
            return type.equals("release");
        } else if (filterType.startsWith("快照") || filterType.startsWith("Snapshot")) {
            return type.equals("snapshot");
        } else if (filterType.startsWith("Beta")) {
            return type.equals("old_beta");
        } else if (filterType.startsWith("Alpha")) {
            return type.equals("old_alpha");
        }
        
        return true;
    }
    
    /**
     * 搜索匹配
     * Search matches
     * 
     * @param version 版本元数据 Version metadata
     * @return 是否匹配 Whether matches
     */
    private boolean searchMatches(VersionMetadata version) {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            return true;
        }
        
        return version.getId().toLowerCase().contains(searchText);
    }
    
    /**
     * 安装状态匹配
     * Installed matches
     * 
     * @param version 版本元数据 Version metadata
     * @return 是否匹配 Whether matches
     */
    private boolean installedMatches(VersionMetadata version) {
        return !showInstalledOnlyCheckbox.isSelected() || version.isInstalled();
    }
    
    /**
     * 更新版本详情
     * Update version details
     * 
     * @param version 版本元数据 Version metadata
     */
    private void updateVersionDetails(VersionMetadata version) {
        if (version == null) {
            versionTitleLabel.setText("请选择版本 | Please select a version");
            versionIdLabel.setText("-");
            versionTypeLabel.setText("-");
            versionReleaseDateLabel.setText("-");
            versionJavaLabel.setText("-");
            versionStatusLabel.setText("-");
            
            downloadButton.setDisable(true);
            validateButton.setDisable(true);
            startButton.setDisable(true);
            
            selectedVersionDetail = null;
            return;
        }
        
        versionTitleLabel.setText(version.getId());
        versionIdLabel.setText(version.getId());
        versionTypeLabel.setText(formatType(version.getType()));
        
        LocalDateTime releaseDate = version.getReleaseTime();
        versionReleaseDateLabel.setText(releaseDate != null ? releaseDate.format(dateFormatter) : "-");
        
        if (version.isInstalled()) {
            versionStatusLabel.setText("已安装 | Installed");
            downloadButton.setText("重新下载 | Re-download");
            downloadButton.setDisable(false);
            validateButton.setDisable(false);
            startButton.setDisable(false);
        } else {
            versionStatusLabel.setText("未安装 | Not installed");
            downloadButton.setText("下载 | Download");
            downloadButton.setDisable(false);
            validateButton.setDisable(true);
            startButton.setDisable(true);
        }
        
        // 加载详细信息
        // Load details
        if (version.isInstalled()) {
            loadVersionDetails(version);
        } else {
            versionJavaLabel.setText("待下载后确定 | To be determined after download");
            selectedVersionDetail = null;
        }
    }
    
    /**
     * 加载版本详细信息
     * Load version details
     * 
     * @param version 版本元数据 Version metadata
     */
    private void loadVersionDetails(VersionMetadata version) {
        versionManager.getVersionDetails(version.getId())
                .thenAccept(detail -> {
                    Platform.runLater(() -> {
                        selectedVersionDetail = detail;
                        
                        if (detail != null && detail.getJavaVersion() != null) {
                            versionJavaLabel.setText("Java " + detail.getJavaVersion().getMajorVersion());
                        } else {
                            versionJavaLabel.setText("Java 8");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        LogUtils.error("加载版本详情失败 | Failed to load version details", e);
                        versionJavaLabel.setText("未知 | Unknown");
                    });
                    return null;
                });
    }
    
    /**
     * 下载选中的版本
     * Download selected version
     */
    private void downloadSelectedVersion() {
        if (selectedVersion == null) {
            return;
        }
        
        // 禁用按钮
        // Disable buttons
        downloadButton.setDisable(true);
        validateButton.setDisable(true);
        startButton.setDisable(true);
        refreshButton.setDisable(true);
        
        // 显示进度条和状态
        // Show progress bar and status
        downloadProgressBar.setVisible(true);
        downloadStatusLabel.setVisible(true);
        downloadStatusLabel.setText("准备下载... | Preparing download...");
        
        // 开始下载
        // Start download
        versionManager.downloadVersion(selectedVersion.getId(), new VersionManager.ProgressCallback() {
            @Override
            public void onProgress(int progress, String status) {
                Platform.runLater(() -> {
                    downloadProgressBar.setProgress(progress / 100.0);
                    downloadStatusLabel.setText(status);
                    statusLabel.setText(status);
                });
            }
            
            @Override
            public void onComplete() {
                Platform.runLater(() -> {
                    // 隐藏进度条和状态
                    // Hide progress bar and status
                    downloadProgressBar.setVisible(false);
                    downloadStatusLabel.setVisible(false);
                    
                    // 更新UI
                    // Update UI
                    statusLabel.setText("下载完成 | Download completed");
                    selectedVersion.setInstalled(true);
                    updateVersionDetails(selectedVersion);
                    
                    // 启用按钮
                    // Enable buttons
                    refreshButton.setDisable(false);
                    
                    // 显示成功消息
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("下载完成 | Download Complete");
                    alert.setHeaderText("版本 " + selectedVersion.getId() + " 已下载完成");
                    alert.setContentText("现在可以启动游戏了");
                    alert.showAndWait();
                });
            }
            
            @Override
            public void onError(String error) {
                Platform.runLater(() -> {
                    // 隐藏进度条
                    // Hide progress bar
                    downloadProgressBar.setVisible(false);
                    downloadStatusLabel.setVisible(false);
                    
                    // 更新状态
                    // Update status
                    statusLabel.setText("下载失败 | Download failed");
                    
                    // 启用按钮
                    // Enable buttons
                    downloadButton.setDisable(false);
                    refreshButton.setDisable(false);
                    
                    // 显示错误消息
                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("下载失败 | Download Failed");
                    alert.setHeaderText("版本 " + selectedVersion.getId() + " 下载失败");
                    alert.setContentText(error);
                    alert.showAndWait();
                });
            }
        });
    }
    
    /**
     * 验证选中的版本
     * Validate selected version
     */
    private void validateSelectedVersion() {
        if (selectedVersion == null || !selectedVersion.isInstalled()) {
            return;
        }
        
        // 禁用按钮
        // Disable buttons
        downloadButton.setDisable(true);
        validateButton.setDisable(true);
        startButton.setDisable(true);
        refreshButton.setDisable(true);
        
        // 更新状态
        // Update status
        statusLabel.setText("正在验证... | Validating...");
        
        // 开始验证
        // Start validation
        versionManager.validateVersion(selectedVersion.getId())
                .thenAccept(valid -> {
                    Platform.runLater(() -> {
                        // 启用按钮
                        // Enable buttons
                        downloadButton.setDisable(false);
                        validateButton.setDisable(false);
                        startButton.setDisable(!valid);
                        refreshButton.setDisable(false);
                        
                        if (valid) {
                            statusLabel.setText("验证成功 | Validation successful");
                            
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("验证成功 | Validation Successful");
                            alert.setHeaderText("版本 " + selectedVersion.getId() + " 验证成功");
                            alert.setContentText("您可以启动游戏了");
                            alert.showAndWait();
                        } else {
                            statusLabel.setText("验证失败 | Validation failed");
                            
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("验证失败 | Validation Failed");
                            alert.setHeaderText("版本 " + selectedVersion.getId() + " 验证失败");
                            alert.setContentText("文件可能已损坏，建议重新下载");
                            alert.showAndWait();
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        // 启用按钮
                        // Enable buttons
                        downloadButton.setDisable(false);
                        validateButton.setDisable(false);
                        startButton.setDisable(true);
                        refreshButton.setDisable(false);
                        
                        // 更新状态
                        // Update status
                        statusLabel.setText("验证出错 | Validation error");
                        
                        // 显示错误消息
                        // Show error message
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("验证出错 | Validation Error");
                        alert.setHeaderText("版本 " + selectedVersion.getId() + " 验证过程中出错");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }
    
    /**
     * 启动游戏
     * Launch game
     */
    private void launchGame() {
        if (selectedVersion == null || !selectedVersion.isInstalled() || selectedVersionDetail == null) {
            return;
        }
        
        // 禁用启动按钮
        // Disable launch button
        startButton.setDisable(true);
        
        // 更新状态
        // Update status
        statusLabel.setText("正在启动游戏... | Launching game...");
        
        // 启动游戏
        // Launch game
        gameLauncher.launchGame(selectedVersion.getId())
                .thenAccept(process -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("游戏已启动 | Game launched");
                        
                        // 启用按钮
                        // Enable buttons
                        startButton.setDisable(false);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        LogUtils.error("启动游戏失败 | Failed to launch game", e);
                        statusLabel.setText("启动游戏失败 | Failed to launch game");
                        
                        // 启用按钮
                        // Enable buttons
                        startButton.setDisable(false);
                        
                        // 显示错误消息
                        // Show error message
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("启动失败 | Launch Failed");
                        alert.setHeaderText("版本 " + selectedVersion.getId() + " 启动失败");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }
    
    /**
     * 格式化版本类型
     * Format version type
     * 
     * @param type 类型 Type
     * @return 格式化后的类型 Formatted type
     */
    private String formatType(String type) {
        if (type == null) {
            return "未知 | Unknown";
        }
        
        switch (type.toLowerCase()) {
            case "release":
                return "正式版 | Release";
            case "snapshot":
                return "快照 | Snapshot";
            case "old_beta":
                return "Beta";
            case "old_alpha":
                return "Alpha";
            default:
                return type;
        }
    }
} 