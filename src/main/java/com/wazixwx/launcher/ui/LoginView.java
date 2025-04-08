package com.wazixwx.launcher.ui;

import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.service.AuthService;
import com.wazixwx.launcher.utils.AnimationUtils;
import com.wazixwx.launcher.utils.BlurUtils;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.StyleUtils;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 登录界面
 * Login View
 * 
 * 提供Mojang和Microsoft账号登录界面
 * Provides login interface for Mojang and Microsoft accounts
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class LoginView extends BorderPane {
    private static final String MS_OAUTH_CLIENT_ID = "389b1b32-b5d5-43b2-bddc-84ce938d6737"; // 微软OAuth客户端ID
    private static final String MS_OAUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String MS_REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    
    private final AuthService authService;
    private final Consumer<Account> onLoginSuccess;
    private final Runnable onCancel;
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param authService 认证服务 Authentication service
     * @param onLoginSuccess 登录成功回调 Login success callback
     * @param onCancel 取消回调 Cancel callback
     */
    public LoginView(AuthService authService, Consumer<Account> onLoginSuccess, Runnable onCancel) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        this.onCancel = onCancel;
        
        initUI();
    }
    
    /**
     * 初始化UI
     * Initialize UI
     */
    private void initUI() {
        // 设置样式和背景
        setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 2px;");
        setPadding(new Insets(20));
        setPrefWidth(600);
        setPrefHeight(400);
        
        // 添加阴影效果
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(10);
        setEffect(shadow);
        
        // 创建标题
        Label titleLabel = new Label("登录 Minecraft 账号");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        // 创建选项卡面板
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        StyleUtils.applyDefaultStyle(tabPane);
        
        // 创建Mojang登录选项卡
        Tab mojangTab = new Tab("Mojang 账号");
        mojangTab.setContent(createMojangLoginForm());
        
        // 创建Microsoft登录选项卡
        Tab microsoftTab = new Tab("Microsoft 账号");
        microsoftTab.setContent(createMicrosoftLoginForm());
        
        tabPane.getTabs().addAll(mojangTab, microsoftTab);
        
        // 创建取消按钮
        Button cancelButton = new Button("取消");
        StyleUtils.applyButtonStyle(cancelButton);
        cancelButton.setOnAction(e -> onCancel.run());
        
        // 顶部放置标题
        setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));
        
        // 中间放置选项卡面板
        setCenter(tabPane);
        
        // 底部放置取消按钮
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.getChildren().add(cancelButton);
        setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(20, 0, 0, 0));
    }
    
    /**
     * 创建Mojang登录表单
     * Create Mojang login form
     * 
     * @return 表单容器 Form container
     */
    private VBox createMojangLoginForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        
        // 创建表单字段
        Label emailLabel = new Label("邮箱");
        TextField emailField = new TextField();
        emailField.setPromptText("example@example.com");
        StyleUtils.applyTextFieldStyle(emailField);
        
        Label passwordLabel = new Label("密码");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        StyleUtils.applyTextFieldStyle(passwordField);
        
        // 创建登录按钮
        Button loginButton = new Button("登录");
        StyleUtils.applyPrimaryButtonStyle(loginButton);
        loginButton.setPrefWidth(200);
        
        // 创建加载指示器
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setMaxSize(24, 24);
        
        // 创建错误消息标签
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(false);
        
        // 设置登录按钮点击事件
        loginButton.setOnAction(e -> {
            errorLabel.setVisible(false);
            
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("请输入邮箱和密码");
                errorLabel.setVisible(true);
                AnimationUtils.shakeNode(errorLabel, 2, 5);
                return;
            }
            
            // 显示加载状态
            progress.setVisible(true);
            loginButton.setDisable(true);
            
            // 调用登录服务
            authService.loginWithMojang(email, password)
                .whenComplete((account, throwable) -> {
                    Platform.runLater(() -> {
                        progress.setVisible(false);
                        loginButton.setDisable(false);
                        
                        if (throwable != null) {
                            errorLabel.setText("登录失败: " + throwable.getMessage());
                            errorLabel.setVisible(true);
                            AnimationUtils.shakeNode(errorLabel, 2, 5);
                            LogUtils.error("Mojang登录失败", throwable);
                        } else {
                            onLoginSuccess.accept(account);
                        }
                    });
                });
        });
        
        // 创建包含按钮和加载指示器的容器
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(loginButton, progress);
        
        // 添加所有元素到表单
        form.getChildren().addAll(
            emailLabel, emailField,
            passwordLabel, passwordField,
            buttonContainer,
            errorLabel
        );
        
        return form;
    }
    
    /**
     * 创建Microsoft登录表单
     * Create Microsoft login form
     * 
     * @return 表单容器 Form container
     */
    private StackPane createMicrosoftLoginForm() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        
        // 创建主面板
        VBox mainPanel = new VBox(15);
        mainPanel.setAlignment(Pos.CENTER);
        
        // 创建说明文本
        Label infoLabel = new Label("使用Microsoft账号登录需要通过微软官方授权");
        infoLabel.setStyle("-fx-font-size: 14px;");
        
        // 创建登录按钮
        Button msLoginButton = new Button("使用Microsoft账号登录");
        StyleUtils.applyPrimaryButtonStyle(msLoginButton);
        msLoginButton.setPrefWidth(250);
        
        // 创建错误消息标签
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(false);
        
        // 将元素添加到主面板
        mainPanel.getChildren().addAll(infoLabel, msLoginButton, errorLabel);
        
        // 创建Web视图面板（初始隐藏）
        StackPane webViewContainer = new StackPane();
        webViewContainer.setVisible(false);
        
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        // 创建加载指示器
        ProgressIndicator webProgress = new ProgressIndicator();
        webProgress.setMaxSize(50, 50);
        
        // 创建返回按钮
        Button backButton = new Button("返回");
        StyleUtils.applyButtonStyle(backButton);
        backButton.setOnAction(e -> {
            webViewContainer.setVisible(false);
            mainPanel.setVisible(true);
        });
        
        // 添加Web视图和加载指示器
        webViewContainer.getChildren().addAll(webView, webProgress);
        StackPane.setAlignment(webProgress, Pos.CENTER);
        
        // 设置Web视图的加载监听器
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webProgress.setVisible(false);
                
                String url = webEngine.getLocation();
                // 检查URL是否包含授权码
                if (url.startsWith(MS_REDIRECT_URI)) {
                    // 从URL中提取授权码
                    String code = extractCodeFromUrl(url);
                    if (code != null) {
                        // 显示加载指示器
                        webProgress.setVisible(true);
                        
                        // 调用Microsoft登录服务
                        authService.loginWithMicrosoft(code)
                            .whenComplete((account, throwable) -> {
                                Platform.runLater(() -> {
                                    webProgress.setVisible(false);
                                    
                                    if (throwable != null) {
                                        webViewContainer.setVisible(false);
                                        mainPanel.setVisible(true);
                                        errorLabel.setText("登录失败: " + throwable.getMessage());
                                        errorLabel.setVisible(true);
                                        AnimationUtils.shakeNode(errorLabel, 2, 5);
                                        LogUtils.error("Microsoft登录失败", throwable);
                                    } else {
                                        onLoginSuccess.accept(account);
                                    }
                                });
                            });
                    }
                }
            } else if (newState == Worker.State.FAILED) {
                webProgress.setVisible(false);
                LogUtils.error("加载Microsoft登录页面失败: " + webEngine.getLoadWorker().getException());
                webViewContainer.setVisible(false);
                mainPanel.setVisible(true);
                errorLabel.setText("加载Microsoft登录页面失败");
                errorLabel.setVisible(true);
            }
        });
        
        // 设置Microsoft登录按钮点击事件
        msLoginButton.setOnAction(e -> {
            try {
                // 构建Microsoft OAuth URL
                String authUrl = MS_OAUTH_URL + 
                    "?client_id=" + MS_OAUTH_CLIENT_ID + 
                    "&response_type=code" + 
                    "&redirect_uri=" + URLEncoder.encode(MS_REDIRECT_URI, StandardCharsets.UTF_8) + 
                    "&scope=" + URLEncoder.encode("XboxLive.signin offline_access", StandardCharsets.UTF_8);
                
                // 加载登录页面
                webEngine.load(authUrl);
                
                // 切换视图
                mainPanel.setVisible(false);
                webViewContainer.setVisible(true);
                webProgress.setVisible(true);
            } catch (Exception ex) {
                LogUtils.error("启动Microsoft登录过程失败", ex);
                errorLabel.setText("启动Microsoft登录过程失败: " + ex.getMessage());
                errorLabel.setVisible(true);
                AnimationUtils.shakeNode(errorLabel, 2, 5);
            }
        });
        
        // 创建底部面板，用于Web视图时显示返回按钮
        HBox bottomPanel = new HBox();
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        bottomPanel.getChildren().add(backButton);
        
        // 设置Web容器布局
        BorderPane webContainer = new BorderPane();
        webContainer.setCenter(webViewContainer);
        webContainer.setBottom(bottomPanel);
        
        // 添加主面板和Web容器到容器
        container.getChildren().addAll(mainPanel, webContainer);
        
        return container;
    }
    
    /**
     * 从URL中提取授权码
     * Extract authorization code from URL
     * 
     * @param url URL字符串 URL string
     * @return 授权码 Authorization code
     */
    private String extractCodeFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        if (key.equals("code")) {
                            return value;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            LogUtils.error("解析URL失败", e);
            return null;
        }
    }
} 