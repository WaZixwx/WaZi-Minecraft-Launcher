package com.wazixwx.launcher.ui;

import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.service.AccountService;
import com.wazixwx.launcher.service.AuthService;
import com.wazixwx.launcher.utils.ConfigManager;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SystemUtils;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 主窗口类
 * Main Window Class
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class MainWindow {
    private static final String MS_CLIENT_ID = "389b1b32-b5d5-43b2-bddc-84ce938d6737";
    
    private final Stage stage;
    private final BorderPane root;
    private final VBox sidebar;
    private final StackPane contentArea;
    private final HBox topBar;
    private final HBox bottomBar;
    
    private final AccountService accountService;
    private final AuthService authService;
    
    private Label usernameLabel;
    private Button loginButton;
    private ImageView avatarView;
    
    /**
     * 构造函数
     * Constructor
     */
    public MainWindow() {
        // 初始化服务
        this.accountService = new AccountService();
        this.authService = new AuthService(accountService, MS_CLIENT_ID);
        
        // 初始化界面
        stage = new Stage(StageStyle.TRANSPARENT);
        root = new BorderPane();
        
        // 设置毛玻璃效果
        BoxBlur blur = new BoxBlur(5, 5, 3);
        blur.setBlurType(BlurType.GAUSSIAN);
        root.setEffect(blur);
        
        // 设置背景色（半透明）
        root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        
        // 创建场景
        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(Color.TRANSPARENT);
        
        // 加载样式表
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        } catch (Exception e) {
            LogUtils.error("加载样式表失败", e);
        }
        
        // 设置窗口属性
        stage.setScene(scene);
        stage.setTitle("WaZi Minecraft Launcher");
        
        // 初始化UI组件
        initializeUI();
        
        // 加载用户信息
        loadUserInfo();
        
        // 添加窗口拖动功能
        addWindowDragFeature(scene);
    }
    
    /**
     * 初始化UI组件
     * Initialize UI components
     */
    private void initializeUI() {
        // 创建顶部栏
        topBar = createTopBar();
        root.setTop(topBar);
        
        // 创建侧边栏
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // 创建内容区域
        contentArea = createContentArea();
        root.setCenter(contentArea);
        
        // 创建底部栏
        bottomBar = createBottomBar();
        root.setBottom(bottomBar);
    }
    
    /**
     * 创建顶部栏
     * Create top bar
     * 
     * @return 顶部栏 Top bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 2px;");
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        // 添加标题
        Label titleLabel = new Label("WaZi Minecraft Launcher");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #333333;");
        
        // 添加窗口控制按钮
        HBox windowControls = new HBox();
        windowControls.setSpacing(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        
        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().add("window-control-button");
        minimizeButton.setOnAction(e -> stage.setIconified(true));
        
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("window-control-button");
        closeButton.setOnAction(e -> System.exit(0));
        
        windowControls.getChildren().addAll(minimizeButton, closeButton);
        
        // 使用HBox.setHgrow使标题占据所有可用空间
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        topBar.getChildren().addAll(titleLabel, windowControls);
        
        return topBar;
    }
    
    /**
     * 创建侧边栏
     * Create sidebar
     * 
     * @return 侧边栏 Sidebar
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 2px;");
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(10);
        
        // 添加用户信息区域
        VBox userInfo = createUserInfo();
        sidebar.getChildren().add(userInfo);
        
        // 添加菜单按钮
        Button playButton = createMenuButton("开始游戏", "play");
        Button versionsButton = createMenuButton("版本管理", "versions");
        Button modsButton = createMenuButton("模组管理", "mods");
        Button skinsButton = createMenuButton("皮肤管理", "skins");
        Button settingsButton = createMenuButton("设置", "settings");
        
        // 设置按钮点击事件
        playButton.setOnAction(e -> showPlayPage());
        versionsButton.setOnAction(e -> showVersionsPage());
        modsButton.setOnAction(e -> showModsPage());
        skinsButton.setOnAction(e -> showSkinsPage());
        settingsButton.setOnAction(e -> showSettingsPage());
        
        // 使用VBox.setMargin为设置按钮添加顶部外边距，使其位于底部
        VBox.setMargin(settingsButton, new Insets(30, 0, 0, 0));
        
        sidebar.getChildren().addAll(playButton, versionsButton, modsButton, skinsButton, settingsButton);
        
        return sidebar;
    }
    
    /**
     * 创建用户信息区域
     * Create user info area
     * 
     * @return 用户信息区域 User info area
     */
    private VBox createUserInfo() {
        VBox userInfo = new VBox();
        userInfo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 2px;");
        userInfo.setPadding(new Insets(10));
        userInfo.setSpacing(5);
        userInfo.setAlignment(Pos.CENTER);
        
        // 添加用户头像
        avatarView = new ImageView();
        avatarView.setFitWidth(64);
        avatarView.setFitHeight(64);
        avatarView.setStyle("-fx-background-radius: 50%;");
        
        // 尝试加载默认头像
        try {
            InputStream imageStream = getClass().getResourceAsStream("/images/default_avatar.png");
            if (imageStream != null) {
                avatarView.setImage(new Image(imageStream));
            } else {
                // 如果资源中没有，尝试从文件加载
                File avatarFile = new File("avatar.png");
                if (avatarFile.exists()) {
                    avatarView.setImage(new Image(new FileInputStream(avatarFile)));
                }
            }
        } catch (FileNotFoundException e) {
            LogUtils.error("无法加载头像", e);
        }
        
        // 添加用户名
        usernameLabel = new Label("未登录");
        usernameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        
        // 添加登录按钮
        loginButton = new Button("登录");
        loginButton.getStyleClass().add("login-button");
        loginButton.setOnAction(e -> showLoginView());
        
        userInfo.getChildren().addAll(avatarView, usernameLabel, loginButton);
        
        return userInfo;
    }
    
    /**
     * 创建菜单按钮
     * Create menu button
     * 
     * @param text 按钮文本 Button text
     * @param id 按钮ID Button ID
     * @return 菜单按钮 Menu button
     */
    private Button createMenuButton(String text, String id) {
        Button button = new Button(text);
        button.setId(id);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPadding(new Insets(10));
        button.getStyleClass().add("menu-button");
        
        // 添加悬停效果
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        
        return button;
    }
    
    /**
     * 创建内容区域
     * Create content area
     * 
     * @return 内容区域 Content area
     */
    private StackPane createContentArea() {
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 2px;");
        contentArea.setPadding(new Insets(10));
        
        // 添加欢迎页面
        VBox welcomePage = createWelcomePage();
        contentArea.getChildren().add(welcomePage);
        
        return contentArea;
    }
    
    /**
     * 创建欢迎页面
     * Create welcome page
     * 
     * @return 欢迎页面 Welcome page
     */
    private VBox createWelcomePage() {
        VBox welcomePage = new VBox();
        welcomePage.setAlignment(Pos.CENTER);
        welcomePage.setSpacing(20);
        
        // 添加标题
        Label welcomeLabel = new Label("欢迎使用 WaZi Minecraft Launcher");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        
        // 添加描述
        Label descriptionLabel = new Label("一个现代化的、高性能的、跨平台的Minecraft启动器");
        descriptionLabel.setFont(Font.font("Microsoft YaHei", 14));
        
        // 添加开始游戏按钮
        Button startButton = new Button("开始游戏");
        startButton.getStyleClass().add("start-button");
        startButton.setPadding(new Insets(15, 30, 15, 30));
        
        // 添加按钮悬停效果
        startButton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), startButton);
            st.setToX(1.1);
            st.setToY(1.1);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        
        startButton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), startButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        
        // 设置点击事件
        startButton.setOnAction(e -> {
            Account selectedAccount = accountService.getSelectedAccount();
            if (selectedAccount == null) {
                // 如果未登录，显示登录界面
                showLoginView();
            } else {
                // 如果已登录，显示游戏启动界面
                showPlayPage();
            }
        });
        
        welcomePage.getChildren().addAll(welcomeLabel, descriptionLabel, startButton);
        
        return welcomePage;
    }
    
    /**
     * 创建底部栏
     * Create bottom bar
     * 
     * @return 底部栏 Bottom bar
     */
    private HBox createBottomBar() {
        HBox bottomBar = new HBox();
        bottomBar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 2px;");
        bottomBar.setPadding(new Insets(5, 10, 5, 10));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        
        // 添加版本信息
        Label versionLabel = new Label("版本: 1.0.0   作者: WaZixwx");
        versionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        
        // 添加网站链接
        Hyperlink websiteLink = new Hyperlink("官网: mc.wazixwx.com");
        websiteLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        websiteLink.setOnAction(e -> {
            try {
                SystemUtils.openUrl("https://mc.wazixwx.com");
            } catch (Exception ex) {
                LogUtils.error("无法打开网站", ex);
            }
        });
        
        HBox.setMargin(websiteLink, new Insets(0, 0, 0, 20));
        
        bottomBar.getChildren().addAll(versionLabel, websiteLink);
        
        return bottomBar;
    }
    
    /**
     * 添加窗口拖动功能
     * Add window drag feature
     * 
     * @param scene 场景 Scene
     */
    private void addWindowDragFeature(Scene scene) {
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        
        scene.setOnMousePressed(e -> {
            xOffset[0] = e.getSceneX();
            yOffset[0] = e.getSceneY();
        });
        
        scene.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset[0]);
            stage.setY(e.getScreenY() - yOffset[0]);
        });
    }
    
    /**
     * 加载用户信息
     * Load user info
     */
    private void loadUserInfo() {
        Account selectedAccount = accountService.getSelectedAccount();
        if (selectedAccount != null) {
            // 更新用户信息UI
            updateUserInfoUI(selectedAccount);
            
            // 验证令牌是否有效
            if (selectedAccount.isExpired()) {
                // 令牌已过期，尝试刷新
                refreshAccountToken(selectedAccount);
            } else {
                // 令牌未过期，验证是否有效
                validateAccountToken(selectedAccount);
            }
        }
    }
    
    /**
     * 刷新账号令牌
     * Refresh account token
     * 
     * @param account 账号 Account
     */
    private void refreshAccountToken(Account account) {
        authService.refreshToken(account)
            .thenAccept(success -> {
                if (!success) {
                    // 如果刷新失败，更新UI为未登录状态
                    Platform.runLater(() -> {
                        usernameLabel.setText("未登录");
                        loginButton.setText("登录");
                        loginButton.setOnAction(e -> showLoginView());
                    });
                }
            });
    }
    
    /**
     * 验证账号令牌
     * Validate account token
     * 
     * @param account 账号 Account
     */
    private void validateAccountToken(Account account) {
        authService.validateToken(account)
            .thenAccept(valid -> {
                if (!valid) {
                    // 如果验证失败，尝试刷新令牌
                    refreshAccountToken(account);
                }
            });
    }
    
    /**
     * 更新用户信息UI
     * Update user info UI
     * 
     * @param account 账号 Account
     */
    private void updateUserInfoUI(Account account) {
        Platform.runLater(() -> {
            usernameLabel.setText(account.getUsername());
            loginButton.setText("切换账号");
            
            // TODO: 加载用户皮肤头像
        });
    }
    
    /**
     * 显示登录界面
     * Show login view
     */
    private void showLoginView() {
        // 创建登录视图
        LoginView loginView = new LoginView(
            authService,
            // 登录成功回调
            account -> {
                // 更新用户信息UI
                updateUserInfoUI(account);
                // 隐藏登录视图
                root.getChildren().remove(root.getChildren().size() - 1);
            },
            // 取消登录回调
            () -> root.getChildren().remove(root.getChildren().size() - 1)
        );
        
        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loginView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // 添加登录视图到主界面
        StackPane overlay = new StackPane(loginView);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setAlignment(Pos.CENTER);
        
        root.getChildren().add(overlay);
    }
    
    /**
     * 显示游戏页面
     * Show play page
     */
    private void showPlayPage() {
        // TODO: 实现游戏启动页面
        LogUtils.info("显示游戏启动页面");
    }
    
    /**
     * 显示版本管理页面
     * Show versions management page
     */
    private void showVersionsPage() {
        // TODO: 实现版本管理页面
        LogUtils.info("显示版本管理页面");
    }
    
    /**
     * 显示模组管理页面
     * Show mods management page
     */
    private void showModsPage() {
        // TODO: 实现模组管理页面
        LogUtils.info("显示模组管理页面");
    }
    
    /**
     * 显示皮肤管理页面
     * Show skins management page
     */
    private void showSkinsPage() {
        // TODO: 实现皮肤管理页面
        LogUtils.info("显示皮肤管理页面");
    }
    
    /**
     * 显示设置页面
     * Show settings page
     */
    private void showSettingsPage() {
        // TODO: 实现设置页面
        LogUtils.info("显示设置页面");
    }
    
    /**
     * 显示窗口
     * Show window
     */
    public void show() {
        stage.show();
    }
} 