package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.model.Mod;
import com.wazixwx.launcher.service.ModService;
import com.wazixwx.launcher.utils.AnimationUtils;
import com.wazixwx.launcher.utils.LogUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 模组控制器类
 * Mod Controller Class
 * 
 * 处理模组管理界面的用户交互逻辑
 * Handles user interaction logic for mod management interface
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ModController implements Initializable {
    
    @FXML
    private TableView<Mod> installedModsTable;
    
    @FXML
    private TableView<Mod> enabledModsTable;
    
    @FXML
    private TableColumn<Mod, String> installedNameColumn;
    
    @FXML
    private TableColumn<Mod, String> installedVersionColumn;
    
    @FXML
    private TableColumn<Mod, String> installedAuthorColumn;
    
    @FXML
    private TableColumn<Mod, String> installedLoaderColumn;
    
    @FXML
    private TableColumn<Mod, String> installedTypeColumn;
    
    @FXML
    private TableColumn<Mod, String> enabledNameColumn;
    
    @FXML
    private TableColumn<Mod, String> enabledVersionColumn;
    
    @FXML
    private TableColumn<Mod, String> enabledAuthorColumn;
    
    @FXML
    private TableColumn<Mod, String> enabledLoaderColumn;
    
    @FXML
    private TableColumn<Mod, String> enabledTypeColumn;
    
    @FXML
    private TextArea modDescriptionArea;
    
    @FXML
    private VBox detailsBox;
    
    @FXML
    private ComboBox<String> versionFilterComboBox;
    
    @FXML
    private ComboBox<String> loaderFilterComboBox;
    
    @FXML
    private Button enableButton;
    
    @FXML
    private Button disableButton;
    
    @FXML
    private Button installButton;
    
    @FXML
    private Button uninstallButton;
    
    @FXML
    private Label modCountLabel;
    
    @FXML
    private Label enabledCountLabel;
    
    private final ObservableList<Mod> installedMods = FXCollections.observableArrayList();
    private final ObservableList<Mod> enabledMods = FXCollections.observableArrayList();
    
    private final ModService modService;
    
    /**
     * 构造函数
     * Constructor
     */
    public ModController() {
        this.modService = LauncherCore.getInstance().getModService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        setupEventHandlers();
        setupFilters();
        refreshModsList();
    }
    
    /**
     * 初始化表格列
     * Initialize table columns
     */
    private void initializeTableColumns() {
        // 已安装模组表格
        // Installed mods table
        installedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        installedVersionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        installedAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        installedLoaderColumn.setCellValueFactory(new PropertyValueFactory<>("modLoaderType"));
        installedTypeColumn.setCellValueFactory(cellData -> {
            Mod.ModType type = cellData.getValue().getModType();
            String displayType = switch (type) {
                case NORMAL -> "普通 | Normal";
                case CORE -> "核心 | Core";
                case API -> "API";
                case RESOURCE -> "资源 | Resource";
                case LOADER -> "加载器 | Loader";
                default -> "未知 | Unknown";
            };
            return new SimpleStringProperty(displayType);
        });
        
        // 已启用模组表格
        // Enabled mods table
        enabledNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        enabledVersionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        enabledAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        enabledLoaderColumn.setCellValueFactory(new PropertyValueFactory<>("modLoaderType"));
        enabledTypeColumn.setCellValueFactory(cellData -> {
            Mod.ModType type = cellData.getValue().getModType();
            String displayType = switch (type) {
                case NORMAL -> "普通 | Normal";
                case CORE -> "核心 | Core";
                case API -> "API";
                case RESOURCE -> "资源 | Resource";
                case LOADER -> "加载器 | Loader";
                default -> "未知 | Unknown";
            };
            return new SimpleStringProperty(displayType);
        });
        
        installedModsTable.setItems(installedMods);
        enabledModsTable.setItems(enabledMods);
    }
    
    /**
     * 设置事件处理器
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // 安装按钮点击事件
        // Install button click event
        installButton.setOnAction(event -> installMod());
        
        // 卸载按钮点击事件
        // Uninstall button click event
        uninstallButton.setOnAction(event -> uninstallMod());
        
        // 启用按钮点击事件
        // Enable button click event
        enableButton.setOnAction(event -> enableMod());
        
        // 禁用按钮点击事件
        // Disable button click event
        disableButton.setOnAction(event -> disableMod());
        
        // 已安装模组表格选择事件
        // Installed mods table selection event
        installedModsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // 清除另一个表格的选择
                // Clear selection in the other table
                enabledModsTable.getSelectionModel().clearSelection();
                
                // 更新按钮状态
                // Update button states
                updateButtonStates();
                
                // 显示模组详情
                // Show mod details
                showModDetails(newSelection);
            }
        });
        
        // 已启用模组表格选择事件
        // Enabled mods table selection event
        enabledModsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // 清除另一个表格的选择
                // Clear selection in the other table
                installedModsTable.getSelectionModel().clearSelection();
                
                // 更新按钮状态
                // Update button states
                updateButtonStates();
                
                // 显示模组详情
                // Show mod details
                showModDetails(newSelection);
            }
        });
        
        // 过滤器更改事件
        // Filter change events
        versionFilterComboBox.setOnAction(event -> applyFilters());
        loaderFilterComboBox.setOnAction(event -> applyFilters());
    }
    
    /**
     * 设置过滤器
     * Setup filters
     */
    private void setupFilters() {
        // 初始化Minecraft版本过滤器
        // Initialize Minecraft version filter
        List<String> versions = LauncherCore.getInstance()
                .getVersionManager()
                .getAllVersions()
                .stream()
                .map(v -> v.getId())
                .collect(Collectors.toList());
        
        // 添加"全部"选项
        // Add "All" option
        versions.add(0, "全部 | All");
        versionFilterComboBox.setItems(FXCollections.observableArrayList(versions));
        versionFilterComboBox.getSelectionModel().selectFirst();
        
        // 初始化模组加载器过滤器
        // Initialize mod loader filter
        loaderFilterComboBox.setItems(FXCollections.observableArrayList(
                "全部 | All", "Forge", "Fabric", "Quilt", "LiteLoader"));
        loaderFilterComboBox.getSelectionModel().selectFirst();
    }
    
    /**
     * 应用过滤器
     * Apply filters
     */
    private void applyFilters() {
        refreshModsList();
    }
    
    /**
     * 更新按钮状态
     * Update button states
     */
    private void updateButtonStates() {
        Mod selectedInstalledMod = installedModsTable.getSelectionModel().getSelectedItem();
        Mod selectedEnabledMod = enabledModsTable.getSelectionModel().getSelectedItem();
        
        enableButton.setDisable(selectedInstalledMod == null || selectedInstalledMod.isEnabled());
        disableButton.setDisable(selectedEnabledMod == null);
        uninstallButton.setDisable(selectedInstalledMod == null && selectedEnabledMod == null);
    }
    
    /**
     * 显示模组详情
     * Show mod details
     * 
     * @param mod 要显示详情的模组 | Mod to show details
     */
    private void showModDetails(Mod mod) {
        if (mod == null) {
            detailsBox.setVisible(false);
            return;
        }
        
        detailsBox.setVisible(true);
        
        // 设置描述文本
        // Set description text
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("名称 | Name: ").append(mod.getName()).append("\n");
        descriptionBuilder.append("版本 | Version: ").append(mod.getVersion()).append("\n");
        descriptionBuilder.append("作者 | Author: ").append(mod.getAuthor()).append("\n");
        descriptionBuilder.append("加载器 | Loader: ").append(mod.getModLoaderType()).append("\n");
        
        String typeText = switch (mod.getModType()) {
            case NORMAL -> "普通 | Normal";
            case CORE -> "核心 | Core";
            case API -> "API";
            case RESOURCE -> "资源 | Resource";
            case LOADER -> "加载器 | Loader";
            default -> "未知 | Unknown";
        };
        descriptionBuilder.append("类型 | Type: ").append(typeText).append("\n\n");
        
        descriptionBuilder.append("描述 | Description: \n").append(mod.getDescription()).append("\n\n");
        
        // 添加兼容版本信息
        // Add compatible version information
        descriptionBuilder.append("兼容的Minecraft版本 | Compatible Minecraft Versions: \n");
        if (mod.getCompatibleMinecraftVersions().isEmpty()) {
            descriptionBuilder.append("未知 | Unknown");
        } else {
            descriptionBuilder.append(String.join(", ", mod.getCompatibleMinecraftVersions()));
        }
        
        modDescriptionArea.setText(descriptionBuilder.toString());
        
        // 使用动画显示详情面板
        // Use animation to show details panel
        AnimationUtils.fadeIn(detailsBox, 200);
    }
    
    /**
     * 刷新模组列表
     * Refresh mods list
     */
    private void refreshModsList() {
        String versionFilter = versionFilterComboBox.getValue();
        String loaderFilter = loaderFilterComboBox.getValue();
        
        boolean filterVersion = versionFilter != null && !versionFilter.contains("All");
        boolean filterLoader = loaderFilter != null && !loaderFilter.contains("All");
        
        modService.getAllMods().thenAcceptAsync(mods -> {
            // 应用过滤器
            // Apply filters
            List<Mod> filteredMods = mods.stream()
                .filter(mod -> {
                    if (filterVersion) {
                        if (!mod.getCompatibleMinecraftVersions().contains(versionFilter)) {
                            return false;
                        }
                    }
                    
                    if (filterLoader) {
                        if (!mod.getModLoaderType().equalsIgnoreCase(loaderFilter)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // 分离已启用和未启用的模组
            // Separate enabled and disabled mods
            List<Mod> disabledMods = filteredMods.stream()
                .filter(mod -> !mod.isEnabled())
                .collect(Collectors.toList());
            
            List<Mod> enabledModsList = filteredMods.stream()
                .filter(Mod::isEnabled)
                .collect(Collectors.toList());
            
            // 更新UI
            // Update UI
            Platform.runLater(() -> {
                installedMods.clear();
                installedMods.addAll(disabledMods);
                
                enabledMods.clear();
                enabledMods.addAll(enabledModsList);
                
                modCountLabel.setText(String.format("共 %d 个已安装模组 | %d installed mods in total", filteredMods.size(), filteredMods.size()));
                enabledCountLabel.setText(String.format("共 %d 个已启用模组 | %d enabled mods in total", enabledModsList.size(), enabledModsList.size()));
                
                updateButtonStates();
            });
        });
    }
    
    /**
     * 安装模组
     * Install mod
     */
    private void installMod() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模组文件 | Select Mod File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Minecraft模组 | Minecraft Mods", "*.jar")
        );
        
        File file = fileChooser.showOpenDialog(installButton.getScene().getWindow());
        if (file != null) {
            // 显示加载对话框
            // Show loading dialog
            Alert loadingDialog = new Alert(Alert.AlertType.INFORMATION);
            loadingDialog.setTitle("正在安装模组 | Installing Mod");
            loadingDialog.setHeaderText(null);
            loadingDialog.setContentText("正在安装模组，请稍候... | Installing mod, please wait...");
            
            // 非模态显示
            // Non-modal display
            Stage stage = (Stage) loadingDialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            loadingDialog.show();
            
            modService.installMod(file.toPath()).thenAcceptAsync(success -> {
                Platform.runLater(() -> {
                    loadingDialog.close();
                    
                    if (success) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("安装成功 | Installation Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("模组已成功安装 | Mod has been successfully installed");
                        alert.showAndWait();
                        
                        // 刷新模组列表
                        // Refresh mods list
                        refreshModsList();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("安装失败 | Installation Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("模组安装失败 | Failed to install mod");
                        alert.showAndWait();
                    }
                });
            });
        }
    }
    
    /**
     * 卸载模组
     * Uninstall mod
     */
    private void uninstallMod() {
        Mod selectedMod = installedModsTable.getSelectionModel().getSelectedItem();
        if (selectedMod == null) {
            selectedMod = enabledModsTable.getSelectionModel().getSelectedItem();
        }
        
        if (selectedMod != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("确认卸载 | Confirm Uninstallation");
            confirmDialog.setHeaderText(null);
            confirmDialog.setContentText("确定要卸载模组 " + selectedMod.getName() + " 吗？ | Are you sure you want to uninstall mod " + selectedMod.getName() + "?");
            
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Mod modToUninstall = selectedMod;
                    
                    modService.uninstallMod(modToUninstall).thenAcceptAsync(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("卸载成功 | Uninstallation Successful");
                                alert.setHeaderText(null);
                                alert.setContentText("模组已成功卸载 | Mod has been successfully uninstalled");
                                alert.showAndWait();
                                
                                // 刷新模组列表
                                // Refresh mods list
                                refreshModsList();
                                
                                // 清除详情区域
                                // Clear details area
                                detailsBox.setVisible(false);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("卸载失败 | Uninstallation Failed");
                                alert.setHeaderText(null);
                                alert.setContentText("模组卸载失败 | Failed to uninstall mod");
                                alert.showAndWait();
                            }
                        });
                    });
                }
            });
        }
    }
    
    /**
     * 启用模组
     * Enable mod
     */
    private void enableMod() {
        Mod selectedMod = installedModsTable.getSelectionModel().getSelectedItem();
        
        if (selectedMod != null) {
            modService.enableMod(selectedMod).thenAcceptAsync(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        // 刷新模组列表
                        // Refresh mods list
                        refreshModsList();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("启用失败 | Enabling Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("模组启用失败 | Failed to enable mod");
                        alert.showAndWait();
                    }
                });
            });
        }
    }
    
    /**
     * 禁用模组
     * Disable mod
     */
    private void disableMod() {
        Mod selectedMod = enabledModsTable.getSelectionModel().getSelectedItem();
        
        if (selectedMod != null) {
            modService.disableMod(selectedMod).thenAcceptAsync(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        // 刷新模组列表
                        // Refresh mods list
                        refreshModsList();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("禁用失败 | Disabling Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("模组禁用失败 | Failed to disable mod");
                        alert.showAndWait();
                    }
                });
            });
        }
    }
} 