package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.model.ResourcePack;
import com.wazixwx.launcher.service.ResourcePackService;
import com.wazixwx.launcher.utils.AnimationUtils;
import com.wazixwx.launcher.utils.BlurUtils;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.ResourcePackUtils;
import com.wazixwx.launcher.utils.StyleUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源包界面控制器
 * Resource Pack Controller
 * 
 * 用于管理资源包的UI交互
 * Used to manage UI interactions for resource packs
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ResourcePackController {
    private final ResourcePackService resourcePackService = ResourcePackService.getInstance();
    private final ObservableList<ResourcePack> availableResourcePacks = FXCollections.observableArrayList();
    private final ObservableList<ResourcePack> enabledResourcePacks = FXCollections.observableArrayList();
    
    @FXML private TableView<ResourcePack> availablePacksTable;
    @FXML private TableView<ResourcePack> enabledPacksTable;
    @FXML private Button addPackButton;
    @FXML private Button removePackButton;
    @FXML private Button moveUpButton;
    @FXML private Button moveDownButton;
    @FXML private Button enablePackButton;
    @FXML private Button disablePackButton;
    @FXML private Label packNameLabel;
    @FXML private Label packDescriptionLabel;
    @FXML private Label packVersionLabel;
    @FXML private ImageView packIconView;
    @FXML private Pane detailsPane;
    
    private ResourcePack selectedPack;
    private Stage stage;
    
    /**
     * 初始化
     * Initialize
     */
    @FXML
    public void initialize() {
        configureButtons();
        configureTables();
        applyStyles();
        loadResourcePacks();
    }
    
    /**
     * 设置舞台
     * Set stage
     * 
     * @param stage 舞台 | Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * 配置按钮
     * Configure buttons
     */
    private void configureButtons() {
        addPackButton.setOnAction(event -> addResourcePack());
        removePackButton.setOnAction(event -> removeResourcePack());
        moveUpButton.setOnAction(event -> moveResourcePackUp());
        moveDownButton.setOnAction(event -> moveResourcePackDown());
        enablePackButton.setOnAction(event -> enableResourcePack());
        disablePackButton.setOnAction(event -> disableResourcePack());
        
        // 设置按钮状态
        // Set button states
        removePackButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
        enablePackButton.setDisable(true);
        disablePackButton.setDisable(true);
    }
    
    /**
     * 配置表格
     * Configure tables
     */
    private void configureTables() {
        // 可用资源包表格
        // Available resource packs table
        TableColumn<ResourcePack, String> availableNameColumn = new TableColumn<>("资源包名称 | Pack Name");
        availableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        availableNameColumn.setPrefWidth(200);
        
        TableColumn<ResourcePack, String> availableVersionColumn = new TableColumn<>("版本 | Version");
        availableVersionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        availableVersionColumn.setPrefWidth(100);
        
        availablePacksTable.getColumns().addAll(availableNameColumn, availableVersionColumn);
        availablePacksTable.setItems(availableResourcePacks);
        
        // 启用资源包表格
        // Enabled resource packs table
        TableColumn<ResourcePack, String> enabledNameColumn = new TableColumn<>("资源包名称 | Pack Name");
        enabledNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        enabledNameColumn.setPrefWidth(200);
        
        TableColumn<ResourcePack, Integer> priorityColumn = new TableColumn<>("优先级 | Priority");
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setPrefWidth(100);
        
        enabledPacksTable.getColumns().addAll(enabledNameColumn, priorityColumn);
        enabledPacksTable.setItems(enabledResourcePacks);
        
        // 选择事件
        // Selection events
        availablePacksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                enabledPacksTable.getSelectionModel().clearSelection();
                selectResourcePack(newSelection);
                removePackButton.setDisable(false);
                enablePackButton.setDisable(false);
                disablePackButton.setDisable(true);
                moveUpButton.setDisable(true);
                moveDownButton.setDisable(true);
            }
        });
        
        enabledPacksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                availablePacksTable.getSelectionModel().clearSelection();
                selectResourcePack(newSelection);
                removePackButton.setDisable(false);
                enablePackButton.setDisable(true);
                disablePackButton.setDisable(false);
                
                int index = enabledResourcePacks.indexOf(newSelection);
                moveUpButton.setDisable(index <= 0);
                moveDownButton.setDisable(index >= enabledResourcePacks.size() - 1);
            }
        });
    }
    
    /**
     * 应用样式
     * Apply styles
     */
    private void applyStyles() {
        // 应用毛玻璃效果
        // Apply frosted glass effect
        BlurUtils.applyBlurEffect(detailsPane);
        
        // 应用圆角
        // Apply rounded corners
        StyleUtils.applyRoundedCorners(detailsPane, 2);
        StyleUtils.applyRoundedCorners(availablePacksTable, 2);
        StyleUtils.applyRoundedCorners(enabledPacksTable, 2);
        StyleUtils.applyRoundedCorners(addPackButton, 2);
        StyleUtils.applyRoundedCorners(removePackButton, 2);
        StyleUtils.applyRoundedCorners(moveUpButton, 2);
        StyleUtils.applyRoundedCorners(moveDownButton, 2);
        StyleUtils.applyRoundedCorners(enablePackButton, 2);
        StyleUtils.applyRoundedCorners(disablePackButton, 2);
        
        // 应用阴影效果
        // Apply shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.color(0, 0, 0, 0.2));
        dropShadow.setRadius(10);
        detailsPane.setEffect(dropShadow);
        availablePacksTable.setEffect(dropShadow);
        enabledPacksTable.setEffect(dropShadow);
        
        // 按钮悬停动画
        // Button hover animations
        AnimationUtils.addButtonHoverAnimation(addPackButton);
        AnimationUtils.addButtonHoverAnimation(removePackButton);
        AnimationUtils.addButtonHoverAnimation(moveUpButton);
        AnimationUtils.addButtonHoverAnimation(moveDownButton);
        AnimationUtils.addButtonHoverAnimation(enablePackButton);
        AnimationUtils.addButtonHoverAnimation(disablePackButton);
    }
    
    /**
     * 选择资源包
     * Select resource pack
     * 
     * @param resourcePack 资源包 | Resource pack
     */
    private void selectResourcePack(ResourcePack resourcePack) {
        this.selectedPack = resourcePack;
        
        if (resourcePack != null) {
            packNameLabel.setText(resourcePack.getName());
            packDescriptionLabel.setText(resourcePack.getDescription());
            packVersionLabel.setText("版本 | Version: " + resourcePack.getVersion());
            
            // 尝试加载资源包图标
            // Try to load resource pack icon
            loadResourcePackIcon(resourcePack);
        } else {
            packNameLabel.setText("");
            packDescriptionLabel.setText("");
            packVersionLabel.setText("");
            packIconView.setImage(null);
        }
    }
    
    /**
     * 加载资源包图标
     * Load resource pack icon
     * 
     * @param resourcePack 资源包 | Resource pack
     */
    private void loadResourcePackIcon(ResourcePack resourcePack) {
        // 使用ResourcePackUtils加载资源包图标
        // Use ResourcePackUtils to load resource pack icon
        Image icon = ResourcePackUtils.loadResourcePackIcon(resourcePack);
        packIconView.setImage(icon);
        
        // 显示Minecraft兼容版本
        // Show Minecraft compatible versions
        String versionInfo = "版本 | Version: " + resourcePack.getVersion();
        String compatibleVersions = ResourcePackUtils.getFormatVersionName(resourcePack.getVersion());
        if (!compatibleVersions.equals("未知版本 | Unknown version")) {
            versionInfo += " (" + compatibleVersions + ")";
        }
        packVersionLabel.setText(versionInfo);
    }
    
    /**
     * 加载资源包
     * Load resource packs
     */
    private void loadResourcePacks() {
        resourcePackService.scanAndLoadResourcePacks()
            .thenAcceptAsync(packs -> {
                Platform.runLater(() -> {
                    // 更新可用和启用的资源包列表
                    // Update available and enabled resource pack lists
                    List<ResourcePack> enabled = packs.stream()
                        .filter(ResourcePack::isEnabled)
                        .sorted(Comparator.comparingInt(ResourcePack::getPriority).reversed())
                        .collect(Collectors.toList());
                    
                    List<ResourcePack> available = packs.stream()
                        .filter(pack -> !pack.isEnabled())
                        .collect(Collectors.toList());
                    
                    availableResourcePacks.clear();
                    enabledResourcePacks.clear();
                    availableResourcePacks.addAll(available);
                    enabledResourcePacks.addAll(enabled);
                });
            })
            .exceptionally(ex -> {
                LogUtils.error("加载资源包失败 | Failed to load resource packs", ex);
                return null;
            });
    }
    
    /**
     * 添加资源包
     * Add resource pack
     */
    private void addResourcePack() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择资源包 | Select Resource Pack");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("资源包文件 | Resource Pack Files", "*.zip")
        );
        
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            Path filePath = selectedFile.toPath();
            
            resourcePackService.loadResourcePack(filePath)
                .thenComposeAsync(resourcePack -> {
                    if (resourcePack != null) {
                        return resourcePackService.saveResourcePack(resourcePack);
                    }
                    return null;
                })
                .thenRunAsync(this::loadResourcePacks)
                .exceptionally(ex -> {
                    LogUtils.error("添加资源包失败 | Failed to add resource pack", ex);
                    return null;
                });
        }
    }
    
    /**
     * 移除资源包
     * Remove resource pack
     */
    private void removeResourcePack() {
        if (selectedPack != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认删除 | Confirm Deletion");
            alert.setHeaderText("删除资源包 | Delete Resource Pack");
            alert.setContentText("确定要删除资源包 \"" + selectedPack.getName() + "\" 吗？ | Are you sure you want to delete the resource pack \"" + selectedPack.getName() + "\"?");
            
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    resourcePackService.deleteResourcePack(selectedPack)
                        .thenRunAsync(this::loadResourcePacks)
                        .exceptionally(ex -> {
                            LogUtils.error("删除资源包失败 | Failed to delete resource pack", ex);
                            return null;
                        });
                }
            });
        }
    }
    
    /**
     * 启用资源包
     * Enable resource pack
     */
    private void enableResourcePack() {
        if (selectedPack != null) {
            resourcePackService.enableResourcePack(selectedPack)
                .thenRunAsync(this::loadResourcePacks)
                .exceptionally(ex -> {
                    LogUtils.error("启用资源包失败 | Failed to enable resource pack", ex);
                    return null;
                });
        }
    }
    
    /**
     * 禁用资源包
     * Disable resource pack
     */
    private void disableResourcePack() {
        if (selectedPack != null) {
            resourcePackService.disableResourcePack(selectedPack)
                .thenRunAsync(this::loadResourcePacks)
                .exceptionally(ex -> {
                    LogUtils.error("禁用资源包失败 | Failed to disable resource pack", ex);
                    return null;
                });
        }
    }
    
    /**
     * 向上移动资源包
     * Move resource pack up
     */
    private void moveResourcePackUp() {
        if (selectedPack != null && selectedPack.isEnabled()) {
            int index = enabledResourcePacks.indexOf(selectedPack);
            if (index > 0) {
                ResourcePack abovePack = enabledResourcePacks.get(index - 1);
                int newPriority = abovePack.getPriority() + 1;
                
                resourcePackService.setResourcePackPriority(selectedPack, newPriority)
                    .thenRunAsync(this::loadResourcePacks)
                    .exceptionally(ex -> {
                        LogUtils.error("移动资源包失败 | Failed to move resource pack", ex);
                        return null;
                    });
            }
        }
    }
    
    /**
     * 向下移动资源包
     * Move resource pack down
     */
    private void moveResourcePackDown() {
        if (selectedPack != null && selectedPack.isEnabled()) {
            int index = enabledResourcePacks.indexOf(selectedPack);
            if (index < enabledResourcePacks.size() - 1) {
                ResourcePack belowPack = enabledResourcePacks.get(index + 1);
                int newPriority = belowPack.getPriority() - 1;
                
                resourcePackService.setResourcePackPriority(selectedPack, newPriority)
                    .thenRunAsync(this::loadResourcePacks)
                    .exceptionally(ex -> {
                        LogUtils.error("移动资源包失败 | Failed to move resource pack", ex);
                        return null;
                    });
            }
        }
    }
    
    /**
     * 刷新
     * Refresh
     */
    public void refresh() {
        loadResourcePacks();
    }
} 