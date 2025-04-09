package com.wazixwx.launcher.ui.controller;

import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.model.Skin;
import com.wazixwx.launcher.service.SkinService;
import com.wazixwx.launcher.utils.AnimationUtils;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SkinRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * 皮肤管理控制器类
 * Skin Management Controller Class
 * 
 * 负责皮肤管理界面的交互逻辑
 * Responsible for the interaction logic of the skin management interface
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SkinController implements Initializable {

    @FXML private ListView<Skin> skinListView;
    @FXML private Button addSkinButton;
    @FXML private Button deleteSkinButton;
    @FXML private Button applySkinButton;
    @FXML private StackPane previewContainer;
    @FXML private Canvas skinCanvas;
    @FXML private Button rotateCcwButton;
    @FXML private Button rotateResetButton;
    @FXML private Button rotateCwButton;
    @FXML private CheckBox animateCheckbox;
    @FXML private Label skinNameLabel;
    @FXML private Label skinTypeLabel;
    @FXML private Label skinFileLabel;
    @FXML private Label skinSizeLabel;
    @FXML private Label statusLabel;

    private SkinService skinService;
    private SkinRenderer skinRenderer;
    private AnimationTimer animationTimer;
    private double rotationY = 0.0;
    private boolean isAnimating = true;
    private SimpleBooleanProperty skinSelected = new SimpleBooleanProperty(false);

    /**
     * 初始化控制器
     * Initialize the controller
     * 
     * @param location 位置 | Location
     * @param resources 资源 | Resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LogUtils.info("初始化皮肤管理页面 | Initializing skin management page");
        
        // 获取皮肤服务
        // Get skin service
        skinService = LauncherCore.getInstance().getSkinService();
        
        // 初始化皮肤渲染器
        // Initialize skin renderer
        skinRenderer = new SkinRenderer(skinCanvas);
        
        // 初始化UI
        // Initialize UI
        initializeUI();
        
        // 加载皮肤列表
        // Load skin list
        loadSkinList();
        
        // 设置动画定时器
        // Set up animation timer
        initializeAnimationTimer();
    }
    
    /**
     * 初始化UI
     * Initialize UI
     */
    private void initializeUI() {
        // 设置列表单元格工厂
        // Set list cell factory
        skinListView.setCellFactory(param -> new ListCell<Skin>() {
            @Override
            protected void updateItem(Skin skin, boolean empty) {
                super.updateItem(skin, empty);
                
                if (empty || skin == null) {
                    setText(null);
                } else {
                    setText(skin.getName() + " (" + skin.getTypeDisplayName() + ")");
                }
            }
        });
        
        // 设置列表选择监听器
        // Set list selection listener
        skinListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateSkinInfo(newVal);
            skinSelected.set(newVal != null);
        });
        
        // 禁用按钮，直到选择了皮肤
        // Disable buttons until a skin is selected
        deleteSkinButton.disableProperty().bind(skinSelected.not());
        applySkinButton.disableProperty().bind(skinSelected.not());
        
        // 设置按钮事件
        // Set button events
        addSkinButton.setOnAction(e -> addSkin());
        deleteSkinButton.setOnAction(e -> deleteSkin());
        applySkinButton.setOnAction(e -> applySkin());
        
        // 设置旋转按钮事件
        // Set rotation button events
        rotateCcwButton.setOnAction(e -> rotateModel(-15));
        rotateResetButton.setOnAction(e -> resetRotation());
        rotateCwButton.setOnAction(e -> rotateModel(15));
        
        // 设置动画复选框事件
        // Set animation checkbox event
        animateCheckbox.setOnAction(e -> {
            isAnimating = animateCheckbox.isSelected();
            if (!isAnimating) {
                resetRotation();
            }
        });
        
        // 默认选中动画复选框
        // Animation checkbox selected by default
        animateCheckbox.setSelected(true);
        isAnimating = true;
    }
    
    /**
     * 初始化动画定时器
     * Initialize animation timer
     */
    private void initializeAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                // 约60帧每秒
                // About 60 frames per second
                if ((now - lastUpdate) >= 16_666_666) {
                    if (isAnimating) {
                        rotationY += 0.5;
                        if (rotationY >= 360) {
                            rotationY = 0;
                        }
                        renderSkin();
                    }
                    lastUpdate = now;
                }
            }
        };
        
        animationTimer.start();
    }
    
    /**
     * 加载皮肤列表
     * Load skin list
     */
    private void loadSkinList() {
        List<Skin> skins = skinService.getAllSkins();
        skinListView.setItems(FXCollections.observableArrayList(skins));
        
        // 选择当前选中的皮肤（如果有）
        // Select currently selected skin (if any)
        Skin selectedSkin = skinService.getSelectedSkin();
        if (selectedSkin != null) {
            skinListView.getSelectionModel().select(selectedSkin);
        } else if (!skins.isEmpty()) {
            skinListView.getSelectionModel().select(0);
        }
    }
    
    /**
     * 更新皮肤信息
     * Update skin information
     * 
     * @param skin 皮肤 | Skin
     */
    private void updateSkinInfo(Skin skin) {
        if (skin == null) {
            skinNameLabel.setText("");
            skinTypeLabel.setText("");
            skinFileLabel.setText("");
            skinSizeLabel.setText("");
            skinRenderer.clear();
            return;
        }
        
        skinNameLabel.setText(skin.getName());
        skinTypeLabel.setText(skin.getTypeDisplayName());
        skinFileLabel.setText(skin.getFilePath().getFileName().toString());
        
        try {
            long size = Files.size(skin.getFilePath());
            skinSizeLabel.setText(formatFileSize(size));
        } catch (IOException e) {
            skinSizeLabel.setText("未知 | Unknown");
            LogUtils.error("获取皮肤文件大小失败 | Failed to get skin file size", e);
        }
        
        // 加载并渲染皮肤
        // Load and render skin
        loadSkinImage(skin);
    }
    
    /**
     * 加载皮肤图像
     * Load skin image
     * 
     * @param skin 皮肤 | Skin
     */
    private void loadSkinImage(Skin skin) {
        try {
            File skinFile = skin.getFilePath().toFile();
            if (skinFile.exists()) {
                try (FileInputStream fis = new FileInputStream(skinFile)) {
                    Image skinImage = new Image(fis);
                    skinRenderer.setSkinImage(skinImage, skin.isSlimModel());
                    renderSkin();
                }
            } else {
                LogUtils.error("皮肤文件不存在 | Skin file does not exist: " + skinFile.getAbsolutePath());
                statusLabel.setText("错误：皮肤文件不存在 | Error: Skin file does not exist");
            }
        } catch (Exception e) {
            LogUtils.error("加载皮肤图像失败 | Failed to load skin image", e);
            statusLabel.setText("错误：加载皮肤图像失败 | Error: Failed to load skin image");
        }
    }
    
    /**
     * 渲染皮肤
     * Render skin
     */
    private void renderSkin() {
        skinRenderer.render(rotationY);
    }
    
    /**
     * 旋转模型
     * Rotate model
     * 
     * @param degrees 角度 | Degrees
     */
    private void rotateModel(double degrees) {
        rotationY += degrees;
        if (rotationY < 0) rotationY += 360;
        if (rotationY >= 360) rotationY -= 360;
        renderSkin();
    }
    
    /**
     * 重置旋转
     * Reset rotation
     */
    private void resetRotation() {
        rotationY = 0;
        renderSkin();
    }
    
    /**
     * 添加皮肤
     * Add skin
     */
    private void addSkin() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择皮肤文件 | Select Skin File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG图片 | PNG Images", "*.png")
        );
        
        File selectedFile = fileChooser.showOpenDialog(previewContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // 检查皮肤文件是否有效
                // Check if the skin file is valid
                boolean isValid = skinService.validateSkin(selectedFile);
                
                if (isValid) {
                    // 创建皮肤目录（如果不存在）
                    // Create skins directory (if not exists)
                    Path skinsDir = Paths.get("skins");
                    Files.createDirectories(skinsDir);
                    
                    // 复制皮肤文件
                    // Copy skin file
                    String skinType = skinService.determineSkinType(selectedFile);
                    String newFileName = UUID.randomUUID().toString() + ".png";
                    Path targetPath = skinsDir.resolve(newFileName);
                    Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // 获取名称（不含扩展名）
                    // Get name (without extension)
                    String name = selectedFile.getName();
                    if (name.toLowerCase().endsWith(".png")) {
                        name = name.substring(0, name.length() - 4);
                    }
                    
                    // 添加皮肤到服务
                    // Add skin to service
                    Skin newSkin = skinService.addSkin(name, skinType, targetPath);
                    
                    // 刷新列表并选择新皮肤
                    // Refresh the list and select the new skin
                    loadSkinList();
                    skinListView.getSelectionModel().select(newSkin);
                    
                    statusLabel.setText("皮肤已添加 | Skin added");
                    AnimationUtils.flashNode(statusLabel);
                } else {
                    statusLabel.setText("错误：无效的皮肤文件 | Error: Invalid skin file");
                    AnimationUtils.flashNode(statusLabel);
                }
            } catch (Exception e) {
                LogUtils.error("添加皮肤失败 | Failed to add skin", e);
                statusLabel.setText("错误：添加皮肤失败 | Error: Failed to add skin");
                AnimationUtils.flashNode(statusLabel);
            }
        }
    }
    
    /**
     * 删除皮肤
     * Delete skin
     */
    private void deleteSkin() {
        Skin selectedSkin = skinListView.getSelectionModel().getSelectedItem();
        if (selectedSkin == null) {
            return;
        }
        
        // 确认对话框
        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("删除皮肤 | Delete Skin");
        alert.setHeaderText("确认删除 | Confirm Deletion");
        alert.setContentText("您确定要删除皮肤 \"" + selectedSkin.getName() + "\" 吗？此操作不可撤销。\n"
            + "Are you sure you want to delete the skin \"" + selectedSkin.getName() + "\"? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // 删除皮肤
                    // Delete skin
                    boolean success = skinService.deleteSkin(selectedSkin);
                    
                    if (success) {
                        // 刷新列表
                        // Refresh the list
                        loadSkinList();
                        
                        statusLabel.setText("皮肤已删除 | Skin deleted");
                        AnimationUtils.flashNode(statusLabel);
                    } else {
                        statusLabel.setText("错误：删除皮肤失败 | Error: Failed to delete skin");
                        AnimationUtils.flashNode(statusLabel);
                    }
                } catch (Exception e) {
                    LogUtils.error("删除皮肤失败 | Failed to delete skin", e);
                    statusLabel.setText("错误：删除皮肤失败 | Error: Failed to delete skin");
                    AnimationUtils.flashNode(statusLabel);
                }
            }
        });
    }
    
    /**
     * 应用皮肤
     * Apply skin
     */
    private void applySkin() {
        Skin selectedSkin = skinListView.getSelectionModel().getSelectedItem();
        if (selectedSkin == null) {
            return;
        }
        
        skinService.setSelectedSkin(selectedSkin);
        statusLabel.setText("皮肤已应用，将在下次游戏启动时生效 | Skin applied, will take effect on next game launch");
        AnimationUtils.flashNode(statusLabel);
    }
    
    /**
     * 格式化文件大小
     * Format file size
     * 
     * @param size 文件大小（字节）| File size (bytes)
     * @return 格式化的文件大小 | Formatted file size
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 释放资源
     * Release resources
     */
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
} 