package com.wazixwx.launcher;

import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.ui.MainWindow;
import com.wazixwx.launcher.utils.LogUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * 启动器入口类
 * Launcher Entry Class
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化日志系统
            // Initialize logging system
            LogUtils.initializeLogging();
            
            // 记录启动信息
            // Log startup information
            LogUtils.info("启动器正在启动 | Launcher is starting up");
            
            // 初始化启动器核心
            // Initialize launcher core
            LauncherCore.getInstance().initialize()
                .thenRunAsync(() -> {
                    // 创建并显示主窗口
                    // Create and show main window
                    Platform.runLater(() -> {
                        MainWindow mainWindow = new MainWindow(primaryStage);
                        // 将主窗口实例保存到LauncherCore中
                        // Save main window instance to LauncherCore
                        LauncherCore.getInstance().setMainWindow(mainWindow);
                        mainWindow.show();
                    });
                })
                .exceptionally(e -> {
                    // 处理初始化错误
                    // Handle initialization error
                    LogUtils.error("启动器初始化失败 | Launcher initialization failed", e);
                    Platform.runLater(() -> showErrorAlert(e));
                    return null;
                });
        } catch (Exception e) {
            LogUtils.error("启动器启动失败 | Launcher startup failed", e);
            showErrorAlert(e);
        }
    }
    
    /**
     * 显示错误弹窗
     * Show error alert
     * 
     * @param e 异常 | Exception
     */
    private void showErrorAlert(Throwable e) {
        // 简单弹窗，实际应用中可以使用更好的UI
        // Simple alert dialog, can use better UI in actual application
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "启动器启动失败 | Launcher startup failed: " + e.getMessage());
        alert.showAndWait();
        Platform.exit();
    }
    
    /**
     * 主方法
     * Main method
     * 
     * @param args 命令行参数 | Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
} 