package com.wazixwx.launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import com.wazixwx.launcher.ui.MainWindow;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow();
        mainWindow.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 