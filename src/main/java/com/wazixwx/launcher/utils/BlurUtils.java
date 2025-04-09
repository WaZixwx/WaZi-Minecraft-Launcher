package com.wazixwx.launcher.utils;

import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Region;

/**
 * 毛玻璃效果工具类
 * Frosted Glass Effect Utility Class
 * 
 * 提供为JavaFX界面添加毛玻璃效果的工具方法
 * Provides utility methods for adding frosted glass effects to JavaFX UI
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class BlurUtils {
    
    /**
     * 为区域添加盒式模糊效果（毛玻璃效果）
     * Add box blur effect (frosted glass effect) to a region
     * 
     * @param region 要添加效果的区域 | Region to add effect to
     * @param radius 模糊半径 | Blur radius
     * @param iterations 迭代次数 | Iteration count
     */
    public static void addBoxBlur(Region region, double radius, int iterations) {
        BoxBlur blur = new BoxBlur(radius, radius, iterations);
        region.setEffect(blur);
    }
    
    /**
     * 为区域添加高斯模糊效果
     * Add Gaussian blur effect to a region
     * 
     * @param region 要添加效果的区域 | Region to add effect to
     * @param radius 模糊半径 | Blur radius
     */
    public static void addGaussianBlur(Region region, double radius) {
        GaussianBlur blur = new GaussianBlur(radius);
        region.setEffect(blur);
    }
    
    /**
     * 移除区域的效果
     * Remove effect from a region
     * 
     * @param region 要移除效果的区域 | Region to remove effect from
     */
    public static void removeEffect(Region region) {
        region.setEffect(null);
    }
    
    /**
     * 获取当前的模糊效果
     * Get current blur effect
     * 
     * @param region 要获取效果的区域 | Region to get effect from
     * @return 当前的效果，如果没有则返回null | Current effect, or null if none
     */
    public static Effect getCurrentEffect(Region region) {
        return region.getEffect();
    }
    
    /**
     * 检查区域是否有模糊效果
     * Check if region has blur effect
     * 
     * @param region 要检查的区域 | Region to check
     * @return 是否有模糊效果 | Whether region has blur effect
     */
    public static boolean hasBlurEffect(Region region) {
        Effect effect = region.getEffect();
        return effect instanceof BoxBlur || effect instanceof GaussianBlur;
    }
} 