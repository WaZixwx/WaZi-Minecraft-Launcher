package com.wazixwx.launcher.utils;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;

/**
 * 样式工具类
 * 用于处理窗口的样式和主题
 */
public class StyleUtils {
    
    /**
     * 应用全局毛玻璃效果到场景
     * @param scene 要应用效果的场景
     * @param radius 模糊半径
     * @param iterations 迭代次数
     */
    public static void applyGlobalBlur(Scene scene, double radius, int iterations) {
        BoxBlur blur = new BoxBlur(radius, radius, iterations);
        blur.setBlurType(BlurType.GAUSSIAN);
        scene.getRoot().setEffect(blur);
    }
    
    /**
     * 应用全局毛玻璃效果到区域
     * @param region 要应用效果的区域
     * @param radius 模糊半径
     * @param iterations 迭代次数
     */
    public static void applyBlur(Region region, double radius, int iterations) {
        BoxBlur blur = new BoxBlur(radius, radius, iterations);
        blur.setBlurType(BlurType.GAUSSIAN);
        region.setEffect(blur);
    }
    
    /**
     * 应用阴影效果到区域
     * @param region 要应用效果的区域
     * @param radius 阴影半径
     * @param spread 阴影扩散
     * @param color 阴影颜色
     */
    public static void applyShadow(Region region, double radius, double spread, Color color) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(radius);
        shadow.setSpread(spread);
        shadow.setColor(color);
        region.setEffect(shadow);
    }
    
    /**
     * 应用圆角样式到区域
     * @param region 要应用样式的区域
     * @param radius 圆角半径（像素）
     */
    public static void applyRoundedCorners(Region region, double radius) {
        region.setStyle(String.format("-fx-background-radius: %fpx;", radius));
    }
    
    /**
     * 应用半透明背景到区域
     * @param region 要应用样式的区域
     * @param color 背景颜色
     * @param opacity 不透明度（0.0-1.0）
     */
    public static void applySemiTransparentBackground(Region region, Color color, double opacity) {
        String hexColor = String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
        
        region.setStyle(String.format("-fx-background-color: %s%02X;", 
            hexColor, 
            (int)(opacity * 255)));
    }
    
    /**
     * 应用全局主题到场景
     * @param scene 要应用主题的场景
     * @param primaryColor 主色调
     * @param secondaryColor 次要色调
     * @param backgroundColor 背景色
     * @param textColor 文本颜色
     */
    public static void applyTheme(Scene scene, Color primaryColor, Color secondaryColor, 
                                 Color backgroundColor, Color textColor) {
        String primaryHex = String.format("#%02X%02X%02X", 
            (int)(primaryColor.getRed() * 255), 
            (int)(primaryColor.getGreen() * 255), 
            (int)(primaryColor.getBlue() * 255));
        
        String secondaryHex = String.format("#%02X%02X%02X", 
            (int)(secondaryColor.getRed() * 255), 
            (int)(secondaryColor.getGreen() * 255), 
            (int)(secondaryColor.getBlue() * 255));
        
        String backgroundHex = String.format("#%02X%02X%02X", 
            (int)(backgroundColor.getRed() * 255), 
            (int)(backgroundColor.getGreen() * 255), 
            (int)(backgroundColor.getBlue() * 255));
        
        String textHex = String.format("#%02X%02X%02X", 
            (int)(textColor.getRed() * 255), 
            (int)(textColor.getGreen() * 255), 
            (int)(textColor.getBlue() * 255));
        
        String css = String.format(
            ".root { -fx-background-color: %s; }\n" +
            ".primary { -fx-background-color: %s; }\n" +
            ".secondary { -fx-background-color: %s; }\n" +
            ".text { -fx-text-fill: %s; }\n" +
            ".button { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
            ".button:hover { -fx-background-color: %s; }\n" +
            ".text-field { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
            ".list-view { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
            ".menu-bar { -fx-background-color: %s; }\n" +
            ".menu-item { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
            ".menu-item:hover { -fx-background-color: %s; }\n" +
            ".tab-pane { -fx-background-color: %s; }\n" +
            ".tab { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
            ".tab:selected { -fx-background-color: %s; }\n",
            backgroundHex, primaryHex, secondaryHex, textHex,
            primaryHex, textHex, secondaryHex,
            backgroundHex, textHex, backgroundHex, textHex,
            primaryHex, backgroundHex, textHex, secondaryHex,
            backgroundHex, backgroundHex, textHex, primaryHex
        );
        
        scene.getStylesheets().clear();
        scene.getStylesheets().add("data:text/css," + css);
    }
    
    /**
     * 应用默认主题到场景
     * @param scene 要应用主题的场景
     */
    public static void applyDefaultTheme(Scene scene) {
        applyTheme(
            scene,
            Color.rgb(33, 150, 243),  // 主色调：蓝色
            Color.rgb(76, 175, 80),   // 次要色调：绿色
            Color.rgb(245, 245, 245), // 背景色：浅灰色
            Color.rgb(33, 33, 33)     // 文本颜色：深灰色
        );
    }
    
    /**
     * 应用暗色主题到场景
     * @param scene 要应用主题的场景
     */
    public static void applyDarkTheme(Scene scene) {
        applyTheme(
            scene,
            Color.rgb(33, 150, 243),  // 主色调：蓝色
            Color.rgb(76, 175, 80),   // 次要色调：绿色
            Color.rgb(33, 33, 33),    // 背景色：深灰色
            Color.rgb(245, 245, 245)  // 文本颜色：浅灰色
        );
    }
} 